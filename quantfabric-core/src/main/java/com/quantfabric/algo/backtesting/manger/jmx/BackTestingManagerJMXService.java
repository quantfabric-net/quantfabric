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
package com.quantfabric.algo.backtesting.manger.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.quantfabric.util.Startable;

public class BackTestingManagerJMXService implements Startable
{
	private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	private final ObjectName JMXObjectName;
	private final BackTestingManagerMXBean service;
	
	public BackTestingManagerJMXService(String rootJMXObjectName, BackTestingManagerMXBean service) throws MalformedObjectNameException, NullPointerException
	{
		this.JMXObjectName = new ObjectName(rootJMXObjectName + ",group=services,serviceName=BackTesting Management");
		this.service = service;
	}
		
	public String getJMXObjectName()
	{
		return JMXObjectName.getCanonicalName();
	}
	
	public MBeanServer getMBeanServer()
	{
		return mBeanServer;
	}

	@Override
	public void start() throws Exception
	{
		mBeanServer.registerMBean(service, JMXObjectName);
	
	}

	@Override
	public void stop() throws Exception
	{
		mBeanServer.unregisterMBean(JMXObjectName);
	}
}
