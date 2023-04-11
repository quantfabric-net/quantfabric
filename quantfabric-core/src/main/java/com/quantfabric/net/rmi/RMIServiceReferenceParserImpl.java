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
package com.quantfabric.net.rmi;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import com.quantfabric.util.ConfigurationException;

public class RMIServiceReferenceParserImpl implements RMIServiceReferenceParser
{
	private Element rootElement = null;
		
	public RMIServiceReferenceParserImpl()
	{
		super();
	}

	public RMIServiceReferenceParserImpl(Element rootElement)
	{
		this();
		this.rootElement = rootElement;
	}

	@Override
	public void setRoot(Element rootElement)
	{
		this.rootElement = rootElement;
	}

	@Override
	public RMIServiceReference getRmiServiceReference()
	{
		if (rootElement == null)
			throw new ConfigurationException("Root element of the node doesn't specified.");
				
		final String rmiHost = StringUtils.defaultIfEmpty(rootElement.getAttribute("rmiHost"), "localhost");		
		final String rmiServiceName = rootElement.getAttribute("rmiServiceName");
		final int rmiPort = 
				Integer.parseInt(StringUtils.defaultIfEmpty(rootElement.getAttribute("rmiPort"), 
						String.valueOf(QuantfabricRMIRegistry.DEFAULT_RMI_REGISTRY_PORT)));
		final int rmiServicePort = 
				Integer.parseInt(StringUtils.defaultIfEmpty(rootElement.getAttribute("rmiServicePort"), "0"));

		
		return new RMIServiceReference() {			

			private static final long serialVersionUID = 7769713795853595947L;

			@Override
			public String getRmiServiceName()
			{
				return rmiServiceName;
			}
			
			@Override
			public int getRmiPort()
			{
				return rmiPort;
			}
			
			@Override
			public String getRmiHost()
			{
				return rmiHost;
			}

			@Override
			public int getRmiServicePort()
			{
				return rmiServicePort;
			}
			
			@Override
			public String toString()
			{
				return "[ RMIServiceReference : RmiRegistyHost=" + getRmiHost() + "; RmiRegistryPort=" + getRmiPort() + "; Service=" + getRmiServiceName() +" ] ";
			}
		};
		
	}
	
}
