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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListIterator implements Iterator<Node>
{
	private final Iterator<Node> iterator;
	
	public NodeListIterator(NodeList nodeList)
	{
		super();
		this.iterator = list(nodeList).iterator();
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public Node next()
	{
		return iterator.next();
	}

	@Override
	public void remove()
	{
		iterator.remove();		
	}

	private static List<Node> list(NodeList nodeList)
	{
		List<Node> list = new LinkedList<Node>();
		
		for (int i = 0; i < nodeList.getLength(); list.add(nodeList.item(i++)));
		
		return list;
	}

}
