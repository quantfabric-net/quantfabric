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
package com.quantfabric.algo.market.gate.access.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.quantfabric.algo.market.gate.access.AbstractMarketDataServiceHost;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionOptions;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionResult;
import com.quantfabric.net.rmi.QuantfabricRMIRegistry;

public class RmiMarketDataServiceHost extends AbstractMarketDataServiceHost implements RmiMarketDataService
{	
	private final String serviceHostName;
	private final String serviceName;
	private final int port;
	private final Registry registry;
	
	public RmiMarketDataServiceHost(String serviceHostName, String rmiServiceName, int rmiServicePort) throws RemoteException
	{
		this.serviceHostName = serviceHostName;
		
		this.serviceName = rmiServiceName;
		this.port = rmiServicePort;
		
		this.registry = QuantfabricRMIRegistry.getInstance().getRegistry();
	}
	
	@Override
	public void start() throws RemoteException, AlreadyBoundException
	{
		registry.bind(serviceName, UnicastRemoteObject.exportObject(this, port));
	}
	
	@Override
	public void stop() throws RemoteException, NotBoundException
	{
		UnicastRemoteObject.unexportObject(this, false);
		registry.unbind(serviceName);		
	}
	
	@Override
	public SubscriptionResult subscribe(String productCode,
			ContentType contentType, SubscriptionOptions subscriptionOption)
			throws Exception
	{
		if (!isProductExist(productCode))
			return SubscriptionResult.UNKNOWN_PRODUCT_CODE;
		else
			if (!getProduct(productCode).getAvailableContentTypes().contains(contentType))
				return SubscriptionResult.UNAVAILABLE_CONTENT_TYPE;
			else
				return SubscriptionResult.SUCCESS;		
	}

	@Override
	public String getServiceHostName()
	{
		return serviceHostName;
	}
}
