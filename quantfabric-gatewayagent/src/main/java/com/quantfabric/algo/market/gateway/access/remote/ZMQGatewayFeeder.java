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
package com.quantfabric.algo.market.gateway.access.remote;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.quantfabric.algo.market.gateway.access.product.Connector;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.algo.market.gate.access.product.subscriber.ZMQStreamClient;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;

public class ZMQGatewayFeeder implements GatewayFeeder
{
	private static final AtomicInteger subscriptionCounter = new AtomicInteger(0);
	
	Map<String, Connector> subscription = new HashMap<String, Connector>();
	
	@Override
	public String subscribe(PublisherAddress publisherAddress, Subscriber subscriber)
			throws RemoteGatewayException
	{
		ZMQStreamClient client = new ZMQStreamClient();
		
		try
		{
			client.connect(publisherAddress, subscriber);
		}
		catch (Exception e)
		{
			throw new RemoteGatewayException(
					"Can't establish connection to product publisher.", e);
		}
		
		String subscriptionId = String.valueOf(subscriptionCounter.incrementAndGet());
		
		subscription.put(subscriptionId, client);
		
		return subscriptionId;
	}

	@Override
	public void unsubscribe(String subscriptionId) throws RemoteGatewayException
	{
		if (!subscription.containsKey(subscriptionId)) 
			throw new RemoteGatewayException("Unknown subscription [" + subscriptionId +"]");
			
		try
		{
			subscription.get(subscriptionId).close();
			subscription.remove(subscriptionId);
		}
		catch (Exception e)
		{
			throw new RemoteGatewayException(
					"Error during closing connection to product publisher.", e);
		}		
	}
}
