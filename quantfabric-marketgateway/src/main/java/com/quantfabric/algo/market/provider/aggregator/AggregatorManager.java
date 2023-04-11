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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.dataprovider.FeedReference;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProvider;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDictionary;

public class AggregatorManager
{	
	public static class AggregatorManagerException extends Exception
	{
		private static final long serialVersionUID = 7551468615077510412L;

		public AggregatorManagerException()
		{
			super();
		}

		public AggregatorManagerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public AggregatorManagerException(String message)
		{
			super(message);
		}

		public AggregatorManagerException(Throwable cause)
		{
			super(cause);
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(AggregatorManager.class);
	
	private final Map<FeedReference, List<MarketViewAggregator>> aggregators =
		new HashMap<FeedReference, List<MarketViewAggregator>>();
	
	private final AggregatorFactory aggregatorFactory;
	
	private final MultiTimeFrameHistoryProviderDictionary feedMultiTimeFrameHistoryProvider;
	
	public AggregatorManager()
	{
		this(null);
	}
	
	public AggregatorManager(MultiTimeFrameHistoryProviderDictionary feedHistoryRecorderProvider)
	{
		this(new AggregatorFactory(), feedHistoryRecorderProvider);
	}
	
	public AggregatorManager(AggregatorFactory aggregatorFactory, MultiTimeFrameHistoryProviderDictionary feedHistoryRecorderProvider)
	{
		this.aggregatorFactory = aggregatorFactory;
		this.feedMultiTimeFrameHistoryProvider = feedHistoryRecorderProvider;
	}
	
	public List<MarketViewAggregator> createAggregators(
			FeedReference feedReference, 
			Collection<AggregatorDefinition> aggregatorDefinitions) throws AggregatorManagerException
	{
		List<MarketViewAggregator> aggregators =
			new ArrayList<MarketViewAggregator>();
		
		for (AggregatorDefinition aggregatorDefinition : aggregatorDefinitions)
		{
			if (feedMultiTimeFrameHistoryProvider != null)
			{
				MultiTimeFrameHistoryProvider multiTimeFrameHistoryProvider =
						feedMultiTimeFrameHistoryProvider.getMultiTimeFrameHistoryProvider(feedReference);
				
				if (multiTimeFrameHistoryProvider != null)
				{
					aggregatorDefinition.getProperties().put("multiTimeFrameHistoryProvider", multiTimeFrameHistoryProvider);
				}
				else
				{
					throw new AggregatorManagerException("can't find multiTimeFrameHistoryProvider for feed (" +
							feedReference + ")");
				}
			}
			else
			{
				if (Boolean.parseBoolean(aggregatorDefinition.getProperties().getProperty("isHistoryRecorder", "false")))
					throw new AggregatorManagerException("feedHistoryRecorderProvider not specified but history recording is required");
			}
			
			MarketViewAggregator aggregator = aggregatorFactory.createAggregator(aggregatorDefinition);
			if (aggregator != null)
			{
				aggregators.add(aggregator);
				addAggregator(feedReference, aggregator);
			}
			else
				log.error(String.format(
						"Aggregator of type (%s) is not created via factory (%s)",
                        aggregatorDefinition, aggregatorFactory.getClass().getName()));
		}
		
		return aggregators;
	}
	
	private void addAggregator(FeedReference feedReference, MarketViewAggregator aggregator)
	{
		if (!aggregators.containsKey(feedReference))
			aggregators.put(feedReference, new ArrayList<MarketViewAggregator>());
		
		aggregators.get(feedReference).add(aggregator);	
	}
	
	public List<MarketViewAggregator> getAggregators(FeedReference feedReference)
	{
		return aggregators.get(feedReference);
	}
	
	public void deleteAggregators()
	{
		aggregators.clear();
	}
}
