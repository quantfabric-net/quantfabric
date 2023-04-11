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

import java.util.Properties;

import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.util.CollectionHelper;

public class OrderBookAggregator extends BaseMarketViewAggregator
{
	private boolean bidEndUpdate = false;
	private boolean offerEndUpdate = false;
	
	private final MDOrderBook currentOrderBook = new MDOrderBook();
	private MDOrderBook lastOrderBook;
	
	private boolean orderBookIsPrePopulated = false;
	
	public OrderBookAggregator(String name, Properties properties)
	{
		super(name, properties);
	}
		
	public MDOrderBook getMDOrderBook()
	{
		return lastOrderBook;
	}


	@Override
	public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
			throws OrderBookSnapshotListenerException
	{		
		if (!orderBookIsPrePopulated)
		{
			MDPrice top = orderBookSnapshot.getOrderBookView().getTop();
		
			if (top != null)
			{
				currentOrderBook.pupulate(top);
				orderBookIsPrePopulated = true;
			}
			else
				return;
		}
		
		if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.BID_BOOK)
		{
			currentOrderBook.setBids(orderBookSnapshot.getOrderBookView());
			currentOrderBook.setSourceTimestamp(orderBookSnapshot.getOrderBookView().getSourceTimestamp());
			return;
		}
		
		if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.OFFER_BOOK)
		{
			currentOrderBook.setOffers(orderBookSnapshot.getOrderBookView());
			currentOrderBook.setSourceTimestamp(orderBookSnapshot.getOrderBookView().getSourceTimestamp());
        }
	}

	@Override
	public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified)
	{		
		if (!orderBookIsPrePopulated)
			return;
		
		if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK)
			bidEndUpdate = true;
		else
			if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK)
				offerEndUpdate = true;
		
		if (bidEndUpdate && offerEndUpdate)
		{
			bidEndUpdate = false;
			offerEndUpdate = false;
			
			currentOrderBook.setSnapshotId(updateId);
			
			lastOrderBook = currentOrderBook.clone();
			
			if (getProperties().containsKey("depth"))
			{
				int depth = Integer.parseInt(getProperties().getProperty("depth"));
				CollectionHelper.shrinkList(lastOrderBook.getBids().getAllLevels(), depth);
				CollectionHelper.shrinkList(lastOrderBook.getOffers().getAllLevels(), depth);
			}
			
			publish(lastOrderBook);
		}
	}
	
	@Override
	public void processNoUpdate(long snapshotId)
	{
		
	}

}
