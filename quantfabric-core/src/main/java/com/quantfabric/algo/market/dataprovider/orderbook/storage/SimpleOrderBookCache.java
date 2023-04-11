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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookView;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookViewBean;
import com.quantfabric.util.Converter;

public class SimpleOrderBookCache
	extends BaseOrderBookCache
	implements OrderBookView
{
	private final ConcurrentHashMap<String, MDPrice> book =
		new ConcurrentHashMap<>();
	
	private final Map<Long, String> uniqPrices =
		new ConcurrentHashMap<>();

	private MDTrade trade =  null;
	
	private long sourceTimestamp;
	
	public SimpleOrderBookCache(OrderBookTypes orderBookType, FeedName monitoredFeedName)
	{
		super(orderBookType, monitoredFeedName);
		clear();
	}
	
	@Override
	public void addTrade(MDTrade mdTrade) {
		
		trade = mdTrade;
		setBookModified(true);
		sourceTimestamp = mdTrade.getSourceTimestamp();
	}
	
	@Override
	public MDTrade getTrade() {
		
		return trade;
	}
	
	@Override
	public void addPrice(MDPrice mdPrice)
	{		
		//System.out.println(getOrderBookType() + " <- " + mdPrice.getMdItemType());
		if (uniqPrices.containsKey(mdPrice.getPrice()))
			book.remove(uniqPrices.get(mdPrice.getPrice()));
			
		book.put(mdPrice.getMdItemId(), mdPrice);
		uniqPrices.put(mdPrice.getPrice(), mdPrice.getMdItemId());
		
		setBookModified(true);
		//if (mdPrice.getSourceTimestamp() > sourceTimestamp)
			sourceTimestamp = mdPrice.getSourceTimestamp();
	}
	
	@Override
	public void deletePrice(MDDelete mdDelete)
	{
		//System.out.println(getOrderBookType() + " <- X " + mdDelete.getMdItemType());
		
		MDPrice removedPrice = book.remove(mdDelete.getMdItemId());
		
		if (removedPrice != null)
			uniqPrices.remove(removedPrice.getPrice());
		
		setBookModified(true);
		//if (mdDelete.getSourceTimestamp() > sourceTimestamp)
			sourceTimestamp = mdDelete.getSourceTimestamp();
	}
		
	@Override
	public void clear()
	{
		book.clear();
		uniqPrices.clear();
		trade = null;
		setBookModified(true);
		sourceTimestamp = System.currentTimeMillis();
	}

	@Override
	public MDPrice getTop()
	{	
		List<MDPrice> prices = getAllLevels();
		if (prices != null && prices.size() > 0)
			return prices.get(0);
		
		return null;
	}
	
	private final Comparator<MDPrice> priceComparator =  new Comparator<MDPrice>()
	{
		public int compare(MDPrice o1, MDPrice o2)
		{
			if (getOrderBookType() == OrderBookTypes.BID_BOOK)
				return Long.compare(o2.getPrice(), o1.getPrice());
			else
				return Long.compare(o1.getPrice(), o2.getPrice());
		}	
	};
	
		
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
	public List<MDPrice> getAllLevels()
	{
		if (!book.isEmpty())
		{
			List<MDPrice> sortedBook = new ArrayList<MDPrice>(book.values()); 
			Collections.sort(sortedBook, priceComparator);
			
			int i = 0;
			for (MDPrice price : sortedBook)
				price.setDepthLevel(i++);
			
			return sortedBook;
		}
		
		return new ArrayList<MDPrice>();
	}
		
	@Override
	public long getSourceTimestamp()
	{
		return sourceTimestamp;
	}

	@Override
	protected OrderBookSnapshot createShapshot(long snapshotId)
	{
		return new OrderBookSnapshot(this.clone(), snapshotId);
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
	public OrderBookView clone() 
	{
		return new OrderBookViewBean(this);
	}
}
