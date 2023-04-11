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
package com.quantfabric.algo.trading.strategy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.quantfabric.algo.market.datamodel.ComplexMarketView;
import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.trading.execution.ExecutionProvider;

public class PereodicMarketUpdatesDataStreamPoster implements MarketUpdatesDataStreamPoster
{
	private final ExecutionProvider executionProvider;
	private final long periodToPost;
	
	private final Map<String, ComplexMarketView> marketViews = new ConcurrentHashMap<String, ComplexMarketView>();
	private final Map<String, MDOrderBook> orderBooks = new ConcurrentHashMap<String, MDOrderBook>();
	
	private final Set<String> updatedMarketViews = new HashSet<String>();
	private final Set<String> updatedOrderBooks = new HashSet<String>();
	
	public PereodicMarketUpdatesDataStreamPoster(ExecutionProvider executionProvider, long periodToPost)
	{
		this.executionProvider = executionProvider;
		this.periodToPost = periodToPost;
		
		Timer pereodicPoser = new Timer(true);
		pereodicPoser.schedule(
			new TimerTask()
			{			
				@Override
				public void run()
				{
					synchronized (updatedMarketViews)
					{
						for (String feedName : updatedMarketViews)
						{
							ComplexMarketView marketView = marketViews.get(feedName);
							if (marketView != null)
								PereodicMarketUpdatesDataStreamPoster.this.executionProvider.
									sendToStrategyDataStream(marketView);
						}
						updatedMarketViews.clear();
					}
					synchronized (updatedOrderBooks)
					{					
						for (String feedName : updatedOrderBooks)
						{
							MDOrderBook orderBook = orderBooks.get(feedName);
							if (orderBook != null)
								PereodicMarketUpdatesDataStreamPoster.this.executionProvider.
									sendToStrategyDataStream(orderBook);
						}
						updatedOrderBooks.clear();
					}				
				}
			}, periodToPost, periodToPost);
	}
	
	@Override
	public void post(ComplexMarketView marketView)
	{
		marketViews.put(marketView.getFeedName(), marketView);
		synchronized (updatedMarketViews)
		{
			updatedMarketViews.add(marketView.getFeedName());
		}
		
	}

	@Override
	public void post(MDOrderBook orderBook)
	{
		orderBooks.put(orderBook.getFeedName(), orderBook);
		synchronized (updatedOrderBooks)
		{
			updatedOrderBooks.add(orderBook.getFeedName());
		}		
	}

	public long getPeriodToPost()
	{
		return periodToPost;
	}
}
