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
package com.quantfabric.algo.market.connector.binance.stream;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.*;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter;
import com.quantfabric.market.connector.xchange.XChangeQuantfabricService;
import com.quantfabric.market.connector.xchange.streaming.StreamingTradeService;
import com.quantfabric.util.UnsupportedMDTypeForExchange;
import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderType;
import org.knowm.xchange.binance.dto.trade.TimeInForce;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.function.BiConsumer;

import static com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingXChangeAdapter.getCurrencyPair;
import static com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingXChangeAdapter.getProductSubscriptionBuilder;

public class BinanceStreamingTradeService extends BinanceMarketDataServiceRaw
        implements XChangeQuantfabricService, StreamingTradeService {

    private static final Logger logger = LoggerFactory.getLogger(BinanceStreamingTradeService.class);
    BinanceStreamingXChangeAdapter adapter;

    protected BinanceStreamingTradeService(BinanceExchange exchange,
                                           BinanceAuthenticated binance,
                                           ResilienceRegistries resilienceRegistries,
                                           BinanceStreamingXChangeAdapter adapter) {
        super(exchange, binance, resilienceRegistries);
        this.adapter = adapter;
    }

    @Override
    public void subscribe(HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap) {
        if(!(((BinanceStreamingXChange)exchange).isConnected())) {
            ((BinanceStreamingXChange)exchange).connect(
                    getProductSubscriptionBuilder(feedEventMap).build()
            ).blockingAwait();
        }
        for (Feed f : feedEventMap.keySet()) {
            if(f instanceof ExecutionFeed) {
                CurrencyPair currencyPair = getCurrencyPair(f.getInstrument());
                Disposable subscription = applyHandler(feedEventMap, f, currencyPair);
                f.setDisposable(subscription);
            }
        }
    }

    @Override
    public Observable<TradeOrder>getOrderChanges(CurrencyPair currencyPair, Object... args) {
        //1) Filter all execRaw by specified currency in the .xml file.
        //2) remove REJECTED execution type from the map
        //3) then cast ExecutionReportBinanceUserTransaction to TradeOrder type
        return ((BinanceStreamingXChange)exchange)
                .getStreamingTradeService()
                .getRawExecutionReports().filter(rep -> currencyPair.equals(
                        XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(
                                String.valueOf(rep.getCurrencyPair())
                        )
                )).filter(rep ->
                        !rep.getExecutionType().equals(ExecutionReportBinanceUserTransaction.ExecutionType.REJECTED)
                ).map(this::tradeOrderFromRawExecutionReport);
    }


    @Override
    public Observable<TradeOrder> getOrderChanges(Instrument instrument, Object... args) {
        return getOrderChanges(getCurrencyPair(instrument), args);
    }

    @Override
    public Observable<OrderExecutionReport> getExecutionReport(CurrencyPair currencyPair, Object... args) {
        return ((BinanceStreamingXChange)exchange)
                .getStreamingTradeService()
                .getRawExecutionReports().filter(rep -> currencyPair.equals(
                        XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(
                                String.valueOf(rep.getCurrencyPair())
                        )
                )).map(this::fromRawExecutionReport);
    }

    @Override
    public Observable<OrderExecutionReport> getExecutionReport(Instrument instrument, Object... args) {
        return getExecutionReport(getCurrencyPair(instrument), args);
    }

    private Disposable applyHandler(HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap, Feed feed, CurrencyPair currencyPair) {
        Disposable subscription = null;
        if (feed instanceof ExecutionFeed) {
            switch (((ExecutionFeed) feed).getExecutionType()) {
                case EXECUTION_REPORT:
                    //reference: BinanceStreamingXChangeAdapter.userTradesObservation()
                    subscription = getExecutionReport(currencyPair).subscribe(
                            execRep -> feedEventMap.get(feed).accept(feed, execRep),
                            throwable -> LOG.error("ERROR during processing execution report: ", throwable)
                    );
                    break;
                case ORDER_CHANGES:
                    //reference: BinanceStreamingXChangeAdapter.orderChangesObservation()
                    subscription = this.getOrderChanges(currencyPair).subscribe(
                            orderChange -> feedEventMap.get(feed).accept(feed, orderChange),
                            throwable -> LOG.error("ERROR during processing order change: ", throwable)
                    );
                    break;
                default:
                    throw new UnsupportedMDTypeForExchange("BinanceStreamingExchange does not support {}" + ((ExecutionFeed) feed).getExecutionType());

            }
        }
        return subscription;
    }

    private TradeOrder tradeOrderFromRawExecutionReport(ExecutionReportBinanceUserTransaction raw) {
        Instrument instrument = adapter.getInstrumentProvider().
                getInstrumentBySymbol(raw.getCurrencyPair().base.toString());
        TradeOrder tradeOrder = new TradeOrder(raw.getClientOrderId());
        tradeOrder.setInstrumentId(instrument.getId());
        tradeOrder.setInstrument(instrument);
        tradeOrder.setOrderReference(raw.getClientOrderId());
        tradeOrder.setOrderSide(convertOrderSide(raw.getSide()));
        tradeOrder.setOrderType(convertOrderType(raw.getOrderType()));
        tradeOrder.setPrice(instrument.castToLong(raw.getOrderPrice()));
        //tradeOrder.setPrice2();
        tradeOrder.setSize(1);
        tradeOrder.setStopPrice(instrument.castToLong(raw.getStopPrice()));
        tradeOrder.setTimeInForceMode(convertTimeInForce(raw.getTimeInForce()));
        return tradeOrder;
    }
    private OrderExecutionReport fromRawExecutionReport(ExecutionReportBinanceUserTransaction raw) {
        String orderReference = raw.getClientOrderId();
        OrderExecutionReport report = null;
        long timestamp = raw.getTimestamp();
        long messageId = java.lang.System.nanoTime(); //orderId or tradeId or clientSideId or generated from timestamp (nanos)
        String sourceName = adapter.getVenueName();
        Date doneTransaction = new Date();
        String text = String.valueOf(messageId);

        Instrument instrument = adapter.getInstrumentProvider().getInstrumentBySymbol(raw.getCurrencyPair().base.toString());
        switch (raw.getExecutionType()) {
            case NEW:
                logger.info("Handled NEW report");
                report = new Accepted(messageId, sourceName, timestamp,orderReference, orderReference, orderReference);
                break;
            case CANCELED:
                switch (raw.getCurrentOrderStatus()) {
                    case CANCELED:
                        logger.info("Handled CANCELED report");
                        report = new Canceled(messageId, sourceName, timestamp, orderReference, orderReference, orderReference);
                        break;
                    case PENDING_CANCEL:
                        logger.info("Handled PENDING_CANCEL report");
                        report = new PendingCancel(messageId, sourceName, timestamp, orderReference, orderReference, orderReference);
                        break;
                }
                break;
            case REJECTED:
                logger.info("Handled REJECTED report, reason: {} ", raw.getOrderRejectReason());
                report = new Rejected(messageId, sourceName, timestamp, orderReference,
                        orderReference, orderReference, raw.getOrderRejectReason());
                break;
            case REPLACED:
                logger.info("Handled REPLACED report");
                report = new Replaced(messageId, sourceName, timestamp, orderReference, orderReference, orderReference);
                break;
            case EXPIRED:
                logger.info("Handled EXPIRED report");
                report = new Expired(messageId, sourceName, timestamp, orderReference, orderReference, orderReference);
                break;
            case TRADE:
                switch (raw.getCurrentOrderStatus()) {
                    case FILLED:
                        logger.info("Handled Filled Trade  report");
                        report = new Filled(messageId, sourceName, new Date(timestamp), orderReference, orderReference, orderReference,
                                instrument.castToLong(raw.getOrderPrice()), instrument.castToLong(raw.getOrderQuantity()));
                        break;
                    case PARTIALLY_FILLED:
                        logger.info("Handled Partial Filled Trade report");
                        report = new PartialFilled(messageId, sourceName, new Date(timestamp), orderReference, orderReference, orderReference,
                                instrument.castToLong(raw.getOrderPrice()), instrument.castToLong(raw.getOrderQuantity()));
                        break;
                }
                break;
        }
        if (report != null) {
            report.setOriginalLocalOrderReference(null);
            report.setDoneTransationTime(doneTransaction);
            report.setText(text);
        }
        return report;
    }

    private TradeOrder.OrderSide convertOrderSide(OrderSide orderSide) {
        if(OrderSide.SELL.equals(orderSide)) return TradeOrder.OrderSide.SELL;
        return TradeOrder.OrderSide.BUY;
    }

    private TradeOrder.OrderType convertOrderType(OrderType orderType) {
        TradeOrder.OrderType type = null;
        switch (orderType) {
            case LIMIT:
                type = TradeOrder.OrderType.LIMIT;
                break;
            case STOP_LOSS:
                type = TradeOrder.OrderType.STOP_LOSS;
                break;
            case MARKET:
                type = TradeOrder.OrderType.MARKET;
                break;
            case LIMIT_MAKER:
                type = TradeOrder.OrderType.LIMIT_MAKER;
                break;
            case TAKE_PROFIT:
                type = TradeOrder.OrderType.TAKE_PROFIT;
                break;
            case STOP_LOSS_LIMIT:
                type = TradeOrder.OrderType.STOP_LOSS_LIMIT;
                break;
            case TAKE_PROFIT_LIMIT:
                type = TradeOrder.OrderType.TAKE_PROFIT_LIMIT;
        }
        return type;
    }

    private TradeOrder.TimeInForceMode convertTimeInForce(TimeInForce tif) {
        TradeOrder.TimeInForceMode tifm = null;
        switch (tif) {
            case FOK:
                tifm = TradeOrder.TimeInForceMode.FILL_OR_KILL;
                break;
            case GTC:
                tifm = TradeOrder.TimeInForceMode.GOOD_TILL_CANCEL;
                break;
            case IOC:
                tifm = TradeOrder.TimeInForceMode.IMMEDIATE_OR_CANCEL;
                break;
        }
        return  tifm;
    }
}
