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
package com.quantfabric.algo.market.gate.jmx;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.gateway.MarketConnection;
import com.quantfabric.algo.market.gateway.MarketGatewayManager;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.market.gate.jmx.mbean.ExecutionFeedMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.InstrumentMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketConnectionMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataFeedMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataPipelineMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketGatewayMBean;


public class MGatewayJMXProvider {
	
	public MGatewayJMXProvider(MarketGatewayManager service){
		theService = service;
	}

	public void start(){
    	try{
    		ObjectName mbeanName = new ObjectName(JMX_OBJECT_NAME);
    		MarketGatewayMgmt mbean = new MarketGatewayMgmt(theService,this);
    		mbs.registerMBean(new StandardMBean( mbean, MarketGatewayMBean.class),mbeanName);
         }
        catch(Exception ex)
        {
            log.error("Failed to register MBean: {}, {}", ex.getMessage(), ex);
        }
        
        registerServiceSubMgmtBeans();
 	}	
	

	private final MarketGatewayManager theService;
	private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static final Logger log = LoggerFactory.getLogger(MGatewayJMXProvider.class);
    public static final String JMX_OBJECT_NAME = "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway";
    public static final String DEFAULT_MBS_DOMAIN = "jmxrmi";

    
    public ObjectName registerMarketConnection(MarketConnection marketConnection)
    {
    	try
		{
			ObjectName mcBeanName = 
				InternalObjectNameUtil.toObjectNameMarketConnection("Xcellerate", marketConnection);
			MarketConnectionMgmt mbean = new MarketConnectionMgmt(this, marketConnection);
			
			mbs.registerMBean(new StandardMBean(mbean, MarketConnectionMBean.class), mcBeanName);
			
			return mcBeanName;
		}
		catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
			   NotCompliantMBeanException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    public ObjectName registerMarketDataPipeline(MarketDataPipeline marketDataPipeline)
    {
    	try
		{
			ObjectName mdpBeanName = 
				InternalObjectNameUtil.toObjectNameMarketDataPipeline("Xcellerate", marketDataPipeline);
			MarketDataPipelineMgmt mbean = new MarketDataPipelineMgmt(marketDataPipeline);
			
			mbs.registerMBean(new StandardMBean(mbean, MarketDataPipelineMBean.class), mdpBeanName);
		
			return mdpBeanName;
		}
		catch (MalformedObjectNameException | NotCompliantMBeanException | MBeanRegistrationException |
			   InstanceAlreadyExistsException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    public ObjectName registerExecutionFeed(MarketConnection connection, ExecutionFeed feed)
    {
    	try
		{
			ObjectName fBeanName = 
				InternalObjectNameUtil.toObjectNameFeed("Xcellerate", feed, connection);
			ExecutionFeedMgmt mbean = new ExecutionFeedMgmt(feed);
			
			mbs.registerMBean(new StandardMBean(mbean, ExecutionFeedMBean.class), fBeanName);
		
			return fBeanName;
		}
		catch (MalformedObjectNameException | NotCompliantMBeanException | MBeanRegistrationException |
			   InstanceAlreadyExistsException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    public Set<ObjectName> getMarketDataFeeds(MarketConnection connection)
    {
    	try
		{
			return mbs.queryNames(new ObjectName(
					InternalObjectNameUtil.rootForMarketDataFeeds("Xcellerate", connection.getName()) + "*"), null);
		}
		catch (MalformedObjectNameException | NullPointerException e)
		{
			log.error(e.getMessage());
		}

		return Collections.emptySet();
    }
    
    public Set<ObjectName> getExecutionFeeds(MarketConnection connection)
    {
    	try
		{
			return mbs.queryNames(new ObjectName(
					InternalObjectNameUtil.rootForExecutionFeeds("Xcellerate", connection.getName()) + "*"), null);
		}
		catch (MalformedObjectNameException | NullPointerException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    public ObjectName registerMarketDataFeed(MarketConnection connection, MarketDataFeed feed)
    {
    	try
		{
			ObjectName fBeanName = 
				InternalObjectNameUtil.toObjectNameFeed("Xcellerate", feed, connection);
			MarketDataFeedMgmt mbean = new MarketDataFeedMgmt(feed);
			
			mbs.registerMBean(new StandardMBean(mbean, MarketDataFeedMBean.class), fBeanName);
		
			return fBeanName;
		}
		catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
			   NotCompliantMBeanException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    public ObjectName registerInstrument(Instrument instrument)
    {
    	try
		{
			ObjectName iBeanName = 
				InternalObjectNameUtil.toObjectNameInstrument("Xcellerate", instrument);
			InstrumentMgmt mbean = new InstrumentMgmt(instrument);
			
			mbs.registerMBean(new StandardMBean(mbean, InstrumentMBean.class), iBeanName);
			
			return iBeanName;
		}
		catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException |
			   NotCompliantMBeanException e)
		{
			log.error(e.getMessage());
		}

		return null;
    }
    
    private void registerServiceSubMgmtBeans()
    {
    	for (Instrument instrument : theService.getInstruments()) {
			registerInstrument(instrument);
		}
    	
    	for (MarketConnection mc : theService.getConnections())
    	{
    		registerMarketConnection(mc);
    		for (MarketDataFeed mdf : mc.getMarketDataFeeds())
    			registerMarketDataFeed(mc, mdf);
    		for (ExecutionFeed ef : mc.getExecutionFeeds())
    			registerExecutionFeed(mc, ef);
    	}
    	
    	for (MarketDataPipeline mdp : theService.getMDPipelines())
    		registerMarketDataPipeline(mdp);
    }
}
