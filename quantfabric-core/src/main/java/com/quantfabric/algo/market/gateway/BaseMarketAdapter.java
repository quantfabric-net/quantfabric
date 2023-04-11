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
package com.quantfabric.algo.market.gateway;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider.MarketDataCacheProviderException;
import com.quantfabric.algo.commands.Command;
import com.quantfabric.algo.commands.CommandFactory;
import com.quantfabric.algo.commands.CommandFactory.CommandFactoryException;
import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.datamodel.LatencyOffset;
import com.quantfabric.algo.market.datamodel.MDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDMessageInfo;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookProcessor;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookProcessorPair;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.BaseOrderBookCache;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.OrderBookCacheReaderPair;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.SimpleOrderBookCache;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.TopGatewayOderBookCache;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.TradeProcessorCacheReaderPair;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.TradeCache;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.FeedProvider;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed.MarketDataType;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.messaging.BasePublisher;
import com.quantfabric.messaging.Subscriber;


public abstract class BaseMarketAdapter extends BasePublisher<Subscriber<Object>,FeedName,Object>
										implements MarketAdapter {

	private static final Logger log = LoggerFactory.getLogger(MarketAdapter.class);
	private long marketLatencyOffset = 0L;	
	private MarketDataCacheProvider marketDataCacheProvider = null;
	private final CommandFactory commandFactory;
	private final List<LogonListener> logonListeners = new LinkedList<LogonListener>();
	private FeedProvider feedProvider = null;
	private InstrumentProvider instrumentProvider = null;
	
	private final Map<FeedName, OrderBookProcessorPair> orderBookProcessorPairs =	new HashMap<FeedName, OrderBookProcessorPair>();
	
	private final Map<FeedName, OrderBookCacheReaderPair> orderBookCacheReaders = new HashMap<FeedName, OrderBookCacheReaderPair>();
	
	private final Map<FeedName, TradeProcessorCacheReaderPair> tradeProcessorCacheReaderPairs = new HashMap<FeedName, TradeProcessorCacheReaderPair>();
	
	public BaseMarketAdapter(CommandFactory commandFactory)
	{
		super();
		this.commandFactory = commandFactory;
	}
	
	private TradeProcessorCacheReaderPair getTradeProcessorCacheReaderPair(FeedName feedName, boolean createIfNotExists) {
		
		if (createIfNotExists && !tradeProcessorCacheReaderPairs.containsKey(feedName))
			try {
				createTradeProcessors(feedName);
			}
			catch (MarketAdapterException e) {
				getLogger().error("Can't create trade processors", e);	
			}
		
		return tradeProcessorCacheReaderPairs.get(feedName);
	}
	
	private void createTradeProcessors(FeedName feedName) throws MarketAdapterException
	{
		if (getFeedProvider() == null)
			throw new MarketAdapterException(
				String.format("Feed provider not specified for market adapter (%s)", 
					getIdentifier()));
				
		BaseOrderBookCache tradeCache;
		
		MarketDataFeed feed = getFeedProvider().getMarketDataFeed(feedName);
		
		if (feed == null)
			throw new MarketAdapterException(
				String.format("Can't resolve market data feed by feed name (%s)", 
						feedName.getName()));
		
		tradeCache = getTradeCache(OrderBookTypes.TRADE, feed);
		OrderBookProcessor tradeProcessor = new OrderBookProcessor(this, tradeCache);
		
		tradeProcessorCacheReaderPairs.put(feedName, new TradeProcessorCacheReaderPair(tradeCache, tradeProcessor));
		
		try
		{
			tradeProcessor.start();
		}
		catch (Exception e)
		{
			getLogger().error("can't start trade processors", e);
		}
		
	}
		
	private OrderBookCacheReaderPair getOrderBookCacheReaderPair(FeedName feedName,
																 boolean createIfNotExists)
	{
		if (createIfNotExists && !orderBookProcessorPairs.containsKey(feedName))
			try
			{
				createOrderBookProcessors(feedName);
			}
			catch (MarketAdapterException e)
			{
				getLogger().error("Can't create order book processors", e);
			}
		
		return orderBookCacheReaders.get(feedName);
	}
	
	private void createOrderBookProcessors(FeedName feedName) throws MarketAdapterException
	{
		if (getFeedProvider() == null)
			throw new MarketAdapterException(
				String.format("Feed provider not specified for market adapter (%s)", 
					getIdentifier()));
				
		BaseOrderBookCache bidCache;
		BaseOrderBookCache offerCache;
		
		MarketDataFeed feed = getFeedProvider().getMarketDataFeed(feedName);
		
		if (feed == null)
			throw new MarketAdapterException(
				String.format("Can't resolve market data feed by feed name (%s)", 
						feedName.getName()));
		
		bidCache = getOrderBookCache(OrderBookTypes.BID_BOOK, feed);
		offerCache = getOrderBookCache(OrderBookTypes.OFFER_BOOK, feed);
						
		OrderBookProcessor bidProcessor = new OrderBookProcessor(this, bidCache);
		OrderBookProcessor offerProcessor = new OrderBookProcessor(this, offerCache);
		
		orderBookProcessorPairs.put(feedName, 
				new OrderBookProcessorPair(bidProcessor, offerProcessor));
		
		orderBookCacheReaders.put(feedName,
				new OrderBookCacheReaderPair(bidCache, offerCache));
		
		try
		{
			bidProcessor.start();
			offerProcessor.start();
		}
		catch (Exception e)
		{
			getLogger().error("can't start oder book processors", e);
		}
		
	}
	
	protected BaseOrderBookCache getOrderBookCache(
			OrderBookTypes orderBookType,
			MarketDataFeed feed) throws MarketAdapterException
	{		
		
		if (feed.getMarketDepth() == MarketDataFeed.TOP_MARKET_DEPTH)
			return new TopGatewayOderBookCache(orderBookType, feed.getFeedName());
		else
			return new SimpleOrderBookCache(orderBookType, feed.getFeedName());
	}
	
	protected BaseOrderBookCache getTradeCache(OrderBookTypes orderBookType, MarketDataFeed feed) throws MarketAdapterException {
		
		if (feed.getMarketDataType() == MarketDataType.PRICES_AND_TRADES || feed.getMarketDataType() == MarketDataType.TRADES)
			return new TradeCache(orderBookType, feed.getFeedName());
		else 
			return null;
	}

	@Override
	public void addOrderBookSnapshotListener(FeedName feedName,
			OrderBookSnapshotListener listener)
	{
		if (feedName != null) {

			OrderBookCacheReaderPair readerPair = getOrderBookCacheReaderPair(feedName, true);
			if (readerPair != null) {
				readerPair.getBidOrderBookCacheReader().addOrderBookSnapshotListener(listener);
				readerPair.getOfferOrderBookCacheReader().addOrderBookSnapshotListener(listener);
			} else
				getLogger().error(String.format("Can't get orderbook cache readers for feed (%s)", feedName.getName()));

			if (getFeedProvider().getMarketDataFeed(feedName).getMarketDataType() == MarketDataType.PRICES_AND_TRADES
					|| getFeedProvider().getMarketDataFeed(feedName).getMarketDataType() == MarketDataType.TRADES) {
				TradeProcessorCacheReaderPair tradeReaderPair = getTradeProcessorCacheReaderPair(feedName, true);

				if (tradeReaderPair != null) {
					tradeReaderPair.getTradeCacheReader().addOrderBookSnapshotListener(listener);
				} else
					getLogger().error(String.format("Can't get trade cache readers for feed (%s)", feedName.getName()));
			}

		}
		else
			getLogger().error("Can't add order book listener, feedName is null.");
		
	}

	@Override
	public void removeOrderBookSnapshotListener(FeedName feedName,
			String listenerName)
	{
		OrderBookCacheReaderPair readerPair =
			getOrderBookCacheReaderPair(feedName, false);
		
		if (readerPair != null)
		{
			readerPair.getBidOrderBookCacheReader().
				removeOrderBookSnapshotListener(listenerName);
			
			readerPair.getOfferOrderBookCacheReader().
				removeOrderBookSnapshotListener(listenerName);
		}
		else
			getLogger().error(
					String.format(
						"Can't remove order book listener, order book reader is not exists for feed.",
						feedName.getName()));
		
		TradeProcessorCacheReaderPair tradeReaderPair =
				getTradeProcessorCacheReaderPair(feedName, false);
		
		if (tradeReaderPair != null) {
			tradeReaderPair.getTradeCacheReader().removeOrderBookSnapshotListener(listenerName);
		}
		else
			getLogger().error(
				String.format("Can't remove trade cache readers, trade cache reader doesn't exist for feed (%s)",
						feedName.getName()));
	}	
	
	protected static Logger getLogger()
	{
		return log;
	}
		
	protected long getMarketLatencyOffset()
	{
		return marketLatencyOffset;
	}

	protected void setMarketLatencyOffset(long marketLatencyOffset) throws PublisherException
	{
		this.marketLatencyOffset = marketLatencyOffset;
		publish(new LatencyOffset(getMarketLatencyOffset()));
	}
	
	@Override
	public void setMarketDataCacheProvider(
			MarketDataCacheProvider marketDataCacheProvider)
	{
		this.marketDataCacheProvider = marketDataCacheProvider;
	}

	@Override
	public void setFeedProvider(FeedProvider feedProvider)
	{
		this.feedProvider = feedProvider;
	}

	protected FeedProvider getFeedProvider()
	{
		return feedProvider;
	}
	
	@Override
	public void setInstrumentProvider(InstrumentProvider instrumentProvider)
	{
		this.instrumentProvider = instrumentProvider;		
	}
	
	public InstrumentProvider getInstrumentProvider()
	{
		return instrumentProvider;
	}	

	@Override
	public void addLogonListerner(LogonListener logonListener)
	{
		logonListeners.add(logonListener);		
	}

	@Override
	public void removeLogonListerner(LogonListener logonListener)
	{
		logonListeners.remove(logonListener);		
	}
	
	protected synchronized void invokeLogonListenersByLoggedIn()
	{
		if (marketDataCacheProvider != null)
			try
			{
				marketDataCacheProvider.newContext();
			}
			catch (MarketDataCacheProviderException e)
			{
				log.error("can't create new Context in MarketDataCache", e);
			}
		
		for (LogonListener logonListener : logonListeners)
			logonListener.loggedin(this);
	}
	
	protected synchronized void invokeLogonListenersByLogout()
	{
		for (LogonListener logonListener : logonListeners)
			logonListener.logout(this);
	}
	
	@Override
	public void execute(Command command)
	{
		try
		{			
			commandFactory.create(command).execute(this);
		}
		catch (CommandFactoryException e)
		{		
			getLogger().error("Command executing error", e);
		}
	}
	
	private long calcMessageLatency(MDMessageInfo mdMessageInfo)
	{
		return mdMessageInfo.getTimestamp() - 
			mdMessageInfo.getSourceTimestamp() - getMarketLatencyOffset();
	}

	@Override
	public void publish(Object data) throws PublisherException 
	{
		FeedName feedName = null;
		MarketDataFeed mdFeed = null;
		
		if (data instanceof MDMessageInfo)
		{
			MDMessageInfo mdMessageInfo = (MDMessageInfo)data;
			mdMessageInfo.setMessageLatency(calcMessageLatency(mdMessageInfo));
		}
		if (data instanceof MDFeedEvent)
		{		
			MDFeedEvent feedEvent = (MDFeedEvent)data; 
						
			if (feedEvent.getFeedId() != Feed.FEED_ID_NOT_SET)
				mdFeed = feedProvider.getMarketDataFeed(feedEvent.getFeedId());
			else
				mdFeed = feedProvider.getMarketDataFeed(feedEvent.getSymbol());				
			
			if (mdFeed != null)
			{
				feedName = mdFeed.getFeedName();
				feedEvent.setFeedName(feedName.getName());
				feedEvent.setFeedGroupId(mdFeed.getFeedGroupId());
				feedEvent.setInstrumentId(mdFeed.getInstrumentId());
				feedEvent.setPointsInOne(mdFeed.getInstrument().getPointsInOne());
			}
		}
		if (data instanceof MDItem)
		{		
			MDFeedEvent mdItem = (MDFeedEvent)data;
			
			if (mdFeed != null)
			{				
				if (mdFeed.isSaveData() && marketDataCacheProvider != null)
					try
					{
						marketDataCacheProvider.save(feedName, mdItem);
					}
					catch (MarketDataCacheProviderException e)
					{
						throw new PublisherException(e);
					}
			}
		}
		if (data instanceof OrderExecutionReport)
		{
			ExecutionFeed exFeed = feedProvider.getExecutionFeed(new FeedNameImpl("exec_reports"));
			if (exFeed != null)
				feedName = exFeed.getFeedName();
		}
			
		Collection<Subscriber<Object>> subscribers = null;
		
		if (feedName != null)
			subscribers = getSubscribers(feedName);
		else
			subscribers = getSubscribers();
		
		if (subscribers != null)
		{					
			synchronized(subscribers)
			{
				subscribers.parallelStream().forEach(s -> s.sendUpdate(data));
			}				
		}
	}

	@Override
	public boolean isReadyToExecution()
	{
		return getStatus() == AdapterStatus.CONNECTED;
	}
}
