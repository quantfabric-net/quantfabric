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
package com.quantfabric.algo.market.dataprovider.orderbook.storage;

import java.util.Collections;
import java.util.List;

import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookView;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookViewBean;
import com.quantfabric.util.Converter;

public class TradeCache
	extends BaseOrderBookCache
	implements OrderBookView
{
	private MDTrade trade =  null;
	
	private long sourceTimestamp;
	
	public TradeCache(OrderBookTypes orderBookType,
                      FeedName monitoredFeedName)
	{
		super(orderBookType, monitoredFeedName);
		clear();
	}

	@Override
	public MDPrice getTop()
	{
		return null;
	}
	
	public MDTrade getTrade() {
		
		return trade;
	}

	@Override
	public List<MDPrice> getAllLevels()
	{
		return Collections.emptyList();
	}
	
	@Override
	public void addTrade(MDTrade mdTrade) {
		
		trade = mdTrade;
		setBookModified(true);
		sourceTimestamp = mdTrade.getSourceTimestamp();
	}

	@Override
	public void addPrice(MDPrice mdPrice)
	{		
	}

	@Override
	public void deletePrice(MDDelete mdDelete)
	{		
	}

	@Override
	public void clear()
	{
		trade = null;
		setBookModified(true);
		sourceTimestamp = System.currentTimeMillis();
	}

	@Override
	protected OrderBookSnapshot createShapshot(long snapshotId)
	{
		return new OrderBookSnapshot(this.clone(), snapshotId);
	}

	@Override
	public String toString()
	{
		return Converter.toString(trade);
	}

	@Override
	public MDPrice getLevel2()
	{
		return null;
	}

	@Override
	public MDPrice getLevel3()
	{
		return null;
	}

	@Override
	public long getSourceTimestamp()
	{
		return sourceTimestamp;
	}

	@Override
	public OrderBookView clone() 
	{
		return new OrderBookViewBean(this);
	}	
}
