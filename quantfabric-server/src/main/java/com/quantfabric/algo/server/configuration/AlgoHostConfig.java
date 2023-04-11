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

import java.util.Properties;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.quantfabric.util.XMLConfigParser;


public class AlgoHostConfig {

	private String name;
	private String mainClassName;
	private String pathToLoad;
	private Properties prop;
	
	public String getName()
	{
		return name;
	}
	
	public String getMainClassName()
	{
		return mainClassName;
	}

	public String getPathToLoad()
	{
		return pathToLoad;
	}

	public Properties getProperties()
	{
		return prop;
	}


	public static AlgoHostConfig load (Node parrentNode)
	{
		AlgoHostConfig retVal = new AlgoHostConfig();
		
		// Get attributes.
		NamedNodeMap pna = parrentNode.getAttributes();
		Node n = pna.getNamedItem("name");
		retVal.name = n.getNodeValue();

		n = pna.getNamedItem("mainClassName");
		retVal.mainClassName = n.getNodeValue();

		n = pna.getNamedItem("pathToLoad");
		retVal.pathToLoad = n.getNodeValue();
		
		retVal.prop = XMLConfigParser.parseSettingsNode(parrentNode);

		return retVal;
	}
	
}
