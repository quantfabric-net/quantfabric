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
package com.quantfabric.newpackage;
import java.io.IOException;
import java.util.Properties;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.algo.market.gateway.access.agent.Agent;
import com.quantfabric.algo.market.gateway.access.agent.AgentFactory;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;


public class IncrTestApp
{
	public static void main(String[] args) throws RemoteGatewayException, IOException, GatewayAgentException, InstantiationException, IllegalAccessException, ClassNotFoundException, NotSpecifiedProperty, InterruptedException
	{	
		String host = "127.0.0.1"; // "127.0.0.1";
		String rmiServiceName = "MarketDataService-1"; //"MarketDataService-1";
		String factoryName = "com.quantfabric.algo.market.gateway.access.remote.DefaultGatewayAgentFactory";
		
		Properties settings = new Properties();
		
		settings.put("host", host);
		settings.put("serviceName", rmiServiceName);
		
		AgentFactory factory = (AgentFactory) Class.forName(factoryName).newInstance();
		Agent ga = factory.createAgent("default", settings);
		
		ga.start();
		
		for (Product p : ga.getProducts()) {
			Translator tr = new Translator(p.getDescription());
				
			ga.subscribe(p.getProductCode(), ContentType.FULL_BOOK, null, tr);
			ga.subscribe(p.getProductCode(), ContentType.INCREMENTAL_UPDATE, null, tr);
				
			Thread.sleep(5000);
			tr.subscribe();
		}
		
		
		System.in.read();
		
		System.out.println("Stop GatewayAgent");
		
		ga.stop();
	}
}
