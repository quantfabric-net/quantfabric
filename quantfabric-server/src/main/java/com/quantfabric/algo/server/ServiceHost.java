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
package com.quantfabric.algo.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


import com.quantfabric.algo.runtime.RuntimeContext;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.runtime.QuantfabricRuntimeService;
import com.quantfabric.algo.server.configuration.ServiceConfiguration;
import com.quantfabric.algo.server.configuration.ServiceHostConfiguration;
import com.quantfabric.util.ConfigurationException;
import com.quantfabric.util.Startable;
import com.quantfabric.util.QuantfabricException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHost implements Startable {
	
	private static final Logger log = LoggerFactory.getLogger(ServiceHost.class);
	
	/////////////////////////////////////////////////////////////
	protected final RuntimeContext ctx = new QuantfabricRuntime();
	private ServiceHostConfiguration configuration;
	protected volatile boolean serverStarted = false;

	// Loading plug-ins 
    private void loadServices () 
    {
    	
    	// initialize services
		for (ServiceConfiguration srvCfg : configuration
									.getServicesConfigurations().values()) {
			loadService(srvCfg);
		}    	

    }
	protected void loadService (ServiceConfiguration config) {
		 try {
				ServiceActivator.CreateService(config, ctx);
			} catch (Throwable t) {
				
				throw new QuantfabricException("can't loading service",t);
			}
	 }


    /*****************************************
     * load configuration
     * 
     *****************************************/
    public synchronized void loadConfiguration(String filePath){
    	
    	if(serverStarted){
    		throw new QuantfabricException("Cannot load configuration while service running");
    	}
    	URL configUrl;
    	try {
    		configUrl = new File(filePath).toURI().toURL();
		} catch (MalformedURLException ex) {
			log.error("Configuration file is not found");
			throw new ConfigurationException("File not found");
		}
    	loadConfiguration(configUrl);

    }
  
    public synchronized void loadConfiguration(URL configUrl){

		// Load configuration
		ServiceHostConfiguration config = new ServiceHostConfiguration();
		
		try {
			config.configure(configUrl);
		} catch (Exception e) {
			log.error("Configuration error",e);
			throw new ConfigurationException(e);
		}
		
		configuration = config;
		
		setContextProperties();
		loadServices();
    }
   
    private void setContextProperties()
	{
		for (Map.Entry<String, Object> property: configuration.getContextProperties().entrySet())
			ctx.setProperty(property.getKey(), property.getValue());		
	}

    /*****************************************
     * Startable
     *****************************************/

    @Override
	public synchronized void start() {
		
		if(!serverStarted){

			for (Object service : ctx.getServices()) 
        		if(service instanceof QuantfabricRuntimeService){
     		       QuantfabricRuntimeService runtimeSrv = (QuantfabricRuntimeService)service;
 	        	   if(!runtimeSrv.isStarted())runtimeSrv.start();
     	    }	

			serverStarted = true;
		}
	}

    @Override
	public synchronized void stop(){
		if(serverStarted){
			for (Object service : ctx.getServices()) 
        		if(service instanceof QuantfabricRuntimeService){
     		       QuantfabricRuntimeService runtimeSrv = (QuantfabricRuntimeService)service;
 	        	   runtimeSrv.stop();
     	    }	
			serverStarted = false;
		}
	}

	public synchronized boolean isServerStarted() {
		return serverStarted;
	}
 
   
}

