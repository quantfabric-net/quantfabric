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
package com.quantfabric.algo.market.connector;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import com.quantfabric.algo.commands.CommandFactory;
import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter.CurrencyConverter.CurrencyConversionException;
import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.gateway.BaseMarketAdapter;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.report.Accepted;
import com.quantfabric.algo.order.report.Canceled;
import com.quantfabric.algo.order.report.Filled;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.algo.order.report.PartialFilled;
import com.quantfabric.algo.order.report.Rejected;
import com.quantfabric.messaging.Publisher;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;

import static com.quantfabric.algo.configuration.QuantfabricConstants.SUBSCRIBE_TYPE;


public abstract  class VirtualCoinMarketAdapter extends BaseMarketAdapter {
	
	public static class CurrencyConverter {
		
		public static class CurrencyConversionException extends Exception {

			private static final long serialVersionUID = -2295937582534482280L;
			
			public CurrencyConversionException(String message) {
				
				super(message);
			}			
		}
		
		public static CurrencyPair toCurrencyPair(String symbol) throws CurrencyConversionException {
			
			String[] symbols = symbol.split("_");
			
			switch (symbols.length) {
				case 2 : 
					return new CurrencyPair(symbols[0], symbols[1]);
				case 1 :
					return new CurrencyPair(symbols[0]);
				case 0:
					return CurrencyPair.BTC_USD;
				default :
					throw new CurrencyConverter.CurrencyConversionException("Can't convert currency " + symbol);					
			}
		}
		
		public static String toSymbol(CurrencyPair currencyPair) throws CurrencyConversionException {			
			
			return currencyPair.base.getSymbol() + "_" + currencyPair.counter.getSymbol();
		}
	}
	
	public static class BTCTypeConverter {
		
		public static class BTCConversionError extends Exception {
			
			private static final long serialVersionUID = -5335747761578598274L;

			public BTCConversionError(String message) {
				
				super(message);
			}
		}
		
		public static OrderType toBtcSide(OrderSide orderSide) throws BTCConversionError {
			
			switch (orderSide) {
				case BUY :
					return OrderType.BID;
				case SELL:
					return OrderType.ASK;
				default : 
					throw new BTCTypeConverter.BTCConversionError("Can't convert Order.OrderSide. " + orderSide);
			}
		}
		
		public static OrderSide toOrderSide(OrderType orderType) throws BTCConversionError {
			
			switch (orderType) {
				case BID:
					return OrderSide.BUY;
				case ASK:
					return OrderSide.SELL;
				default : 
					throw new BTCTypeConverter.BTCConversionError("Can't convert OrderType. " + orderType);
			}
		}
	}
	
	public enum SubscribeType{
		POLLING,
		PUSHING
	}
	
	private AdapterStatus adapterStatus;
	private Exchange exchange;
	private MarketDataService marketDataService;
	private TradeService tradeService;
	private long delay = 1000*60;
	private int orderBookDepth = 0;
	private boolean isSetDepth = false;
	private final Properties properties;
	private Timer timer;
	private boolean isTimerCanceled = false;
	private static final AtomicInteger id = new AtomicInteger(0);
	private final Vector<VirtualCoinConfirmationBatch> confirmationBatches = new Vector<VirtualCoinConfirmationBatch>();
	
	public VirtualCoinMarketAdapter(CommandFactory commandFactory, Properties properties) {
		
		super(commandFactory);
		adapterStatus = AdapterStatus.DISCONNECTED;
		this.properties = properties;
		timer = new Timer(true);
	}
	
	public VirtualCoinConfirmationBatch newConfirmationBatch() {
		
		synchronized (confirmationBatches) {
			confirmationBatches.add(new VirtualCoinConfirmationBatch(this));
			return getCurrentBatch();
		}
	}
	
	public VirtualCoinConfirmationBatch getCurrentBatch() {
		synchronized (confirmationBatches) {
			if (confirmationBatches.isEmpty())
				newConfirmationBatch();

			return confirmationBatches.lastElement();
		}
	}
	
	public void subscribeMarketData(String symbol) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, CurrencyConversionException {
		subscribeMarketData(getFeedProvider().getMarketDataFeed(symbol));
		getLogger().info("VirtualCoinAdapter subscribed to " + symbol);
	}
	
	public SubscribeType getSubscribeType(){
		if(properties.containsKey(SUBSCRIBE_TYPE)){
			return SubscribeType.valueOf(properties.getProperty(SUBSCRIBE_TYPE).toUpperCase());
		}
		else{
			return SubscribeType.POLLING;
		}
	}
	
	public void subscribeMarketData(MarketDataFeed feed) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, CurrencyConversionException {
		
		if(getSubscribeType().equals(SubscribeType.PUSHING)){
			pushingSubscribe(feed, this.properties);
		}
		else if(getSubscribeType().equals(SubscribeType.POLLING)){
			pollingSubscribe(feed, this.properties);
		}
		else{
			// this code will never be called, just in case
			throw new RuntimeException("Unknown suscribe type");
		}
	}

