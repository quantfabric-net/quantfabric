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
package com.quantfabric.algo.market.dataprovider.orderbook.aggreagator;

import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.messaging.Publisher;

public class NativeOrderBookPublisher
	extends OrderBookAggregator
{
	private OrderBookSnapshot currentOrderBookSnapshot = null;
	
	public NativeOrderBookPublisher(String identifier)
	{
		super(identifier);
	}

	public OrderBookSnapshot getCurrentOrderBookSnapshot()
	{
		return currentOrderBookSnapshot;
	}
	
	@Override
	public void onNewSnapshot(OrderBookSnapshot orderBookSnapshot) throws OrderBookSnapshotListenerException
	{
		currentOrderBookSnapshot = orderBookSnapshot;	
	}

	@Override
	public String getName()
	{
		return getIdentifier();
	}

	@Override
	public void onEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified) 
	{
		try
		{
			if (isBookModified)
				super.publish(getCurrentOrderBookSnapshot());
		}
		catch (Publisher.PublisherException e)
		{
			//throw new OrderBookSnapshotListenerException("can't publish order book snapshot", e);
		}	
	}

	@Override
	public void onNoUpdate(long snapshotId)
	{
		// TODO Auto-generated method stub
		
	}
}
