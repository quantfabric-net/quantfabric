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
package com.quantfabric.algo.market.dataprovider.orderbook;

import java.util.ArrayList;
import java.util.List;

import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.util.Converter;

public class OrderBookViewBean implements OrderBookView
{
	private List<MDPrice> allLevels;
	private FeedName feedName;
	private OrderBookTypes orderBookType;
	private long sourceTimestamp;
	private MDTrade trade;

	public OrderBookViewBean()
	{		
	}
	
	public OrderBookViewBean(OrderBookView orderBookView)
	{
		List<MDPrice> levels = orderBookView.getAllLevels();		
		this.allLevels = new ArrayList<MDPrice>(levels.size());		
		for (MDPrice sourcePrice : levels)
			this.allLevels.add(sourcePrice.clone());
		
		this.trade = orderBookView.getTrade();
		this.feedName = orderBookView.getFeedName();
		this.orderBookType = orderBookView.getOrderBookType();
		this.sourceTimestamp = orderBookView.getSourceTimestamp();
	}
	
	@Override
	public FeedName getFeedName()
	{
		return feedName;
	}

	public void setFeedName(FeedName feedName)
	{
		this.feedName = feedName;
	}
	
	@Override
	public OrderBookTypes getOrderBookType()
	{
		return orderBookType;
	}

	public void setOrderBookType(OrderBookTypes orderBookType)
	{
		this.orderBookType = orderBookType;
	}
	
	@Override
	public MDPrice getTop()
	{	
		List<MDPrice> prices = getAllLevels();
		if (prices != null && prices.size() > 0)
			return prices.get(0);
		
		return null;
	}

	@Override
	public MDPrice getLevel2()
	{
		List<MDPrice> prices = getAllLevels();
		if (prices != null && prices.size() > 1)
			return prices.get(1);
		
		return null;
	}

	@Override
	public MDPrice getLevel3()
	{
		List<MDPrice> prices = getAllLevels();
		if (prices != null && prices.size() > 2)
			return prices.get(2);
		
		return null;
	}

	@Override
	public MDTrade getTrade() {
		
		return this.trade;
	}

	@Override
	public List<MDPrice> getAllLevels()
	{
		return allLevels;
	}
	
	public void setAllLevels(List<MDPrice> allLevels)
	{
		this.allLevels = allLevels;
	}
	
	@Override
	public String toString()
	{
		StringBuffer ret = new StringBuffer();
		for (MDPrice price : getAllLevels())
			ret.append(Converter.toString(price) + "\n");
		return ret.toString();
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
