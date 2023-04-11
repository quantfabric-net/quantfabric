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
package com.quantfabric.algo.server.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.quantfabric.util.NodeListIterator;
import com.quantfabric.util.XMLConfigParser;

public class AlgoServerConfig {
	
	private String name;
	private String port;
	private List<AlgoHostConfig> ahc;
	private Properties prop;

	public String getName()
	{
		return name;
	}
	
	public String getPort()
	{
		return port;
	}
	
	public List<AlgoHostConfig> getAlgoHostList()
	{
		return ahc;
	}

	public Properties getProperties()
	{
		return prop;
	}

	public static AlgoServerConfig load (Node parrentNode)
	{
		AlgoServerConfig retVal = new AlgoServerConfig();
		
		List<AlgoHostConfig> ahc = new ArrayList<AlgoHostConfig>();

		// Get attributes.
		NamedNodeMap pna = parrentNode.getAttributes();
		Node n = pna.getNamedItem("port");
		retVal.port = n.getNodeValue();

		n = pna.getNamedItem("name");
		retVal.name = n.getNodeValue();
		
		retVal.prop = XMLConfigParser.parseSettingsNode(parrentNode);
		
		NodeListIterator nodesIterator = new NodeListIterator(parrentNode.getChildNodes());
		
		while (nodesIterator.hasNext())
		{
			Node node = nodesIterator.next();

			if (node.getNodeName().equals("algoHost"))
			{
				ahc.add(AlgoHostConfig.load(node));
			}
		}

		retVal.ahc = ahc;
		
		return retVal;
	}
}
