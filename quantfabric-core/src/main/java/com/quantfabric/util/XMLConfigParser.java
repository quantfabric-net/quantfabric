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
package com.quantfabric.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;

public class XMLConfigParser
{
	public static Properties findAndParseSettingsNode(Element root, String settingsNodeName)
	{
		Element element = findNode(root, settingsNodeName);
		
		if (element != null)
			return XMLConfigParser.parseSettingsNode(element);

		return null;
	}
	
	public static Element findNode(Element root, String nodeName)
	{
		DOMElementIterator domElementIterator = new DOMElementIterator(root.getChildNodes());	
		
		while(domElementIterator.hasNext())
		{
			Element element = domElementIterator.next();
			String elementName = element.getNodeName();
			
			if (elementName.equals(nodeName))
			{
				return element;
			}
		}
		return null;
	}
	
	public static Properties parseSettingsNode(Node settingsNode)
	{
		Properties properties = new Properties();
		
		NodeList nodes = settingsNode.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node settingNode = nodes.item(i);

			if (settingNode.getNodeName().equals("setting"))
			{
				Node propertyNameAttr = settingNode.getAttributes().getNamedItem("name");
				if (propertyNameAttr != null)
				{
					String propertyName = propertyNameAttr.getTextContent();
					Node propertyValueAttr = settingNode.getAttributes().getNamedItem("value");
					
					if (propertyValueAttr != null)
					{
						String propertyValue = QuantfabricRuntime.sabstituteVariableValues(propertyValueAttr.getTextContent());
						properties.put(propertyName, propertyValue);
					}
					else
						properties.put(propertyName, null);						
				}
			}
		}
		return properties;
	}
	
	public static Collection<PersistingUpdateListenerConfig> parseEsperPersisterSettings(
			Element parentElement)
	{
		DOMElementIterator persisterSettingsNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());

		Collection<PersistingUpdateListenerConfig> esperEventPersisterConfigs = new ArrayList<PersistingUpdateListenerConfig>();

		while (persisterSettingsNodeIterator.hasNext())
		{
			Element persisterSettingsElement = persisterSettingsNodeIterator
					.next();

			if (persisterSettingsElement.getNodeName().equals(
					"esperPersistingUpdateListener"))
				esperEventPersisterConfigs.add(PersistingUpdateListenerConfig
						.getFromXML(persisterSettingsElement));
		}

		return esperEventPersisterConfigs;
	}
}
