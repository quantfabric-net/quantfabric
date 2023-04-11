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
package com.quantfabric.algo.market.dataprovider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.util.DOMElementIterator;

public class AbstractDataViewRequest implements DataViewRequest
{	
	private static final AtomicLong countInstances = new AtomicLong();

	private static final long generateId()
	{
		return countInstances.incrementAndGet();
	}

	private final long id = generateId();
	protected Set<String> dependences;
	protected Map<String, String> parameters;
		
	public AbstractDataViewRequest(Set<String> dependences,
			Map<String, String> parameters)
	{
		super();
		this.dependences = dependences;
		this.parameters = parameters;
	}

	public AbstractDataViewRequest()
	{
		this(new HashSet<String>(),  new HashMap<String,String>());
	}

	public Set<String> getDependences()
	{
		return dependences;
	}

	public Map<String, String> getParameters()
	{
		return parameters;
	}

	public long getId()
	{
		return id;
	}
	
	public static void parseValuedParameters(Map<String, String> parameters,
			Node parentElement)
	{
		DOMElementIterator parameterNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (parameterNodeIterator.hasNext())
		{
			Element element = parameterNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("parameter"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String value = element.getAttributes().getNamedItem("value")
						.getTextContent();
				parameters.put(name, value);
			}
		}

	}

	public static void parseDependences(Set<String> dependences,
			Node parentElement)
	{
		DOMElementIterator dependenceNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (dependenceNodeIterator.hasNext())
		{
			Element element = dependenceNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("dependence"))
			{
				String view = element.getAttributes().getNamedItem("view")
						.getTextContent();
				dependences.add(view);
			}
		}
	}
}