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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.market.gateway.access.MarketDataService;
import com.quantfabric.algo.market.gateway.access.agent.Agent;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionOptions;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionResult;

public class GatewayAgent extends Agent
{
	private MarketDataService mdService;
	private final Map<String, Product> products = new HashMap<String, Product>();
	private final Set<String> subscriptions = new LinkedHashSet<String>();
	private final String name;
	
	private final GatewayConnector gatewayConnector;
	private final GatewayFeeder gatewayFeeder;
	
	private volatile boolean isStarted = false;
	
	public GatewayAgent(String name, GatewayConnector gatewayConnector, GatewayFeeder gatewayFeeder)
	{
		super();
		this.gatewayConnector = gatewayConnector;
		this.gatewayFeeder = gatewayFeeder;
		this.name = name;
	}
	
	@Override
	public Set<Product> getProducts()
	{
		return Collections.unmodifiableSet(new HashSet<Product>(products.values()));		
	}
	
	@Override
	public boolean isStarted()
	{
		return isStarted;
	}

	@Override
	public synchronized void start() throws GatewayAgentException, RemoteGatewayException
	{		
		mdService = gatewayConnector.connect();
				
		try
		{
			for (Product p : mdService.getProductList())
				products.put(p.getProductCode(), p);
		}
		catch (Exception e)
		{
			throw new GatewayAgentException("Error during products proccessing", e);
		}
		
		isStarted = true;
	}
	
	@Override
	public synchronized void stop() throws RemoteGatewayException
	{
		mdService = null;
		products.clear();		
		
		for (Iterator<String> subscriptionsIterator = subscriptions.iterator(); subscriptionsIterator.hasNext();)
		{
			gatewayFeeder.unsubscribe(subscriptionsIterator.next());
			subscriptionsIterator.remove();
		}
		
		isStarted = false;
	}
		
	@Override
	public String subscribe(String productCode, ContentType contentType, 
			SubscriptionOptions subscriptionOption,
			Subscriber subscriber) throws RemoteGatewayException, GatewayAgentException
	{
		if (!isStarted)
			throw new GatewayAgentException("Gateway agent isn't started.");
		
		if (!products.containsKey(productCode))
			throw new GatewayAgentException("Unknown product (productCode=" + productCode + ").");
		
		SubscriptionResult subscriptionResult;
		try
		{
			subscriptionResult = mdService.subscribe(productCode, contentType, subscriptionOption);
		}
		catch (Exception e)
		{
			throw new GatewayAgentException("Subscription failed.", e);
		}
		
		if (subscriptionResult != SubscriptionResult.SUCCESS)
			throw new GatewayAgentException("Subscription failed - " + subscriptionResult + ".");
		
		Product product = products.get(productCode);
		
		if (!product.getAvailableContentTypes().contains(contentType))
			throw new GatewayAgentException("Unavailable content type (" + 
						"productCode=" + productCode + ", contentType=" + contentType +").");
		
		String subscriptionId;
		subscriptions.add(subscriptionId = gatewayFeeder.subscribe(product.getPublisherAddress(contentType), subscriber));
		
		return subscriptionId;
	}

	@Override
	public void unsubscribe(String subscriptionId) throws GatewayAgentException, RemoteGatewayException
	{
		if (!isStarted)
			throw new GatewayAgentException("Gateway agent isn't started.");
		
		gatewayFeeder.unsubscribe(subscriptionId);
	}
	
	public static GatewayAgent createDefaultGatewayAgent(String agentName, String host, String rmiServiceName)
	{
		return new DefaultGatewayAgent(agentName, host, rmiServiceName);
	}

	@Override
	public String getName() {
		
		return name;
	}
}
