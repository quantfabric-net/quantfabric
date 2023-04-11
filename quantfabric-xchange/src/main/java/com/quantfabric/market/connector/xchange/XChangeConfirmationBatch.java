/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quantfabric.market.connector.xchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.gateway.MarketAdapter;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.messaging.Publisher.PublisherException;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

public class XChangeConfirmationBatch {

    private final static Logger log = LoggerFactory.getLogger(XChangeConfirmationBatch.class);
    public static final int POINTS_IN_ONE = 100000000;
    public static final double MINIMAL_SIZE = 0.1;

    private final Timer pollingTimer = new Timer();
    private MarketAdapter marketAdapter;
    private final long delay = 30000;
    private final Map<String, XChangeOrderExecutionState> ordersList = new HashMap<String, XChangeOrderExecutionState>();

    public XChangeConfirmationBatch(MarketAdapter marketAdapter) {

        this.marketAdapter = marketAdapter;
        setupConfirmationPoller(delay);
    }

    public MarketAdapter getMarketAdapter() {

        return marketAdapter;
    }

    public void setMarketAdapter(MarketAdapter marketAdapter) {

        this.marketAdapter = marketAdapter;
    }

    private Set<String> getOrderReferences() {

        synchronized (ordersList) {
            return new HashSet<String>(ordersList.keySet());
        }
    }

    public Collection<XChangeOrderExecutionState> getOrders() {

        synchronized (ordersList) {
            return ordersList.values();
        }
    }

    public String getMarketOrderReference(String orderId) {

        for (XChangeOrderExecutionState order : getOrders()) {
            if (order.getTradeOrder().getOrderReference().equals(orderId))
                return order.getMarketOrderReference();
        }

        return null;
    }

    public void addOrder(String orderId, TradeOrder order, boolean isOrderRejectRequired) {

        synchronized (ordersList) {
            ordersList.put(orderId, new XChangeOrderExecutionState(order, orderId, isOrderRejectRequired));
        }
    }

    public XChangeOrderExecutionState getOrder(String orderId) {
        synchronized (ordersList) {
            return ordersList.get(orderId);
        }
    }

    public void removeOrder(String orderId) {
        synchronized (ordersList) {
            if (ordersList.containsKey(orderId)) {
                log.info("removing executed order [" + getOrder(orderId).getTradeOrder().getOrderReference() + "] from confirmation batch");
                ordersList.remove(orderId);
            }
        }
    }

