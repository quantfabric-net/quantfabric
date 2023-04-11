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
package com.quantfabric.algo.backtesting.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.gateway.MarketConnectionImp;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

public abstract class MarketDataCacheProvider
{
	public static class MarketDataCacheProviderException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2570834812719814454L;

		public MarketDataCacheProviderException()
		{
			super();
		}

		public MarketDataCacheProviderException(String message,
												Throwable cause)
		{
			super(message, cause);
		}

		public MarketDataCacheProviderException(String message)
		{
			super(message);
		}

		public MarketDataCacheProviderException(Throwable cause)
		{
			super(cause);
		}
	}
	
	public static class MarketDataCacheProviderConfigException extends MarketDataCacheProviderException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8146748664306104873L;

		public MarketDataCacheProviderConfigException()
		{
			super();
		}

		public MarketDataCacheProviderConfigException(String message,
													  Throwable cause)
		{
			super(message, cause);
		}

		public MarketDataCacheProviderConfigException(String message)
		{
			super(message);
		}

		public MarketDataCacheProviderConfigException(Throwable cause)
		{
			super(cause);
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(MarketDataCacheProvider.class);
	
	private final MarketConnectionImp connection;
	private final Properties settings;
	
	public MarketDataCacheProvider(MarketConnectionImp connection, Properties settings)
		throws MarketDataCacheProviderException
	{
		this.connection = connection;
		this.settings = settings;
		init();
	}
			
	public static Logger getLogger()
	{
		return log;
	}

	protected MarketConnectionImp getConnection()
	{
		return connection;
	}
	
	protected Properties getSettings()
	{
		return settings;
	}
	
	protected abstract void init() throws MarketDataCacheProviderException;
	
	public abstract int newContext()  throws MarketDataCacheProviderException;
	public abstract void save(MarketDataFeed feed, MDFeedEvent mdItem) throws MarketDataCacheProviderException;
	public abstract void save(MarketDataFeed feed, MDPrice price) throws MarketDataCacheProviderException;
	public abstract void save(MarketDataFeed feed, MDDelete deleteQuote) throws MarketDataCacheProviderException;
	public abstract Collection<MDItem> load(int contextId, MarketDataFeed feed) throws MarketDataCacheProviderException;
	public abstract Collection<MDItem> load(int contextId, MarketDataFeed feed, Date from, Date to) throws MarketDataCacheProviderException;
	public abstract Collection<MDItem> load(int contextId, Collection<MarketDataFeed> feeds) throws MarketDataCacheProviderException;
	public abstract Collection<MDItem> load(int contextId, Collection<MarketDataFeed> feeds, Date from, Date to) throws MarketDataCacheProviderException;
	public abstract Iterator<MDItem> deferredLoad(int contextId, MarketDataFeed feed) throws MarketDataCacheProviderException;
	public abstract Iterator<MDItem> deferredLoad(int contextId, MarketDataFeed feed, Date from, Date to) throws MarketDataCacheProviderException;
	public abstract Iterator<MDItem> deferredLoad(int contextId, Collection<MarketDataFeed> feeds) throws MarketDataCacheProviderException;
	public abstract Iterator<MDItem> deferredLoad(int contextId, Collection<MarketDataFeed> feeds, Date from, Date to) throws MarketDataCacheProviderException;
	
	public void save(FeedName feedName, MDFeedEvent mdItem) throws MarketDataCacheProviderException
	{
		save(connection.getMarketDataFeed(feedName), mdItem);
	}
	public void save(FeedName feedName, MDPrice quote) throws MarketDataCacheProviderException
	{
		save(connection.getMarketDataFeed(feedName), quote);
	}
	public void save(FeedName feedName, MDDelete deleteQuote) throws MarketDataCacheProviderException
	{
		save(connection.getMarketDataFeed(feedName), deleteQuote);
	}
		
	public Collection<MDItem> load(int contextId, FeedName feedName) throws MarketDataCacheProviderException
	{
		return load(contextId, connection.getMarketDataFeed(feedName));
	}
	public Collection<MDItem> load(int contextId, FeedName feedName, Date from, Date to) throws MarketDataCacheProviderException
	{
		return load(contextId, connection.getMarketDataFeed(feedName), from, to);
	}
	public Collection<MDItem> load(int contextId, FeedName[] feedNames) throws MarketDataCacheProviderException
	{		
		return load(contextId, getFeeds(feedNames));
	}
	public Collection<MDItem> load(int contextId, FeedName[] feedNames, Date from, Date to) throws MarketDataCacheProviderException
	{
		return load(contextId, getFeeds(feedNames), from, to);
	}
	
	public Iterator<MDItem> deferredLoad(int contextId, FeedName feedName) throws MarketDataCacheProviderException
	{
		return deferredLoad(contextId, connection.getMarketDataFeed(feedName));
	}
	public Iterator<MDItem> deferredLoad(int contextId, FeedName feedName, Date from, Date to) throws MarketDataCacheProviderException
	{
		return deferredLoad(contextId, connection.getMarketDataFeed(feedName), from, to);
	}
	public Iterator<MDItem> deferredLoad(int contextId, FeedName[] feedNames) throws MarketDataCacheProviderException
	{		
		return deferredLoad(contextId, getFeeds(feedNames));
	}
	public Iterator<MDItem> deferredLoad(int contextId, FeedName[] feedNames, Date from, Date to) throws MarketDataCacheProviderException
	{
		return deferredLoad(contextId, getFeeds(feedNames), from, to);
	}
	
	protected Collection<MarketDataFeed> getFeeds(FeedName[] feedNames)
	{
		Collection<MarketDataFeed> feeds = new ArrayList<>();

		for (FeedName feedName : feedNames) feeds.add(connection.getMarketDataFeed(feedName));
		
		return feeds;
	}
}
