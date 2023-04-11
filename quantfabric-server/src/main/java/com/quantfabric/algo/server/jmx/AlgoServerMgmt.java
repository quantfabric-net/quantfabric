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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.server.AlgoServer;

public class AlgoServerMgmt extends UnicastRemoteObject implements AlgoServerMgmtMBean
{
	private static final long serialVersionUID = 561170632920168061L;
	private static final Logger log = LoggerFactory.getLogger(AlgoServerMgmt.class);
	
	private final AlgoServer algoServer;
	private final CountDownLatch shutdownLatch;

	public AlgoServerMgmt(AlgoServer algoServer, CountDownLatch shutdownLatch) throws RemoteException
	{
		this.algoServer = algoServer;
		this.shutdownLatch = shutdownLatch;
	}

	@Override
	public String ping()
	{
		return algoServer.getName();
	}
	
	@Override
	public synchronized void shutdown() {
		log.info("Shutdown action received through JMX");
        if(shutdownLatch != null)
        {
            log.info("Indicating shutdown action");
            shutdownLatch.countDown();
        }
	}

}
