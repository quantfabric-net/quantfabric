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
package com.quantfabric.algo.market.gate.access;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Set;

import com.quantfabric.algo.market.gate.access.rmi.RmiMarketDataService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionResult;
import com.quantfabric.net.rmi.QuantfabricRMIRegistry;

public class RmiMarketDataServiceTest 
{	
	private static SimpleMarketDataService mdService;

	@BeforeClass
	public static void startService() throws RemoteException, AlreadyBoundException
	{
		mdService = new SimpleMarketDataService();
		mdService.start();
	}
	
	@AfterClass
	public static void stopService() throws NotBoundException, RemoteException
	{
		mdService.stop();
	}
	
	@Test
	public void getProductList() throws Exception
	{
		Registry registry = QuantfabricRMIRegistry.getInstance("localhost").getRegistry();						
		RmiMarketDataService mdService = (RmiMarketDataService)registry.lookup("MarketDataService-1");
		
		Set<Product> products = mdService.getProductList();		
		Assert.assertEquals(1, products.size());
		
		SubscriptionResult result = mdService.subscribe("p1", ContentType.FULL_BOOK, null);		
		Assert.assertEquals(SubscriptionResult.SUCCESS, result);
	}
}
