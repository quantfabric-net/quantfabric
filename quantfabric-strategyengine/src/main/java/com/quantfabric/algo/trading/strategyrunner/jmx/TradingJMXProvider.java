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
package com.quantfabric.algo.trading.strategyrunner.jmx;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.trading.execution.ExecutionProvider;
import com.quantfabric.algo.trading.execution.jmx.ExecutionJMXService;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategyrunner.StrategyLoadRunner;

public class TradingJMXProvider {
	
	private ExecutionJMXService executionJMXService;
	
	public TradingJMXProvider(StrategyLoadRunner service){
		this.service = service;
	}

	public void start(){
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
    	try{
    		ObjectName mbeanName = new ObjectName(JMX_OBJECT_NAME);
    		StrategyLoadRunnerMgmt mbean = new StrategyLoadRunnerMgmt(service,this);
    		mbs.registerMBean( mbean,mbeanName);
    		
    		executionJMXService = new ExecutionJMXService(JMX_OBJECT_NAME);
    		executionJMXService.start();
         }
        catch(Exception ex)
        {
            log.error("Failed to register MBean: " + ex.getMessage(), ex);
        }
        registerServiceSubMgmtBeans();
	}
	
	public void stop()
	{
		try
		{
			ObjectName mbeanName = new ObjectName(JMX_OBJECT_NAME);
			mbs.unregisterMBean(mbeanName);
			executionJMXService.stop();
		}
		catch(Exception ex)
	    {
	         log.error("Failed to unregister MBean: " + ex.getMessage(), ex);
	    }
		unregisterServiceSubMgmtBeans();
	}
	
	public void registerExecutionProvider(ExecutionProvider executionProvider)
	{
		executionJMXService.registerExecutionProvider(executionProvider);
	}
	
	public void unregisterExecutionProvider(ExecutionProvider executionProvider)
	{
		executionJMXService.unregisterExecutionProvider(executionProvider);
	}
	
	public void registerStrategy(TradingStrategy tradingStrategy) {
		try {
			ObjectName mbeanName = InternalObjectNameUtil.toObjectNameStrategy("Xcellerate",tradingStrategy);
			TradingStrategyMgmt mbean = new TradingStrategyMgmt(tradingStrategy);
			mbs.registerMBean(mbean, mbeanName);				
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
				 NotCompliantMBeanException e) {
			log.error(e.getMessage());
		}

	}
	
	public void unregisterStrategy(TradingStrategy tradingStrategy)
	{
		ObjectName mbeanName;
		try
		{
			mbeanName = InternalObjectNameUtil.toObjectNameStrategy("Xcellerate",tradingStrategy);
			mbs.unregisterMBean(mbeanName);
		}
		catch (MalformedObjectNameException | InstanceNotFoundException | MBeanRegistrationException e)
		{
			log.error(e.getMessage());
		}
	}
	
	private void registerServiceSubMgmtBeans(){
		Map<String, TradingStrategy> strategies = service.getStrategies();
		
		for (String  strategyName : strategies.keySet()) {
			TradingStrategy strategy=strategies.get(strategyName);
			registerStrategy(strategy);			
			registerExecutionProvider(strategy.getExecutionProvider());
		}
	}
	
	private void unregisterServiceSubMgmtBeans(){
		Map<String, TradingStrategy> strategies = service.getStrategies();
		
		for (String  strategyName : strategies.keySet()) {
			TradingStrategy strategy=strategies.get(strategyName);
			unregisterStrategy(strategy);			
			unregisterExecutionProvider(strategy.getExecutionProvider());
		}
	}

	private final StrategyLoadRunner service;
	private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(TradingJMXProvider.class);
    public static final String JMX_OBJECT_NAME = "Xcellerate:type=AlgoServer,group=services,serviceName=Trading";
    public static final String DEFAULT_MBS_DOMAIN = "jmxrmi";


}
