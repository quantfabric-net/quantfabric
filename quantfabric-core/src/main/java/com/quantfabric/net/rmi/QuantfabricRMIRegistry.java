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
package com.quantfabric.net.rmi;

import java.net.URL;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.slf4j.LoggerFactory;

public class QuantfabricRMIRegistry
{
	public static int DEFAULT_RMI_REGISTRY_PORT = 6000;	
	private static volatile QuantfabricRMIRegistry instance = null;
	
	private Registry registry = null;
	
	private QuantfabricRMIRegistry() throws RemoteException
	{			
		URL pathToPolicy = ClassLoader.getSystemResource("client.policy");
		if (pathToPolicy != null)
		{			
			System.setProperty("java.security.policy", pathToPolicy.getPath());
			System.setSecurityManager(new RMISecurityManager());
		}
		else
			LoggerFactory.getLogger(QuantfabricRMIRegistry.class).warn("File \"client.policy\" is absent. Will running with default Security Manager");
		
		String strPort = System.getProperty("com.quantfabric.net.rmi.registry-port");
		this.registry = LocateRegistry.createRegistry(strPort == null ? DEFAULT_RMI_REGISTRY_PORT : Integer.parseInt(strPort));
	}
	
	private QuantfabricRMIRegistry(String host) throws RemoteException
	{
		this(host, DEFAULT_RMI_REGISTRY_PORT);
	}
	
	private QuantfabricRMIRegistry(String host, int port) throws RemoteException
	{
		this.registry = LocateRegistry.getRegistry(host, port);
	}

	public Registry getRegistry()
	{
		return registry;
	}

	public static synchronized QuantfabricRMIRegistry getInstance() throws RemoteException
	{		
		if (instance == null) 
		{
            synchronized (QuantfabricRMIRegistry.class)
            {
            	if (instance == null) 
            	{
            		instance = new QuantfabricRMIRegistry();
                }
            }
	
		}
		return instance;
	}
	
	public static synchronized QuantfabricRMIRegistry getInstance(String host) throws RemoteException
	{		
		if (instance == null) 
		{
            synchronized (QuantfabricRMIRegistry.class)
            {
            	if (instance == null) 
            	{
            		instance = new QuantfabricRMIRegistry(host);
                }
            }
	
		}
		return instance;
	}
	
	public static synchronized QuantfabricRMIRegistry getInstance(String host, int port) throws RemoteException
	{		
		if (instance == null) 
		{
            synchronized (QuantfabricRMIRegistry.class)
            {
            	if (instance == null) 
            	{
            		instance = new QuantfabricRMIRegistry(host, port);
                }
            }
	
		}
		return instance;
	}
}
