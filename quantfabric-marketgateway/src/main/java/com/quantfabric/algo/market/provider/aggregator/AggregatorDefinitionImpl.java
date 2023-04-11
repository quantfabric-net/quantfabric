
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
package com.quantfabric.algo.market.provider.aggregator;

import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.util.ConfigurationException;
import com.quantfabric.util.XMLConfigParser;

public class AggregatorDefinitionImpl implements AggregatorDefinition
{
	private final Class<? extends MarketViewAggregator> aggregatorClass;
	private final Properties properties;
	
	public AggregatorDefinitionImpl(AggregatorFactory.AggregatorTypes type)
	{
		this(type.getAggregatorClass(), new Properties());
	}
	
	public AggregatorDefinitionImpl(Class<? extends MarketViewAggregator> aggregatorClass)
	{
		this(aggregatorClass, new Properties());
	}

	public AggregatorDefinitionImpl(AggregatorFactory.AggregatorTypes type, Properties properties)
	{
		this(type.getAggregatorClass(), properties);
	}
	
	public AggregatorDefinitionImpl(Class<? extends MarketViewAggregator> aggregatorClass, Properties properties)
	{
		super();
		this.aggregatorClass = aggregatorClass;
		this.properties = properties;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.aggregator.AggregatorDefinition#getAggregatorClass()
	 */
	@Override
	public Class<? extends MarketViewAggregator> getAggregatorClass()
	{
		return aggregatorClass;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.aggregator.AggregatorDefinition#getProperties()
	 */
	@Override
	public Properties getProperties()
	{
		return properties;
	}
	
	@SuppressWarnings("unchecked")
	public static AggregatorDefinitionImpl load(Node aggregatorRootNode)
	{
		Node nodeType = aggregatorRootNode.getAttributes().getNamedItem("type");
		if (nodeType == null)
		{
			Node nodeClassName = aggregatorRootNode.getAttributes().getNamedItem("class-name");
			if (nodeClassName == null)
			{
				throw new ConfigurationException("Unknown aggregator");
			}
				
			String className = nodeClassName.getTextContent();
			
			if (className.equals(""))
				return null;
			
			Class<? extends MarketViewAggregator> clazz = null;
			
			try
			{
				clazz = (Class<? extends MarketViewAggregator>) Class.forName(className).getConstructor().newInstance();
			}
			catch (Exception e)
			{
				throw new ConfigurationException("Create aggregator is failed");
			}
			
			Properties someClassProperties = new Properties();
			
			DOMElementIterator someClassAggregatorNodeIterator = new DOMElementIterator(aggregatorRootNode.getChildNodes());
			while(someClassAggregatorNodeIterator.hasNext())
			{
				Element someClassAggregatorSubElement = someClassAggregatorNodeIterator.next();
				if (someClassAggregatorSubElement.getNodeName().equals("settings"))
				{
					someClassProperties = XMLConfigParser.parseSettingsNode(someClassAggregatorSubElement);
				}
			}

			if (clazz != null)
				return new AggregatorDefinitionImpl(clazz, someClassProperties);

			return null;

		}
		
		String sType = nodeType.getTextContent();
		AggregatorFactory.AggregatorTypes type = AggregatorFactory.AggregatorTypes.valueOf(sType);
		
		Properties ohlcProperties = new Properties();
		
		DOMElementIterator ohlcAggregatorNodeIterator = new DOMElementIterator(aggregatorRootNode.getChildNodes());
		while(ohlcAggregatorNodeIterator.hasNext())
		{
			Element ohlcAggregatorSubElement = ohlcAggregatorNodeIterator.next();
			if (ohlcAggregatorSubElement.getNodeName().equals("settings"))
			{
				ohlcProperties = XMLConfigParser.parseSettingsNode(ohlcAggregatorSubElement);
			}
		}

		return new AggregatorDefinitionImpl(type, ohlcProperties);
	}

	@Override
	public String toString()
	{
		return "AggregatorDefinition [aggregatorClass=" + aggregatorClass
				+ ", properties=" + properties + "]";
	}
}
