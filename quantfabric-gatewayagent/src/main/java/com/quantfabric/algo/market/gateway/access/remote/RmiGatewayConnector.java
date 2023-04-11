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

import java.rmi.Remote;
import java.rmi.registry.Registry;

import com.quantfabric.algo.market.gateway.access.MarketDataService;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import com.quantfabric.net.rmi.QuantfabricRMIRegistry;

public class RmiGatewayConnector implements GatewayConnector
{
	private final String host;
	private final String rmiServiceName;
	
	public RmiGatewayConnector(String host, String rmiServiceName)
	{
		this.host = host;
		this.rmiServiceName = rmiServiceName;
	}

	@Override
	public MarketDataService connect() throws RemoteGatewayException
	{		
		Remote remObject;
		try
		{
			Registry registry = QuantfabricRMIRegistry.getInstance(host).getRegistry();
			remObject = registry.lookup(rmiServiceName);					
		}
		catch (Exception e)
		{
			throw new RemoteGatewayException("Connection faled.", e);
		}
		
		if (!(remObject instanceof MarketDataService))
			throw new RemoteGatewayException("Remote service isn't MarketDataService.");
		
		return (MarketDataService)remObject;	
	}
}
