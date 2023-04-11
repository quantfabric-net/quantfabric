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

import java.io.IOException;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.quantfabric.algo.market.gate.access.rmi.RmiMarketDataServiceHost;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Description;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;

public class SimpleMarketDataService implements Serializable
{
	private static final long serialVersionUID = 4350811688947582669L;
	private static RmiMarketDataServiceHost mdService;
	
	public SimpleMarketDataService() throws RemoteException
	{
		mdService = new RmiMarketDataServiceHost("Host1", "MarketDataService-1", 6001);
		
		mdService.addProduct(new Product() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1921990256798187924L;

			@Override
			public String getProductCode()
			{
				return "p1";
			}
			
			@Override
			public Description getDescription()
			{
				return null;
			}

			@Override
			public Set<ContentType> getAvailableContentTypes()
			{
				return new HashSet<ContentType>(Arrays.asList(ContentType.values()));
			}

			@Override
			public PublisherAddress getPublisherAddress(
					ContentType contentType)
			{
				return null;
			}
		});
	}
	
	public void start() throws RemoteException, AlreadyBoundException
	{
		mdService.start();
	}
	
	public void stop() throws RemoteException, NotBoundException
	{
		mdService.stop();
	}
	
	public static void main(String[] args) throws AlreadyBoundException, IOException, NotBoundException
	{
		SimpleMarketDataService service = new SimpleMarketDataService();		
		service.start();
		
		System.in.read();
		
		service.stop();
	}
}
