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

import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener.OrderBookSnapshotListenerException;

public class MarketViewAggregatorSupport {

	MarketViewAggregator aggregator;
	
	boolean aggregationObtained = false;
	
	Object aggregation = null;
	Object[] aggregationBatch = null;
	
	public MarketViewAggregatorListener subscriber = new MarketViewAggregatorListener()
	{
		@Override
		public void update(MarketViewAggregator source, Object data, boolean forceProcessing)
		{
			aggregation = data;		
			aggregationObtained = true;
		}

		@Override
		public void update(MarketViewAggregator source, Object[] data, boolean forceProcessing)
		{
			aggregationBatch = data;	
			aggregationObtained = true;
		}
	};
	
	public MarketViewAggregatorSupport(MarketViewAggregator aggregator)
	{
		this.aggregator = aggregator;
		this.aggregator.subscribe(subscriber);
	}
					
	public void sendNewSnapshot(OrderBookSnapshot orderBookSnapshot)
		throws OrderBookSnapshotListenerException
	{			
		this.aggregationObtained = false;
		this.aggregation = null;
		this.aggregationBatch = null;
		
		this.aggregator.onNewSnapshot(orderBookSnapshot);
	}
	
	public void sendEndUpdate(OrderBookInfo orderBookInfo, long updateId, boolean isBookModified)
	{			
		this.aggregator.onEndUpdate(orderBookInfo, updateId, isBookModified);
	}
	
	public boolean isAggregationObtained()
	{
		return aggregationObtained;
	}

	public Object getAggregation()
	{
		return aggregation;
	}

	public Object[] getAggregationBatch()
	{
		return aggregationBatch;
	}
}
