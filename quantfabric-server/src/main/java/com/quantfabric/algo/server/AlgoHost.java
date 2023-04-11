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
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.manger.BackTestingManager;
import com.quantfabric.algo.backtesting.manger.jmx.BackTestingManagerJMXService;
import com.quantfabric.algo.market.gate.MarketGatewayService;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.server.configuration.ServiceConfiguration;
import com.quantfabric.algo.server.jmx.AlgoServerWatchdog;
import com.quantfabric.algo.server.jmx.AlgoServerWatchdogListener;
import com.quantfabric.algo.server.jmx.JMXAlgoHostService;
import com.quantfabric.util.SessionId;
import com.quantfabric.util.ShutdownHook;
import com.quantfabric.util.QuantfabricException;

public class AlgoHost extends ServiceHost 
{
	private static final Logger log = LoggerFactory.getLogger( AlgoHost.class );
		   
    @Override
    protected void loadService(ServiceConfiguration config) 
    {
    	try 
    	{
    		super.loadService(config);
    		log.info("{} service is loaded ", config.getSrvName());
    	}
    	catch(QuantfabricException ex) 
    	{
    		log.error("service {}", config.getSrvName(),(ex));
    	}    	
    }
    
	@Override
	public synchronized void start()
	{
		if (!serverStarted)
		{
			try
			{
				log.info("Start server");
				
				Object service = ctx.getService(MarketGatewayService.class.getName());				
				
				MarketGatewayService gateway = null;
				
				if (service == null) {
					log.error("Gateway service is null");
				}
				else
				{
					gateway = (MarketGatewayService)service;
					gateway.start();
				}				
				
				super.start();

				log.info("The server started");
				
				if (gateway != null)
				{				
					log.info("Connecting to the venues");

					// pause before connect to market
					Thread.sleep(5000);
				
					gateway.connectToAll(true); // only autoConnect
				}
			}
			catch (Exception e)
			{
				log.error("Start server failed", e);
			}
		}
		else
			throw new IllegalStateException(Messages.ERR_STR_ILLEGAL_START);
	}
    
    @Override
	public synchronized void stop()
    {
		super.stop();
		log.info("Stop server");
    }
    
    
	public static void main(String[] args) 
	{
		try 
		{	
			log.info("----------------");
			log.info(" Start AlgoHost ");
			log.info("----------------");

			log.info("Cmd arguments : {} ", Arrays.toString(args));
			log.info("Environment variables : {} ", System.getenv());
			log.info("System properties : {}", System.getProperties());
			
			String cfg_file ="quantfabricAlgo.cfg.xml";		
		
		   	final CountDownLatch shutdownLatch = new CountDownLatch(1);
		   	
		   	String jmxConnectionURL = System.getProperty("com.quantfabric.algo.server.jmx.connection-url",
					null);
			if (jmxConnectionURL != null)
			{
				AlgoServerWatchdog watchdog = new AlgoServerWatchdog(
						jmxConnectionURL);
				
				watchdog.addAlgoServerWatchdogListener(new AlgoServerWatchdogListener() {
					
					@Override
					public void watchdogTriggered()
					{
						shutdownLatch.countDown();
					}
				});
				
				watchdog.start();
			}
		   	
		   	AlgoHost host = new AlgoHost();
			
			// get location services configuration
			String configFilePath = System.getProperty("com.quantfabric.algo.server.config-file-path",
					QuantfabricRuntime.getRootPath() + "\\" + cfg_file);
			
			log.info("Check custom config at {}", configFilePath);

			if (new File(configFilePath).exists())
				// load configuration					
				host.loadConfiguration(configFilePath);
			else
			{
				log.info("Custom config does not specified. Will use default config.");
				URL defaultConfigUrl = AlgoServer.class.getClassLoader().getResource(cfg_file);
				// load configuration
				host.loadConfiguration(defaultConfigUrl);
			}
			
			JMXAlgoHostService jmxAlgoHostService = 
				new JMXAlgoHostService(host,shutdownLatch);
            
			jmxAlgoHostService.start();	
			
			host.start();
            			
			BackTestingManagerJMXService btJmx = 
					new BackTestingManagerJMXService(JMXAlgoHostService.JMX_OBJECT_NAME, new BackTestingManager());
			btJmx.start();
			
		 	ShutdownHook shutdownHook = new ShutdownHook(shutdownLatch);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            try
            {
                log.info("Startup completed (SessionID : {}), the main thread is waiting for shutdown.", SessionId.getSessionID());
                shutdownLatch.await();
                shutdownHook.setShutdown(true);
            }
            catch(InterruptedException e)
            {
                log.error("Booster InterruptedException",e);
            }
            log.info("Shutting down service.");

            if(jmxAlgoHostService != null)
            	jmxAlgoHostService.destroy();

		} 
		catch(Throwable t)
        {
            log.error("Error", t);
            t.printStackTrace();
        }
		
       	log.info("Main thread ended.");
    
        QuantfabricRuntime.notifyShutdown();
        System.exit(0);
	}

}
