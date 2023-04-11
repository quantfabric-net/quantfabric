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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.quantfabric.util.NodeListIterator;

public class QueryDataViewRequestImpl extends AbstractDataViewRequest implements
		QueryDataViewRequest, Serializable
{
	private static final long serialVersionUID = 9133578816410027846L;

	private String query;
	private DataView customDataView;

	public QueryDataViewRequestImpl()
	{
		super();
	}

	public QueryDataViewRequestImpl(
			Set<String> dependences,  Map<String, String> parameters,
			String query, DataView customDataView)
	{
		super(dependences, parameters);
		this.query = query;
		this.customDataView = customDataView;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public DataView getCustomDataView()
	{
		return customDataView;
	}

	public void setCustomDataView(DataView customDataView)
	{
		this.customDataView = customDataView;
	}
		
	@Override
	public String toString()
	{
		return "QueryDataViewRequestImpl [query=" + query + ", customDataView="
				+ customDataView + ", dependences=" + dependences
				+ ", parameters=" + parameters + "]";
	}

	public static QueryDataViewRequest fromXML(Node queryDataViewRequestRootNode, 
			Set<String> dependences, Map<String, String> parameters)
	{
		DataView customDataView = null;
		
		NodeListIterator observationNodeIterator = new NodeListIterator(queryDataViewRequestRootNode.getChildNodes());
		
		while (observationNodeIterator.hasNext())
		{
			Node observationSubElement = observationNodeIterator.next();

			if (observationSubElement.getNodeName().equals("query"))
			{
				String query = observationSubElement.getTextContent().trim();
				parameters.put("query", query);
			}
			else if (observationSubElement.getNodeName().equals("customDataView"))
			{						
				customDataView = DataViewImpl.fromXML(observationSubElement);
			}
		}		
		
		return new QueryDataViewRequestImpl(dependences, parameters, parameters.get("query"), 
				customDataView);
	}
}
