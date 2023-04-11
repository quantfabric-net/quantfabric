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
package com.quantfabric.algo.trading.execution.jmx;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.trading.execution.ExecutionProvider;
import com.quantfabric.algo.trading.execution.tradeMonitor.jmx.TradeMonitorMgmt;
import com.quantfabric.util.Startable;

public class ExecutionJMXService implements Startable
{
	private static final Logger log = LoggerFactory.getLogger(ExecutionJMXService.class);
	private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	
	private final Map<String, TradeMonitorMgmt> tradeMonitorMgmts = new HashMap<String, TradeMonitorMgmt>();
	
	private final String JMXObjectName;
	
	public ExecutionJMXService(String rootJMXObjectName)
	{
		JMXObjectName = rootJMXObjectName + ",executionServiceName=Execution";
	}
		
	public String getJMXObjectName()
	{
		return JMXObjectName;
	}
	
	public MBeanServer getMBeanServer()
	{
		return mBeanServer;
	}

	@Override
	public void start() throws Exception
	{
		mBeanServer.registerMBean(new ExecutionMgmt(this), new ObjectName(JMXObjectName));
		log.info("ExecutionJMXProvider started");		
	}

	@Override
	public void stop() throws Exception
	{
		mBeanServer.unregisterMBean(new ObjectName(JMXObjectName));
		log.info("ExecutionJMXProvider stoped");		
	}
	
	public void registerExecutionProvider(ExecutionProvider executionProvider)
	{
		try
		{
			TradeMonitorMgmt tradeMonitorMgmt = new TradeMonitorMgmt(this, executionProvider.getTradeMonitor());

			tradeMonitorMgmt.registerMBeanObject();
			
			tradeMonitorMgmts.put(tradeMonitorMgmt.getTradeMonitorName(), tradeMonitorMgmt);			
		}
		catch (Exception e)
		{
			log.error("Can't register TradeMonitor", e);
		}
	}
	
	public void unregisterExecutionProvider(ExecutionProvider executionProvider)
	{
		try
		{
			if (tradeMonitorMgmts.containsKey(executionProvider.getTradeMonitor().getName())) 
				tradeMonitorMgmts.get(executionProvider.getTradeMonitor().getName()).unregisterMBeanObject();
			else
				throw new Exception("TradeMonitor is not registered.");	
		}
		catch (Exception e)
		{
			log.error("Can't unregister TradeMonitor", e);
		}
	}

}
