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
package com.quantfabric.algo.trading.execution.tradeMonitor.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.quantfabric.algo.trading.execution.jmx.ExecutionJMXService;
import com.quantfabric.algo.trading.execution.tradeMonitor.TradeMonitor;
import com.quantfabric.algo.trading.execution.tradeMonitor.jmx.mbean.TradeMonitorMXBean;

public class TradeMonitorMgmt implements TradeMonitorMXBean
{
	private static final String tradeMonitrosGroupName = "StrategyTradeMonitors";
	
	private final TradeMonitor tradeMonitor;
	ExecutionJMXService executionJMXService;
	
	public TradeMonitorMgmt(ExecutionJMXService executionJMXService, TradeMonitor tradeMonitor)
	{
		this.executionJMXService = executionJMXService;
		this.tradeMonitor = tradeMonitor;
	}
	
	public String getTradeMonitorName()
	{
		return tradeMonitor.getName();
	}
	
	public void registerMBeanObject() throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
	{
		String tradeMonitorObjectName = executionJMXService.getJMXObjectName() +
			",tradeMonitorsGroup=" + tradeMonitrosGroupName + 
			",tradeMonitor=" + getTradeMonitorName();
		
		ObjectName objectName = new ObjectName(tradeMonitorObjectName);
		executionJMXService.getMBeanServer().registerMBean(this, objectName);
		
		if (tradeMonitor.getTripMeter() != null)
			registerTripMeter(tradeMonitorObjectName);
	}
	
	public void unregisterMBeanObject() throws MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, NotCompliantMBeanException
	{
		String tradeMonitorObjectName = executionJMXService.getJMXObjectName() +
				",tradeMonitorsGroup=" + tradeMonitrosGroupName + 
				",tradeMonitor=" + getTradeMonitorName();
			
		ObjectName objectName = new ObjectName(tradeMonitorObjectName);
		
		executionJMXService.getMBeanServer().unregisterMBean(objectName);
		
		if (tradeMonitor.getTripMeter() != null)
			unregisterTripMeter(tradeMonitorObjectName);
	}
	
	private void registerTripMeter(String tradeMonitorObjectName) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
	{
		ObjectName objectName = new ObjectName(tradeMonitorObjectName 
				+ ", tripMetersGroup=TripMeters, tripMeter=TripMeter");
		
		executionJMXService.getMBeanServer().registerMBean(tradeMonitor.getTripMeter(), objectName);
	}
	
	private void unregisterTripMeter(String tradeMonitorObjectName) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, InstanceNotFoundException
	{
		ObjectName objectName = new ObjectName(tradeMonitorObjectName 
				+ ", tripMetersGroup=TripMeters, tripMeter=TripMeter");
		
		executionJMXService.getMBeanServer().unregisterMBean(objectName);
	}
}
