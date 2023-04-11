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

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;

public class ServiceContext {
	   public ServiceContext(String domainName, MBeanServer mbs, boolean useThreadPoolForNotifications, JMXConnectorServer connectorServer, boolean isReleaseMBeanServerWhenDone)
	    {
	        this.domainName = domainName;
	        this.mbs = mbs;
	        this.useThreadPoolForNotifications = useThreadPoolForNotifications;
	        this.connectorServer = connectorServer;
	        this.isReleaseMBeanServerWhenDone = isReleaseMBeanServerWhenDone;
	    }

	    public boolean isUseThreadPoolForNotifications()
	    {
	        return useThreadPoolForNotifications;
	    }
	    public String getDomainName()
	    {
	        return domainName;
	    }

	    public MBeanServer getMbs()
	    {
	        return mbs;
	    }

	    public JMXConnectorServer getConnectorServer()
	    {
	        return connectorServer;
	    }

	    public boolean isReleaseMBeanServerWhenDone()
	    {
	        return isReleaseMBeanServerWhenDone;
	    }
	    private final String domainName;
	    private final MBeanServer mbs;
	    private final boolean useThreadPoolForNotifications;
	    private final JMXConnectorServer connectorServer;
	    private final boolean isReleaseMBeanServerWhenDone;
}
