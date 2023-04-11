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

import com.quantfabric.algo.market.datamodel.ComplexMarketView;
import com.quantfabric.algo.market.datamodel.MDFeedEvent;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;

public class ComplexMarketViewAggregator extends BaseMarketViewAggregator
{
		
	private final List<MarketViewAggregatorSupport> aggregatorSupports =
		 new ArrayList<MarketViewAggregatorSupport>();
	
	public ComplexMarketViewAggregator(String name, Properties properties)
	{
		super(name, properties);
		
		TopMDQuoteAggregator topMDQuoteAggregator 
			= new TopMDQuoteAggregator(name + "-TopMDQuoteAggregator", properties);
		VWAPAggregator vwapAggregator
			= new VWAPAggregator(name + "VWAPAggregator", properties);
		OWAPAggregator owapAggregator
			= new OWAPAggregator(name + "OWAPAggregator", properties);

		aggregatorSupports.add(new MarketViewAggregatorSupport(topMDQuoteAggregator));
		aggregatorSupports.add(new MarketViewAggregatorSupport(vwapAggregator));
		aggregatorSupports.add(new MarketViewAggregatorSupport(owapAggregator));
	}

	@Override
	public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
			throws OrderBookSnapshotListenerException
	{
		for (MarketViewAggregatorSupport aggregatorSupport : aggregatorSupports)
			aggregatorSupport.sendNewSnapshot(orderBookSnapshot);
	}

	private ComplexMarketView cmvEvent = null; 
	
	public void setToComplexMakretView(Object aggregation, long updateId)
	{
		if (cmvEvent == null)
			cmvEvent = new ComplexMarketView((MDFeedEvent)aggregation, updateId);
		
		cmvEvent.update(aggregation);
	}
	
	public ComplexMarketView getComplexMakretView()
	{
		return cmvEvent;
	}
	
	private boolean bidUpdated = false;
	private boolean offerUpdated = false;
	private boolean tradeUpdated = false;
	
	@Override
	public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified)
	{
		for (MarketViewAggregatorSupport aggregatorSupport : aggregatorSupports)
		{
			aggregatorSupport.sendEndUpdate(orderBookInfo, updateId, isBookModified);
			
			if (aggregatorSupport.isAggregationObtained())
			{
				if(aggregatorSupport.getAggregation() != null)
				{						
					setToComplexMakretView(aggregatorSupport.getAggregation(), updateId);
				}
				if(aggregatorSupport.getAggregationBatch() != null)
				{
					for (Object aggregation :  aggregatorSupport.getAggregationBatch())
					{
						setToComplexMakretView(aggregation, updateId);
					}
				}
			}
		}
		
		if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK)
			bidUpdated = true;
		else if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK)
			offerUpdated = true;
		else
			tradeUpdated = true;
		
		if (bidUpdated && offerUpdated)
		{
			bidUpdated = false;
			offerUpdated = false;
			
			tradeUpdated = false;
			
			publish(getComplexMakretView());
			cmvEvent = null;
		}
	}

	@Override
	public void processNoUpdate(long snapshotId) {}
}
