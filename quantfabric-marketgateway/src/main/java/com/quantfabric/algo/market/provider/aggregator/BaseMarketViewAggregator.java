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
package com.quantfabric.algo.market.provider.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.quantfabric.algo.market.provider.aggregator.event.NoUpdates;
import com.quantfabric.algo.market.provider.aggregator.mdprocessor.MarketDataProcessor;
import com.quantfabric.algo.market.provider.aggregator.mdprocessor.SpreadCorrector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.provider.aggregator.mdprocessor.SlippageFilter;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;

public abstract class BaseMarketViewAggregator implements MarketViewAggregator
{
	private static final Logger logger = LoggerFactory.getLogger(BaseMarketViewAggregator.class);
	protected static Logger getLogger()
	{
		return logger;
	}
	
	private final List<MarketViewAggregatorListener> subscribers = new ArrayList<MarketViewAggregatorListener>();
	private final String name;
	private final Properties properties;

	private final MarketDataProcessor marketDataProcessor;
	
	public BaseMarketViewAggregator(String name, Properties properties)
	{
		this.name = name;
		this.properties = properties;
		
		final long slippageFilter = properties == null ? 0 : Long.parseLong(properties.getProperty("slippageFilter", "0"));
		final int spreadThreshold = properties == null ? 0 : Integer.parseInt(properties.getProperty("spreadThreshold", "0"));
		
		if (slippageFilter != 0)
		{
			this.marketDataProcessor = new SlippageFilter(slippageFilter, this);
					logger.info(String.format("Slippage filter (%d) for aggregator (%s) activated", 
							slippageFilter, this.name));		
		}
		else
		{
			if (spreadThreshold != 0)
			{
				int synthSpread = properties == null ? 0 : Integer.parseInt(properties.getProperty("synthSpread", "0"));
				
				this.marketDataProcessor = new SpreadCorrector(spreadThreshold, synthSpread, this);
				logger.info(String.format("Spread corrector (%d, %d) for aggregator (%s) activated", 
						spreadThreshold, synthSpread, this.name));	
			}
			else
			{			
				this.marketDataProcessor = 
					new MarketDataProcessor() 
					{				
						@Override
						public void processNoUpdate(long snapshotId)
						{
							BaseMarketViewAggregator.this.processNoUpdate(snapshotId);					
						}
						
						@Override
						public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
								throws OrderBookSnapshotListenerException
						{
							BaseMarketViewAggregator.this.processNewSnapshot(orderBookSnapshot);
						}
						
						@Override
						public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
								boolean isBookModified)
						{
							BaseMarketViewAggregator.this.processEndUpdate(orderBookInfo, updateId, isBookModified);					
						}
					};
			}
		}			
	}
	
	@Override
	public Properties getProperties()
	{
		return properties;
	}

	public String getName()
	{
		return name;
	}
	
	public void subscribe(MarketViewAggregatorListener subscriber)
	{
		subscribers.add(subscriber);
	}
	
	public void unSubscribe(MarketViewAggregatorListener subscriber)
	{
		subscribers.remove(subscriber);
	}
	
	public void publish(Object event, boolean forceProcessing)
	{
		for (MarketViewAggregatorListener subscriber : subscribers)
			subscriber.update(this, event, forceProcessing);
	}
	
	public void publish(Object event)
	{
		publish(event, false);
	}
	
	public void publish(Object[] events, boolean forceProcessing)
	{
		for (MarketViewAggregatorListener subscriber : subscribers)
			subscriber.update(this, events, forceProcessing);
	}
	
	public void publish(Object[] events)
	{
		publish(events, false);
	}

	@Override
	public void onNewSnapshot(OrderBookSnapshot orderBookSnapshot)
			throws OrderBookSnapshotListenerException
	{
		marketDataProcessor.processNewSnapshot(orderBookSnapshot);		
	}

	@Override
	public void onEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified)
	{
		marketDataProcessor.processEndUpdate(orderBookInfo, updateId, isBookModified);		
	}

	@Override
	public void onNoUpdate(long snapshotId)
	{
		marketDataProcessor.processNoUpdate(snapshotId);
		publish(new NoUpdates(snapshotId), false);
	}	
	
	
	public abstract void processNewSnapshot(OrderBookSnapshot orderBookSnapshot) 
		throws OrderBookSnapshotListenerException;
	public abstract void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified);
	public abstract void processNoUpdate(long snapshotId);
}
