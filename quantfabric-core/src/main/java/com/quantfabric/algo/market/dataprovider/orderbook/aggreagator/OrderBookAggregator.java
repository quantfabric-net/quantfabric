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

import java.util.Collection;
import java.util.Iterator;

import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener;
import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.messaging.BasePublisher;
import com.quantfabric.messaging.Subscriber;

public abstract class OrderBookAggregator
	extends BasePublisher<Subscriber<Object>,FeedName,Object>
	implements MarketFeeder, OrderBookSnapshotListener	
{
	public static class OrderBookAggregatorException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5688538862851039987L;
		
		public OrderBookAggregatorException()
		{
			super();
		}
		public OrderBookAggregatorException(String message, Throwable cause)
		{
			super(message, cause);
		}
		public OrderBookAggregatorException(String message)
		{
			super(message);
		}
		public OrderBookAggregatorException(Throwable cause)
		{
			super(cause);
		}
	}
	
	private static int countAggregators = 0;
	private static int getAggregatorNumber()
	{
		return ++countAggregators;
	}
	
	private final String identifier;
	private final int hashCode = getAggregatorNumber();
	
	public OrderBookAggregator(String identifier)
	{
		this.identifier = identifier;
	}
	
	public String getIdentifier()
	{
		return identifier;
	}
		
	@Override
	public void publish(Object data)
			throws PublisherException
	{
		Collection<Subscriber<Object>> subscribers = getSubscribers();
		
		if (subscribers != null)
		{					
			synchronized(subscribers)
			{
				for (Iterator<Subscriber<Object>> subscriber = subscribers.iterator(); 
						subscriber.hasNext();) 
					subscriber.next().sendUpdate(data);
			}				
		}	
	}
	
	@Override
	public int hashCode()
	{
		return hashCode;
	}
}
