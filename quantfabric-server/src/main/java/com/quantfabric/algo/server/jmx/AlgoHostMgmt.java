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
package com.quantfabric.algo.server.jmx;


import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.server.AlgoHost;

public class AlgoHostMgmt implements AlgoHostMgmtMBean 
{
	private static final Logger log = LoggerFactory.getLogger(AlgoHostMgmt.class);
	
	private final AlgoHost algoHost;
    private final CountDownLatch shutdownLatch;

	public AlgoHostMgmt(AlgoHost algoHost, CountDownLatch shutdownLatch)
    {
        this.shutdownLatch = shutdownLatch;
        this.algoHost = algoHost;
    }
		
	@Override
	public synchronized void shutdown()
    {
        log.info("Shutdown action received through JMX");
        if(shutdownLatch != null)
        {
            log.info("Indicating shutdown action");
            shutdownLatch.countDown();
        }
    }
	
	@Override
	public synchronized void start() 
	{
		if(!algoHost.isServerStarted())
			algoHost.start();
		else 
			throw new IllegalStateException("Server is already started");
	}

	@Override
	public synchronized void stop() 
	{
		if(algoHost.isServerStarted())
			algoHost.stop();
		else 
			throw new IllegalStateException("Server is already stopped");		
	}
	
	@Override
	public synchronized boolean isServerStarted() throws IOException 
	{
		return algoHost.isServerStarted();
	}
}
