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

import java.rmi.RemoteException;
import java.util.Properties;

import com.quantfabric.algo.market.gate.access.MarketDataServiceHost;
import com.quantfabric.algo.market.gate.access.MarketDataServiceHostFactory;
import com.quantfabric.util.PropertiesViewer;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;

public class RmiMarketDataServiceHostFactory extends MarketDataServiceHostFactory
{
	public static final String RMI_SERVICE_NAME_PROPERTY = "rmiServiceName";
	public static final String RMI_SERVICE_PORT_PROPERTY = "rmiServicePort";
	
	@Override
	public MarketDataServiceHost createMarketDataServiceHost(String serviceHostName, Properties settings) 
			throws MarketDataServiceHostFactoryException
	{
		String rmiServiceName;
		try
		{
			rmiServiceName = PropertiesViewer.getProperty(settings, RMI_SERVICE_NAME_PROPERTY);
		}
		catch (NotSpecifiedProperty e)
		{
			throw new MarketDataServiceHostFactoryException("Setting \"" + RMI_SERVICE_NAME_PROPERTY +"\" is required.", e);
		}
		
		int rmiServicePort;
		try
		{
			rmiServicePort = Integer.parseInt(PropertiesViewer.getProperty(settings, RMI_SERVICE_PORT_PROPERTY));
		}
		catch (NotSpecifiedProperty e)
		{
			throw new MarketDataServiceHostFactoryException("Setting \"" + RMI_SERVICE_PORT_PROPERTY +"\" is required.", e);
		}
		
		try
		{
			return new RmiMarketDataServiceHost(serviceHostName, rmiServiceName, rmiServicePort);
		}
		catch (RemoteException e)
		{
			throw new MarketDataServiceHostFactoryException("Can't create RmiMarketDataServiceHost", e);
		}
	}

}
