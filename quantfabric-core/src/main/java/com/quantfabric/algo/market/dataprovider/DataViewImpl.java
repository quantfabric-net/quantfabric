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

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.cep.StatementDefinition;
import com.quantfabric.cep.StatementDefinitionImpl;
import com.quantfabric.cep.StatementDefinitionImpl.PersistModes;

public class DataViewImpl implements DataView {
	private final boolean isStatic;
	private String compatibleVesion;
	private final String name;
	private String eventType;
	private String prefix="";
	private final List<StatementDefinition> statements = new ArrayList<StatementDefinition>();
	private final Set<String> dependence = new HashSet<String>();
	private final Set<String> parameters = new HashSet<String>();
	/************************************************************/
	public DataViewImpl()
	{		
		this("");
	}
	@ConstructorProperties({"name"})
	public DataViewImpl(String name) {
		this(name,false);
	}
	@ConstructorProperties({"name", "static"})
	public DataViewImpl(String name,boolean isStatic) {
		if(name==null || name.isEmpty())
			throw new IllegalArgumentException("A name of dataview can't be empty");
		this.name = name;
		this.isStatic = isStatic;
	}

	/************************************************************/
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getCompatibleVesion()
	 */
	public String getCompatibleVesion() {
		return compatibleVesion;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#isStatic()
	 */
	public boolean isStatic() {
		return isStatic;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#setCompatibleVesion(java.lang.String)
	 */
	public void setCompatibleVesion(String compatibleVesion) {
		this.compatibleVesion = compatibleVesion;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getEventType()
	 */
	public String getEventType() {
		return eventType;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#setEventType(java.lang.String)
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getPrefix()
	 */
	public String getPrefix() {
		return prefix;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#setPrefix(java.lang.String)
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getStatements()
	 */
	public List<StatementDefinition> getStatements() {
		return statements;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getDependences()
	 */
	public Set<String> getDependences() {
		return dependence;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.IDataView#getParameters()
	 */
	public Set<String> getParameters() {
		return parameters;
	}
	
	public static DataViewImpl fromXML(Node parentElement)
	{
		DOMElementIterator dataviewNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		String name = parentElement.getAttributes().getNamedItem("name")
				.getTextContent();
		String eventType = parentElement.getAttributes()
				.getNamedItem("eventType").getTextContent();
		String prefix = parentElement.getAttributes().getNamedItem("prefix")
				.getTextContent();
		DataViewImpl customDataView = new DataViewImpl(name);
		customDataView.setEventType(eventType);
		customDataView.setPrefix(prefix);
		while (dataviewNodeIterator.hasNext())
		{
			Element element = dataviewNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("dependences"))
			{
				AbstractDataViewRequest.
					parseDependences(customDataView.getDependences(), element);
			}
			else if (nodeName.equals("parameters"))
			{
				parseParameters(customDataView.getParameters(), element);
			}
			else if (nodeName.equals("statements"))
			{
				parseStatements(customDataView.getStatements(), element);
			}
		}
		return customDataView;
	}
	
	private static void parseParameters(Set<String> parameters,
			Element parentElement)
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
				parameters.add(name);
			}
		}

	}
	
	private static void parseStatements(List<StatementDefinition> list,
			Element parentElement)
	{
		DOMElementIterator statementNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (statementNodeIterator.hasNext())
		{
			Element element = statementNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("statement"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String statement = element.getAttributes()
						.getNamedItem("statement").getTextContent();
				boolean debugMode = Boolean.parseBoolean(element
						.getAttributes().getNamedItem("debugMode")
						.getTextContent());

				Node persistModeNode = element.getAttributes().getNamedItem(
						"persistMode");

				PersistModes persistMode = PersistModes.NONE;

				if (persistModeNode != null)
				{
					persistMode = PersistModes.valueOf(persistModeNode
							.getTextContent().toUpperCase());
				}

				
				list.add(new StatementDefinitionImpl(name, statement,
						persistMode, null, debugMode));
			}
		}
	}

	

}