	/**
	 * template method for polling subscribe with generally working by default implementation
	 * 
	 * @param feed - feed on which you want to subscribe
	 * @param properties - this parameter could be used in overloaded method where field "properties" is not available
	 * @throws CurrencyConversionException
	 */
	protected void pollingSubscribe(MarketDataFeed feed, Properties properties)
			throws CurrencyConversionException {
		if (isTimerCanceled) {
			timer = new Timer(true);
			isTimerCanceled = false;
		}
		
		final CurrencyPair currency = CurrencyConverter.toCurrencyPair(feed.getInstrument().getSymbol());
		
		if (properties.containsKey("pollingInterval"))
			delay = Integer.parseInt(properties.getProperty("pollingInterval"));
		
		if (properties.containsKey("orderBookDepth")) {
			orderBookDepth = Integer.parseInt(properties.getProperty("orderBookDepth"));
			isSetDepth = true;
		}

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				OrderBook orderBook = null;

				try {

					if (isSetDepth)
						orderBook = marketDataService.getOrderBook(currency, orderBookDepth, orderBookDepth);
					else
						orderBook = marketDataService.getOrderBook(currency);

					getNewId();

					publishNewSnapshot(orderBook, CurrencyConverter.toSymbol(currency));

					marketDataHandler(orderBook, currency);

					publishEndUpdate(orderBook);
				}
				catch (ExchangeException e) {
					e.printStackTrace();
				}
				catch (NotAvailableFromExchangeException e) {
					getLogger().error(e.getMessage());
				}
				catch (NotYetImplementedForExchangeException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					getLogger().error(e.getMessage());
				}
				catch (CurrencyConversionException e) {
					e.printStackTrace();
				}
				catch (Publisher.PublisherException e) {
					e.printStackTrace();
				}
			}

		}, 0, delay);
	}
	
	/**
	 * template method for pushing subscribe 
	 * 
	 * @param feed - feed on which you want to subscribe
	 * @param properties - this parameter could be used in overloaded method where field "properties" is not available
	 * @throws CurrencyConversionException
	 * @throws {@link UnsupportedOperationException} throws if operation is not overriden in custom virtual coin market adapter
	 */
	protected void pushingSubscribe(MarketDataFeed feed, Properties properties) throws CurrencyConversionException{
		throw new UnsupportedOperationException("Pushing subscribe is not implemented in VirtualCoinMarketAdapter. You have override it in your custom adapter");
	}
	
	public abstract void marketDataHandler(OrderBook orderBook, CurrencyPair currency) throws CurrencyConversionException;	
	public abstract ExchangeSpecification createExchangeSpec();
	public abstract void unsubscribeMarketData(MarketDataFeed feed);
	public void unsubscribeMarketData(String symbol) {
		
		unsubscribeMarketData(getFeedProvider().getMarketDataFeed(symbol));
	}	
	
	protected Exchange getExchange() {
		
		return this.exchange;
	}	
	
	public Exchange getExchangeFromSpec(ExchangeSpecification exSpec) {
		
		return ExchangeFactory.INSTANCE.createExchange(exSpec);
	}
	
	public TradeService getTradeService() {
		return tradeService;
	}

	public void setTradeService(TradeService tradeService) {
		this.tradeService = tradeService;
	}
	
	public MarketDataService getMarketDataService() {
		return marketDataService;
	}
	
	public void setMarketDataService(MarketDataService marketDataService) {
		this.marketDataService = marketDataService;
	}

	protected Timer getTimer() {
		
		return this.timer;
	}
	
	public void start() {
		
		ExchangeSpecification exSpec = createExchangeSpec();
		exchange = getExchangeFromSpec(exSpec);	
		setMarketDataService(exchange.getMarketDataService());
		setTradeService(exchange.getTradeService());
	}

	@Override
	public void logon() throws MarketAdapterException {
		
		start();
		adapterStatus = AdapterStatus.CONNECTED;
		invokeLogonListenersByLoggedIn();
	}

	@Override
	public void logout() throws MarketAdapterException {

		adapterStatus = AdapterStatus.DISCONNECTED;
		getTimer().cancel();
		isTimerCanceled = true;
		invokeLogonListenersByLogout();
	}

	@Override
	public void setPassword(String password) throws MarketAdapterException {
		
	}

	@Override
	public AdapterStatus getStatus() {
		return adapterStatus;
	}

	@Override
	public String getIdentifier() {

		StringBuffer identifier = new StringBuffer(); 
		try
		{
			identifier.append(getVenueName() + ";"); 
			identifier.append(exchange.getExchangeSpecification().getHost());
			identifier.append(":" + exchange.getExchangeSpecification().getPort() + ";");
			identifier.append(exchange.getExchangeSpecification().getUserName());
		}
		catch (Exception e)
		{
			getLogger().warn("can't to compose all identifier info");
		}
		return identifier.toString();
	}
	
	protected long getId(OrderBook orderBook) {
		
		return id.longValue();
	}
	
	protected synchronized void getNewId() {
		
		id.incrementAndGet();
	}
	
	protected String getSourceName() {
		
		return getVenueName();
	}
	
	protected long getSourceTimestamp(OrderBook orderBook) {
		
		return orderBook.getTimeStamp().getTime();
	}
	
	protected int getItemCount(OrderBook orderBook) {
		
		return orderBook.getBids().size() + orderBook.getAsks().size();		
	}
	
	protected void addNewOrder(String orderId, TradeOrder tradeOrder, boolean isOrderRejectRequired) {
		synchronized (this) {
			
			getCurrentBatch().addOrder(orderId, tradeOrder, isOrderRejectRequired);
		}
	}	
	
	public String sendMessage(LimitOrder order, TradeOrder tradeOrder) throws Exception  {
		
		String orderId = null;
		try {
			orderId = getTradeService().placeLimitOrder(order);
		} catch (ExchangeException e) {
			getLogger().error("Rejecting order: [" + tradeOrder.getOrderReference() + "]. Reason: " + e.getMessage());
			addNewOrder(e.getMessage(), tradeOrder, true);
		} 
		return orderId;
	}
	
	public void addOrder(String orderId, TradeOrder tradeOrder) {
		
		addNewOrder(orderId, tradeOrder, false);
	}
	
	public String getMarketOrderReference(String orderId) {
		
		return getCurrentBatch().getMarketOrderReference(orderId);
	}
	
	public synchronized void cancelOrder(String orderId) {
		
		String orderReference = getMarketOrderReference(orderId);
		
		try {			
			if (orderReference != null)
				getTradeService().cancelOrder(orderReference);
			else 
				getLogger().error("Can't cancel order. Order reference is null");
		} catch (ExchangeException e) {
			e.printStackTrace();
		} catch (NotAvailableFromExchangeException e) {
			e.printStackTrace();
		} catch (NotYetImplementedForExchangeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void confirmOrderAcceptance(String orderId, String orderReference)
			throws Publisher.PublisherException {

		long messageId = Long.valueOf(orderId);
		String sourceName = getVenueName();
		Date sourceTimestamp = new Date();
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;

		OrderExecutionReport orderExecutionReport = new Accepted(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);

		publish(orderExecutionReport);
	}
	
	protected void confirmOrderPartialFill(String orderId, String orderReference, String instrumentId, double tradePrice, double tradeSize)
			throws Publisher.PublisherException, CurrencyConversionException {

		long messageId = Long.valueOf(orderId);
		String sourceName = getSourceName();
		Date sourceTimestamp = new Date();
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;
		long price = getInstrumentProvider().getInstrument(instrumentId).castToLong(tradePrice);

		OrderExecutionReport orderExecutionReport = new PartialFilled(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId, price, tradeSize);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);

		publish(orderExecutionReport);
	}
	
	protected void confirmOrderFill(String orderId, String orderReference, String instrumentId, double tradePrice, double tradeSize) throws Publisher.PublisherException,
			CurrencyConversionException {

		long messageId = Long.valueOf(orderId);
		String sourceName = getSourceName();
		Date sourceTimestamp = new Date();
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;
		long price = getInstrumentProvider().getInstrument(instrumentId).castToLong(tradePrice);

		OrderExecutionReport orderExecutionReport = new Filled(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId, price, tradeSize);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);
		
		publish(orderExecutionReport);
	}
	
	protected void confirmOrderCancel(String orderId, String orderReference, long timestamp) throws Publisher.PublisherException,
			CurrencyConversionException {
			
		long messageId = Long.valueOf(orderId);
		String sourceName = getSourceName();
		Date sourceTimestamp = new Date(timestamp);
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;

		OrderExecutionReport orderExecutionReport = new Canceled(messageId, sourceName, sourceTimestamp, institutionOrderReference, localOrderReference,
				executionId);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);

		publish(orderExecutionReport);
	}
	
	protected void confirmOrderReject(String orderId, String orderReference, long timestamp)
			throws Publisher.PublisherException, CurrencyConversionException {

		long messageId = 0;
		String sourceName = getSourceName();
		Date sourceTimestamp = new Date(timestamp);
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;
		String reason = orderId;

		OrderExecutionReport orderExecutionReport = new Rejected(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId, reason);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);

		publish(orderExecutionReport);
	}
	
	protected void publishNewSnapshot(OrderBook orderBook, String symbol) throws Publisher.PublisherException {
		
		Feed feed = getFeedProvider().getMarketDataFeed(symbol);
		
		publish(new NewSnapshot(getId(orderBook), getSourceName(), 
				getSourceTimestamp(orderBook), getItemCount(orderBook), 
				symbol, feed.getFeedId(), feed.getFeedName().getName()));
	}
	
	protected void publishEndUpdate(OrderBook orderBook) throws Publisher.PublisherException {
		
		EndUpdate endUpdate = 
				new EndUpdate(getId(orderBook), MDMessageType.SNAPSHOT,
					getSourceName(), getSourceTimestamp(orderBook), getItemCount(orderBook));

			publish(endUpdate);
	}
}
