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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener.OrderBookSnapshotListenerException;

public abstract class BaseOrderBookCache
	implements	OrderBookInfo, OrderBookCacheReader, OrderBookStorageWriter
{
	private final FeedName monitoredFeedName;
	private final OrderBookTypes orderBookType;
	
	private final Map<String, OrderBookSnapshotListener> orderBookSnapshotListeners =
		new HashMap<String, OrderBookSnapshotListener>();

	private OrderBookSnapshot currentSnapshot = null;
	private boolean bookModified;	
	
	public BaseOrderBookCache(OrderBookTypes orderBookType, FeedName monitoredFeedName)
	{
		this.monitoredFeedName = monitoredFeedName;
		this.orderBookType = orderBookType;
	}
	
	@Override
	public FeedName getFeedName()
	{
		return monitoredFeedName;
	}
	@Override
	public OrderBookTypes getOrderBookType()
	{
		return orderBookType;
	}

	@Override
	public void addOrderBookSnapshotListener(OrderBookSnapshotListener listener)
	{
		orderBookSnapshotListeners.put(listener.getName(), listener);		
	}

	@Override
	public OrderBookSnapshotListener removeOrderBookSnapshotListener(String name)
	{
		return orderBookSnapshotListeners.remove(name);
	}	
	
	protected boolean isBookModified()
	{
		return bookModified;
	}

	protected void setBookModified(boolean bookModified)
	{
		this.bookModified = bookModified;
	}

	
	
	protected abstract OrderBookSnapshot createShapshot(long snapshotId);
	
	@Override
	public OrderBookSnapshot getOrderBookSnapshot()
	{
		return currentSnapshot;
	}
	
	@Override
	public void commit(long snapshotId, long sourceTimestamp) throws OrderBookStorageWriterException
	{
		boolean bookWasModified = isBookModified();
		
		if (bookWasModified)
		{				
			currentSnapshot = createShapshot(snapshotId);
			setBookModified(false);
			publishSnapshotToListeners(currentSnapshot);			
		}
		
		notifyListenersAboutEndUpdate(snapshotId, bookWasModified);
	}

	@Override
	public void commit(EndUpdate endUpdate) throws OrderBookStorageWriterException
	{
		commit(endUpdate.getMessageId(), endUpdate.getSourceTimestamp());		
	}
	
	@Override
	public void noUpdates(EndUpdate endUpdate)
			throws OrderBookStorageWriterException
	{
		notifyListenersAboutNoUpdates(endUpdate.getMessageId());		
	}
	
	protected Collection<OrderBookSnapshotListener> getOrderBookSnapshotListeners()
	{
		return orderBookSnapshotListeners.values();
	}
	
	protected void publishSnapshotToListeners(OrderBookSnapshot currentSnapshot) 
		throws OrderBookStorageWriterException
	{
		for (OrderBookSnapshotListener listener : getOrderBookSnapshotListeners())
		{
			try
			{
				listener.onNewSnapshot(currentSnapshot);
			}
			catch (OrderBookSnapshotListenerException e)
			{
				throw new OrderBookStorageWriterException(
						"can't publish order book snapshot to listener (" + 
						listener.getName(), e);
			}
		}
	}

	protected void notifyListenersAboutEndUpdate(long snapshotId, boolean isBookModified)
	{		
		for (OrderBookSnapshotListener listener : getOrderBookSnapshotListeners())
			listener.onEndUpdate(this, snapshotId, isBookModified);
	}
	
	protected void notifyListenersAboutNoUpdates(long snapshotId)
	{		
		for (OrderBookSnapshotListener listener : getOrderBookSnapshotListeners())
			listener.onNoUpdate(snapshotId);
	}
}
