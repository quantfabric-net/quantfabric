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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

public class AlgoServerTerminator
{
	private static final Logger log = LoggerFactory.getLogger(AlgoServerTerminator.class);
	public static void main(String[] args) throws RemoteException
	{
		String jmxConnectionURL = System.getProperty("com.quantfabric.algo.server.jmx.connection-url",
				null);
		if (jmxConnectionURL == null)
		{
			log.error("\"com.quantfabric.algo.server.jmx.connection-url\" isn't set.");
			return;
		}
			
		log.info("com.quantfabric.algo.server.jmx.connection-url={}", jmxConnectionURL);
			
		final com.quantfabric.algo.server.jmx.AlgoServerMgmtMBean algoServerMgmtMBean =
				com.quantfabric.algo.server.jmx.JMXAlgoServerService.getAlgoServerMgmtMBean(
						jmxConnectionURL);

		if (algoServerMgmtMBean == null)
		{
			log.error("Connection to Algo Server JMX service failed.");
			return;
		}
		
		log.info("Connected to Algo Server JMX service.");

		log.info("Shutting down AlgoServer...");
		algoServerMgmtMBean.shutdown();
		log.info("done.");
	}

}
