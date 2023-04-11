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

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.server.AlgoHost;

public class JMXAlgoHostService
{
    private static final Logger log = LoggerFactory.getLogger(JMXAlgoHostService.class);
  
    public static final String JMX_OBJECT_NAME = "Xcellerate:type=AlgoHost";
    public static final String DEFAULT_MBS_DOMAIN = "jmxrmi";
    
    private final CountDownLatch shutdownLatch;
    private final AlgoHost algoHost;
	
	public JMXAlgoHostService(AlgoHost algoHost, CountDownLatch shutdownLatch)
    {
        this.shutdownLatch = shutdownLatch;
        this.algoHost = algoHost;
    }
		
    public synchronized void start()
    {
        if(log.isInfoEnabled())
            log.info("Starting JMX connector for server management.");
               
    	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
    	
    	try
    	{
    		ObjectName mbeanName = new ObjectName(JMX_OBJECT_NAME);
    		AlgoHostMgmt mbean = new AlgoHostMgmt(algoHost,shutdownLatch);
    		mbs.registerMBean(mbean, mbeanName);
        }
        catch(Exception ex)
        {
            log.error("Failed to register MBean: " + ex.getMessage(), ex);
        }
    }

    public synchronized void destroy()
    {
        if(log.isDebugEnabled())
            log.debug("Disconnecting JMX connector");
    }
}