    private void setupConfirmationPoller(long delay) {

        TimerTask poller = new TimerTask() {

            @Override
            public synchronized void run() {

                log.info("Starting confirmation polling");

                if (getMarketAdapter() != null) {
                    try {

                        List<LimitOrder> limitOrders = ((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getOpenOrders()
                                .getOpenOrders();
                        List<Trade> trades = ((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getTradeHistory(null)
                                .getTrades();

                        Set<String> orderReferences = getOrderReferences();

                        List<XChangeOrderExecutionState> orders = new LinkedList<XChangeOrderExecutionState>(getOrders());

                        if (!orderReferences.isEmpty()) {
                            for (String orderReference : orderReferences) {
                                for (LimitOrder limitOrder : limitOrders) {
                                    if (limitOrder.getId().equals(orderReference)) {
                                        XChangeOrderExecutionState executionState = getOrder(orderReference);
                                        executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.Accepted);
                                    }
                                }
                            }
                        }

                        log.debug("Open orders proceeded. Starting transaction history polling");

                        if (!orderReferences.isEmpty())
                            for (String orderReference : orderReferences) {
                                for (Trade trade : trades) {
                                    if (trade.getId().equals(orderReference)) {
                                        XChangeOrderExecutionState executionState = getOrder(orderReference);

                                        if (!isPartiallyFilled(executionState, trade)) {
                                            if (executionState.isAccepted()) {
                                                executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.Filled);
                                                executionState.setCompleted(true);
                                            } else {
                                                executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.FilledWithoutAcceptance);
                                                executionState.setCompleted(true);
                                            }
                                        } else {
                                            executionState.setPartiallyFilled(true);

                                            if (executionState.isAccepted())
                                                executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.PartiallyFilled);
                                            else {
                                                executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.PartiallyFilledWithoutAcceptance);
                                            }
                                        }
                                        executionState.setTrade(trade);
                                    }
                                }
                            }

                        log.debug("Transaction history proceeded. Searching for cancelled orders");

                        // Ensures double cancel checking will not throw exception due to small polling interval
                        Thread.sleep(2000);
                        /**/
                        if (!orderReferences.isEmpty())
                            for (String orderReference : orderReferences) {
                                if (!checkExistingOrders(((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getOpenOrders().getOpenOrders(), orderReference)
                                        && !checkExistingOrders(((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getTradeHistory(null).getTrades(), orderReference)) {

                                    Thread.sleep(2000);

                                    if (!checkExistingOrders(((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getOpenOrders().getOpenOrders(), orderReference)
                                            && !checkExistingOrders(((XChangeMarketAdapter) getMarketAdapter()).connection.getExchange().getTradeService().getTradeHistory(null).getTrades(), orderReference)) {

                                        XChangeOrderExecutionState executionState = getOrder(orderReference);
                                        if (!executionState.getExecutionState().equals(XChangeOrderExecutionState.XChangeExecutionState.Rejected))
                                            executionState.setExecutionState(XChangeOrderExecutionState.XChangeExecutionState.Cancelled);
                                    }
                                }
                            }

                        for (XChangeOrderExecutionState executionState : orders) {
                            processExecutionReport(executionState, scheduledExecutionTime());
                        }

                        log.debug("Confirmation polling finished");
                    } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                        log.error("Json mapping exception raised. Can't deserialize responce from server. ", e);
                    } catch (ExchangeException e) {
                        log.error("ExchangeException: " + e);
                    } catch (NotAvailableFromExchangeException e) {
                        log.error("NotAvailableFromExchangeException : " + e);
                    } catch (NotYetImplementedForExchangeException e) {
                        log.error("NotYetImplementedForExchangeException exception: " + e);
                    } catch (IOException e) {
                        log.error("ExchangeException: " + e);
                    } catch (PublisherException e) {
                        log.error("PublisherException: " + e);
                    } catch (XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException e) {
                        log.error("CurrencyConversionException: " + e);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException: " + e);
                    }
                }
            }
        };
        pollingTimer.schedule(poller, 1000, delay);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean checkExistingOrders(List collection, String orderReference) {

        if (!collection.isEmpty()) {
            if (collection.get(0) instanceof LimitOrder) {
                List<LimitOrder> limitOrders = (List<LimitOrder>) collection;
                for (LimitOrder limitOrder : limitOrders) {
                    if (limitOrder.getId().equals(orderReference))
                        return true;
                }
            } else if (collection.get(0) instanceof Trade) {
                List<Trade> trades = (List<Trade>) collection;
                for (Trade trade : trades) {
                    if (trade.getId().equals(orderReference))
                        return true;
                }
            }
        }

        return false;
    }

    private boolean isPartiallyFilled(XChangeOrderExecutionState execState, Trade trade) {

        long tradeAmount = (long) Math.abs(trade.getOriginalAmount().doubleValue() * POINTS_IN_ONE);
        long leavesQty = Math.abs(execState.getLeavesQty());
        double tradePrice = trade.getPrice().doubleValue();

        List<XChangeOrderExecutionState.PartiallyFilledOrder> partialFills = new ArrayList<XChangeOrderExecutionState.PartiallyFilledOrder>();

        if (execState.getPartialFills() != null)
            partialFills.addAll(execState.getPartialFills());

        if (!execState.isCompleted()) {
            if (!partialFills.isEmpty()) {

                if (!partialFillExists(partialFills, trade)) {
                    if (Math.abs(tradeAmount - leavesQty) <= MINIMAL_SIZE)
                        return false;

                    execState.addPartialFills(execState.getMarketOrderReference(), tradeAmount, tradePrice, trade.getTimestamp());
                    return true;
                }

                return true;
            } else {
                execState.addPartialFills(execState.getMarketOrderReference(), tradeAmount, tradePrice, trade.getTimestamp());
                return true;
            }
        }

        return false;
    }

    private boolean partialFillExists(List<XChangeOrderExecutionState.PartiallyFilledOrder> partialFills, Trade trade) {

        XChangeOrderExecutionState.PartiallyFilledOrder order = new XChangeOrderExecutionState.PartiallyFilledOrder(
                trade.getId(), trade.getOriginalAmount().doubleValue(), trade.getPrice().doubleValue(), trade.getTimestamp());

        return partialFills.contains(order);
    }

    private synchronized void processExecutionReport(XChangeOrderExecutionState executionState, long executionTimestamp) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {

        switch (executionState.getExecutionState()) {
            case Accepted:
                handleAccepted(executionState);
                break;
            case PartiallyFilled:
                handlePartiallyFilled(executionState);
                break;
            case Filled:
                handleFilled(executionState);
                break;
            case Cancelled:
                handleCancelled(executionState, executionTimestamp);
                break;
            case FilledWithoutAcceptance:
                handleFilledWithoutAcceptance(executionState);
                break;
            case PartiallyFilledWithoutAcceptance:
                handlePartiallyFilledWithoutAcceptance(executionState);
                break;
            case Rejected:
                handleRejected(executionState, executionTimestamp);
                break;
            default:
                break;
        }
    }

    private void handleAccepted(XChangeOrderExecutionState executionState) throws PublisherException {
        if (!executionState.isAccepted()) {
            ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderAcceptance(executionState
                    .getMarketOrderReference(), executionState.getTradeOrder().getOrderReference());
            executionState.setAccepted(true);
        }
    }

    private void handleFilled(XChangeOrderExecutionState executionState) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        if (executionState.isPartiallyFilled())
            handlePartiallyFilled(executionState);

        ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderFill(
                executionState.getMarketOrderReference(),
                executionState.getTradeOrder().getOrderReference(),
                executionState.getTradeOrder().getInstrumentId(),
                executionState.getAvgPx(),
                executionState.getQuantity());
        removeOrder(executionState.getMarketOrderReference());
    }

    private void handlePartiallyFilled(XChangeOrderExecutionState executionState) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        List<XChangeOrderExecutionState.PartiallyFilledOrder> partialFills = executionState.getPartialFills();

        for (XChangeOrderExecutionState.PartiallyFilledOrder partialFill : partialFills) {
            if (!partialFill.isReported())
                if (executionState.getLeavesQty() > MINIMAL_SIZE) {
                    ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderPartialFill(
                            executionState.getMarketOrderReference(),
                            executionState.getTradeOrder().getOrderReference(),
                            executionState.getTradeOrder().getInstrumentId(),
                            partialFill.getTradePrice(),
                            partialFill.getTradeAmount() / POINTS_IN_ONE);
                    partialFill.setReported(true);
                } else {
                    ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderFill(
                            executionState.getMarketOrderReference(),
                            executionState.getTradeOrder().getOrderReference(),
                            executionState.getTradeOrder().getInstrumentId(),
                            executionState.getAvgPx(),
                            executionState.getQuantity());
                    removeOrder(executionState.getMarketOrderReference());
                }
        }
    }

    private void handleFilledWithoutAcceptance(XChangeOrderExecutionState executionState) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        handleAccepted(executionState);
        handleFilled(executionState);
    }

    private void handlePartiallyFilledWithoutAcceptance(XChangeOrderExecutionState executionState) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        handleAccepted(executionState);
        handlePartiallyFilled(executionState);
    }

    private void handleCancelled(XChangeOrderExecutionState executionState, long executionTimestamp) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderCancel(executionState.getMarketOrderReference(),
                executionState.getTradeOrder().getOrderReference(),
                executionTimestamp);
        removeOrder(executionState.getMarketOrderReference());
    }

    private void handleRejected(XChangeOrderExecutionState executionState, long executionTimestamp) throws PublisherException, XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException {
        ((XChangeMarketAdapter) getMarketAdapter()).confirmOrderReject(executionState.getMarketOrderReference(),
                executionState.getTradeOrder().getOrderReference(),
                executionTimestamp);
        removeOrder(executionState.getMarketOrderReference());
    }
}
