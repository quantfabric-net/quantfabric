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

import com.quantfabric.algo.commands.CommandFactory;
import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.gateway.BaseMarketAdapter;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.report.*;
import com.quantfabric.market.connector.xchange.exception.EmptyPropertiesException;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.util.UnsupportedMDTypeForExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

import static com.quantfabric.algo.configuration.QuantfabricConstants.SUBSCRIBE_TYPE;


public abstract  class XChangeMarketAdapter extends BaseMarketAdapter {

	public static class CurrencyConverter {
		private CurrencyConverter(){}
		
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
					throw new CurrencyConversionException("Can't convert currency " + symbol);
			}
		}
		
		public static String toSymbol(CurrencyPair currencyPair) throws CurrencyConversionException {			
			
			return currencyPair.base + "_" + currencyPair.counter;
		}
	}
	
	public static class BTCTypeConverter {
		private BTCTypeConverter(){}
		
		public static class BTCConversionError extends Exception {
			
			private static final long serialVersionUID = -5335747761578598274L;

			public BTCConversionError(String message) {
				
				super(message);
			}
		}
		
		public static Order.OrderType toBtcSide(OrderSide orderSide) throws BTCConversionError {
			
			switch (orderSide) {
				case BUY :
					return Order.OrderType.BID;
				case SELL:
					return Order.OrderType.ASK;
				default : 
					throw new BTCConversionError("Can't convert Order.OrderSide. " + orderSide);
			}
		}
		
		public static OrderSide toOrderSide(Order.OrderType orderType) throws BTCConversionError {
			
			switch (orderType) {
				case BID :
					return OrderSide.BUY;
				case ASK:
					return OrderSide.SELL;
				default : 
					throw new BTCConversionError("Can't convert OrderType. " + orderType);
			}
		}
	}

	private abstract class PollingTask extends TimerTask {
		MarketDataFeed feed;
		Properties properties;
		int orderBookDepth;
		boolean isSetDepth = false;
		XChangeMarketAdapter adapter;
		final CurrencyPair currency;

		public PollingTask(MarketDataFeed feed,Properties props,XChangeMarketAdapter adapter) throws CurrencyConverter.CurrencyConversionException {
			this.feed = feed;
			this.properties=props;
			this.adapter=adapter;
			this.currency = CurrencyConverter.toCurrencyPair(this.feed.getInstrument().getSymbol());
			if (properties.containsKey("orderBookDepth")) {
				orderBookDepth = Integer.parseInt(properties.getProperty("orderBookDepth"));
				isSetDepth = true;
			}
		}

	}
	
	public enum SubscribeType{
		POLLING,
		PUSHING
	}
	

	private long period = 1000*60;
	private Properties properties;
	private Timer timer;
	private boolean isTimerCanceled = false;
	private final Map<MarketDataFeed,TimerTask> pollingTasks = new HashMap<>();
	private AdapterStatus adapterStatus;
	protected final XChangeMarketConnection connection;

	
	protected XChangeMarketAdapter(XChangeMarketConnection<? extends XChange> connection,CommandFactory commandFactory, Properties properties) {
		super(commandFactory);
		adapterStatus = AdapterStatus.DISCONNECTED;
		this.connection = connection;
		this.properties = properties;
		timer = new Timer(true);
	}

	public Properties getProperties() {
		return properties;
	}

	@Override
	public void logon() throws MarketAdapterException {

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

		StringBuilder identifier = new StringBuilder();
		try
		{
			identifier.append(getVenueName()).append(";");
			identifier.append(connection.getExchange().getExchangeSpecification().getHost());
			identifier.append(":").append( connection.getExchange().getExchangeSpecification().getPort()).append(";");
			identifier.append(connection.getExchange().getExchangeSpecification().getUserName());
		}
		catch (Exception e)
		{
			getLogger().warn("can't to compose all identifier info");
		}
		return identifier.toString();
	}

	public SubscribeType getSubscribeType(){
		try {
			propertyCheck();
		} catch (EmptyPropertiesException e) {
			getLogger().error("Property is empty: ", e);
		}
		if(properties.containsKey(SUBSCRIBE_TYPE)){
			return SubscribeType.valueOf(properties.getProperty(SUBSCRIBE_TYPE).toUpperCase());
		}
		else{
			return SubscribeType.POLLING;
		}
	}

	private void propertyCheck() throws EmptyPropertiesException{
		if(properties == null || properties.isEmpty()) {
			properties = new Properties();
			properties.put(SUBSCRIBE_TYPE, "pushing");
			throw new EmptyPropertiesException();
		}
	}

	public void subscribeMarketData(String symbol) throws CurrencyConverter.CurrencyConversionException {
		if(getSubscribeType().equals(SubscribeType.POLLING)){
			pollingSubscribe(getFeedProvider().getMarketDataFeed(symbol), this.properties);
			getLogger().info("XChangeMarketAdapter subscribed to {}", symbol);
		} else{
			// this code will never be called, just in case
			throw new RuntimeException("Unknown subscribe type");
		}
	}

	//for streaming providers with batch subscribe flag (executionFeeds + marketDataFeeds)
	public void subscribeMarketData(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds){
		if(getSubscribeType().equals(SubscribeType.PUSHING)) {
			pushingSubscribe(mdFeeds, exFeeds, this.properties);
			getLogger().info("XChangeMarketAdapter subscribed to {}, {}", mdFeeds, exFeeds);
		} else{
			// this code will never be called, just in case
			throw new RuntimeException("Unknown subscribe type");
		}
	}
	/**
	 * template method for pushing subscribe
	 *
	 * @param feed - feed on which you want to subscribe
	 * @param properties - this parameter could be used in overloaded method where field "properties" is not available
	 * @throws CurrencyConverter.CurrencyConversionException
	 * @throws {@link UnsupportedOperationException} throws if operation is not overriden in custom virtual coin market adapter
	 */
	protected void pushingSubscribe(MarketDataFeed feed, Properties properties) throws CurrencyConverter.CurrencyConversionException {
		connection.getExchange()
				.getXChangeQuantfabricService()
				.subscribe(feed, this::marketDataHandler);

	}
	//Batch subscribe with both marketData and execution feeds type
	protected void pushingSubscribe(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds, Properties properties) {
		HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap = createFeedEventMap(mdFeeds, exFeeds);
		if (!mdFeeds.isEmpty()) {
			connection.getExchange()
					.getXChangeMarketDataService()
					.subscribe(feedEventMap);
		}
		if (!exFeeds.isEmpty()) {
			connection.getExchange()
					.getXChangeTradeService()
					.subscribe(feedEventMap);
		}
	}

	@SuppressWarnings("unused")
	protected HashMap<Feed, BiConsumer<Feed, Object>> createFeedEventMap (Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) {
		return new HashMap<>();
	}

	/**
	 * template method for polling subscribe with generally working by default implementation
	 * 
	 * @param feed - feed on which you want to subscribe
	 * @param properties - this parameter could be used in overloaded method where field "properties" is not available
	 */
	protected void pollingSubscribe(MarketDataFeed feed, Properties properties)
			throws CurrencyConverter.CurrencyConversionException {

		if (isTimerCanceled) {
			timer = new Timer(true);
			isTimerCanceled = false;
		}

		if (properties != null && properties.containsKey("pollingInterval")) {
			period = Integer.parseInt(properties.getProperty("pollingInterval"));
		}

		PollingTask task = null;

		switch (feed.getMarketDataType()){
			case DEALABLE_PRICE:
				task = createQuotesPollingTasks( feed, properties);
				break;
			case TRADES:
				task = createTradesPollingTasks( feed, properties);
				break;
			default:
				throw new UnsupportedMDTypeForExchange("MDType doesn't not have default implementation" + feed.getMarketDataType());
		}

	    if( task != null  ) {
			pollingTasks.put(feed, task);
			timer.scheduleAtFixedRate(task, 0, period);
		}

	}

	protected PollingTask createQuotesPollingTasks(MarketDataFeed feed,Properties properties) throws CurrencyConverter.CurrencyConversionException {
		return new PollingTask(feed, properties,this){
			@Override
			public void run() {

				try {
					long snapshotId = feed.nextSeqId();
					long sourceTimestamp = System.currentTimeMillis();
					publishNewSnapshot(snapshotId, sourceTimestamp, feed);
					int itemCount = connection.getExchange().getXChangeQuantfabricService().getAndPublishOrderBook(snapshotId,feed,sourceTimestamp, adapter::marketDataHandler);
					publishEndUpdate(snapshotId, sourceTimestamp,itemCount);

				} catch (Exception e) {
					getLogger().error(e.getMessage());
				}
			}
		};
	}
	protected PollingTask createTradesPollingTasks(MarketDataFeed feed,Properties properties) throws CurrencyConverter.CurrencyConversionException {
		return new PollingTask( feed, properties,this) {
			long startTime = 0;
			@Override
			public void run() {

				try {

					long snapshotId = feed.nextSeqId();
					long sourceTimestamp = System.currentTimeMillis();
					publishNewSnapshot(snapshotId, sourceTimestamp, feed);
					int itemCount = connection.getExchange().getXChangeQuantfabricService().getAndPublishTrade(snapshotId,feed, startTime,adapter::marketDataHandler);
					publishEndUpdate(snapshotId, sourceTimestamp,itemCount);
					startTime = sourceTimestamp;



				} catch (ExchangeException | NotAvailableFromExchangeException | PublisherException | IOException |
						 CurrencyConverter.CurrencyConversionException e) {
					getLogger().error(e.getMessage());
				}
			}
		};
	}


	public <T> void marketDataHandler(T event) {
		throw new NotYetImplementedForExchangeException();
	}
	public void marketDataHandler(Trades trades, CurrencyPair currency) throws CurrencyConverter.CurrencyConversionException {
		throw new NotYetImplementedForExchangeException();
	}
	public void marketDataHandler(OrderBook orderBook, CurrencyPair currency) throws CurrencyConverter.CurrencyConversionException {
		throw new NotYetImplementedForExchangeException();
	}
	// ------------------------
	public abstract void unsubscribeMarketData(MarketDataFeed feed);
	public abstract void unsubscribeExecution(ExecutionFeed feed);

	public void unsubscribeMarketData(Feed feed) {
		if(feed instanceof ExecutionFeed)
			unsubscribeExecution((ExecutionFeed)feed);
		if(feed instanceof MarketDataFeed)
			unsubscribeMarketData((MarketDataFeed)feed);
	}

	public void unsubscribeMarketData(Collection<Feed> feeds) {
		throw new NotYetImplementedForExchangeException();
	}
	public void unsubscribeMarketData(String symbol) {
		unsubscribeMarketData(getFeedProvider().getMarketDataFeed(symbol));
	}

	protected Timer getTimer() {
		return this.timer;
	}

	
	public String getSourceName() {
		
		return getVenueName();
	}


	protected void addNewOrder(String orderId, TradeOrder tradeOrder, boolean isOrderRejectRequired) {

	}	
	
	public String sendMessage(LimitOrder order, TradeOrder tradeOrder) throws Exception  {
		
		String orderId = null;
		try {
			orderId = connection.getExchange().getTradeService().placeLimitOrder(order);
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

		return null;
	}
	
	public synchronized void cancelOrder(String orderId) {
		
		String orderReference = getMarketOrderReference(orderId);
		
		try {			
			if (orderReference != null)
				connection.getExchange().getTradeService().cancelOrder(orderReference);
			else 
				getLogger().error("Can't cancel order. Order reference is null");
		} catch (ExchangeException | IOException e) {
			getLogger().error(e.getMessage());
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
			throws Publisher.PublisherException, CurrencyConverter.CurrencyConversionException {

		long messageId = Long.parseLong(orderId);
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
	
	protected void confirmOrderFill(String orderId, String orderReference, String instrumentId, double tradePrice, double tradeSize) throws Publisher.PublisherException {

		String sourceName = getSourceName();
		Date sourceTimestamp = new Date();
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;
		long price = getInstrumentProvider().getInstrument(instrumentId).castToLong(tradePrice);

		long messageId = Long.valueOf(orderId);
		OrderExecutionReport orderExecutionReport = new Filled(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId, price, tradeSize);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);
		
		publish(orderExecutionReport);
	}
	
	protected void confirmOrderCancel(String orderId, String orderReference, long timestamp) throws Publisher.PublisherException,
            CurrencyConverter.CurrencyConversionException {
			

		String sourceName = getSourceName();
		Date sourceTimestamp = new Date(timestamp);
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;
		long messageId = Long.valueOf(orderId);

		OrderExecutionReport orderExecutionReport = new Canceled(messageId, sourceName, sourceTimestamp, institutionOrderReference, localOrderReference,
				executionId);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);
		orderExecutionReport.setText(orderId);

		publish(orderExecutionReport);
	}
	
	protected void confirmOrderReject(String orderId, String orderReference, long timestamp)
			throws Publisher.PublisherException, CurrencyConverter.CurrencyConversionException {

		long messageId = 0;
		String sourceName = getSourceName();
		Date sourceTimestamp = new Date(timestamp);
		String orginalOrderReference = null;
		String localOrderReference = orderReference;
		String institutionOrderReference = orderReference;
		String executionId = orderReference;

		OrderExecutionReport orderExecutionReport = new Rejected(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionId, orderId);

		orderExecutionReport.setOriginalLocalOrderReference(orginalOrderReference);
		orderExecutionReport.setDoneTransationTime(sourceTimestamp);

		publish(orderExecutionReport);
	}
	protected void publishNewSnapshot(long snapshotId,long sourceTimestamp,MarketDataFeed feed) throws Publisher.PublisherException {

		publish(new NewSnapshot(snapshotId, getSourceName(),
				sourceTimestamp, -1 ,feed.getInstrument().getSymbol()
				, feed.getFeedId(), feed.getFeedName().getName()));
	}

	protected void publishEndUpdate(long snapshotId,long sourceTimestamp,int itemCount) throws Publisher.PublisherException {

		EndUpdate endUpdate =
				new EndUpdate(snapshotId, MDMessageType.SNAPSHOT,
						getSourceName(), sourceTimestamp, itemCount);

		publish(endUpdate);
	}
}
