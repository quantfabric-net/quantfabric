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
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.server.AlgoServer;

public class JMXAlgoServerService
{
	private static final Logger log = LoggerFactory.getLogger(JMXAlgoServerService.class);
	private static final String jmxServerName = "AlgoServer:name=algoServer";
	
    private final String connectionURL;
    private final int algoServerJMXPort;
    private final AlgoServer algoServer;
    private AlgoServerMgmt serverBean;
    private ObjectName serverName;
    private Registry registry;
    private JMXServiceURL jmxUrl;
    private JMXConnectorServer jmxConnectorServer;
    private MBeanServer mBeanServer;
    private final String registryName = "server";
    private final CountDownLatch shutdownLatch;

	public JMXAlgoServerService(AlgoServer algoServer, CountDownLatch shutdownLatch, String algoServerJMXhost, int algoServerJMXPort) {
		this.algoServer = algoServer;
		this.connectionURL = "service:jmx:rmi:///jndi/rmi://" + 
				algoServerJMXhost + ":" + algoServerJMXPort + "/AlgoServer";
		this.algoServerJMXPort = algoServerJMXPort;
		this.shutdownLatch = shutdownLatch;
		
		try
		{
			serverBean = new AlgoServerMgmt(this.algoServer, this.shutdownLatch);
			serverName = new ObjectName(jmxServerName);
			registry = LocateRegistry.createRegistry(this.algoServerJMXPort);
			jmxUrl = new JMXServiceURL(this.connectionURL);
			mBeanServer = ManagementFactory.getPlatformMBeanServer();
		}
		catch (Exception ex)
		{
			log.error("Failed to create MBean: " + ex.getMessage(), ex);
		}
	}
	
    public String getConnectionURL()
	{
		return connectionURL;
	}

	public synchronized void start()
    {
        if(log.isInfoEnabled())
            log.info("Starting JMX connector for Algo Server pooling.");
               
        try { 
        	mBeanServer.registerMBean(serverBean, serverName);
            registry.rebind(registryName, serverBean);
            jmxConnectorServer = 
            		JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mBeanServer);
            jmxConnectorServer.start();            
        }
        catch(Exception ex)
        {
            log.error("Failed to register MBean: " + ex.getMessage(), ex);
        }
    }
    
    public synchronized void stop()
    {
    	try
		{
			jmxConnectorServer.stop();
	    	registry.unbind(registryName);
	    	mBeanServer.unregisterMBean(serverName);
		}
		catch (Exception ex)
		{
			log.error("Failed to unregister MBean: " + ex.getMessage(), ex);
		}
    }
    
    public static AlgoServerMgmtMBean getAlgoServerMgmtMBean(String connectionURL) {
		AlgoServerMgmtMBean bean = null;

		JMXConnector cntor = null;
		try {
			// The address of the connector server
			JMXServiceURL address = new JMXServiceURL(connectionURL);

			// The creation environment map, null in this case
			Map<String, ?> creationEnvironment = null;

			// Create the JMXCconnectorServer
			cntor = JMXConnectorFactory.newJMXConnector(address, creationEnvironment);

			// The connection environment map, null in this case
			// May contain - for example - user's credentials
			Map<String, ?> connectionEnvironment = null;

			// Connect
			cntor.connect(connectionEnvironment);

			// Obtain a "stub" for the remote MBeanServer
			MBeanServerConnection mbsc = cntor.getMBeanServerConnection();

			bean = JMX.newMXBeanProxy(mbsc, new ObjectName(jmxServerName), AlgoServerMgmtMBean.class);

		} catch (Exception ex) {
			log.error("Failed to get MBean: " + ex.getMessage(), ex);
			return null;
		} finally {
			try {
				if (cntor != null) {
					cntor.close();
				}
			} catch (IOException e) {
				log.error("Failed to close to close JMXCconnector: ", e);
			}
		}
		return bean;
	}

}
