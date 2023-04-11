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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.quantfabric.util.NodeListIterator;

public class Config {
	
	private List<AlgoServerConfig> asc;
	
	public List<AlgoServerConfig> getAlgoServerConfigs ()
	{
		return Collections.unmodifiableList(asc);
	}
	
	public static Config load(File fXmlFile) throws ParserConfigurationException, SAXException, IOException
	{
		Config retVal = new Config();
					
		// Read and parse XML file.
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		List<AlgoServerConfig> ascList = new ArrayList<AlgoServerConfig>();
		
		NodeListIterator nodesIterator = new NodeListIterator(doc.getDocumentElement().getChildNodes());
		
		while (nodesIterator.hasNext())
		{
			Node node = nodesIterator.next();

			if (node.getNodeName().equals("algoServer"))
			{
				ascList.add(AlgoServerConfig.load(node));
			}
		}
		
		retVal.asc = ascList;			
		
		return retVal;
	}

}
