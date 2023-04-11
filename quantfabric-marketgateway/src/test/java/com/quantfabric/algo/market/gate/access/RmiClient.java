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

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.Set;

import com.quantfabric.algo.market.gate.access.product.subscriber.ZMQStreamClient;
import com.quantfabric.algo.market.gateway.access.MarketDataService;
import org.apache.log4j.BasicConfigurator;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.net.rmi.QuantfabricRMIRegistry;

public class RmiClient
{
	public static void main(String[] args) throws Exception
	{

		BasicConfigurator.configure();
		
		Registry registry = QuantfabricRMIRegistry.getInstance(
				"127.0.0.1").getRegistry();
				
		for (String s : registry.list()) //lookup("MarketDataService-1");
			System.out.println(s);
		
		Remote remObject = registry.lookup("MarketDataService-1");
			
		for (Method m : remObject.getClass().getMethods())
			System.out.println(m.getName());
		
		MarketDataService mdService = (MarketDataService)remObject;
		
		Set<Product> products = mdService.getProductList();
		for (Product p : products)
		{
			System.out.println(p.getProductCode());
		
			System.out.println(mdService.subscribe(p.getProductCode(), ContentType.TOP_OF_BOOK, null)); 
		
			new ZMQStreamClient().connect(p.getPublisherAddress(ContentType.TOP_OF_BOOK), new Printer(p.getDescription()));
		}
		
		System.in.read();
	}
}
