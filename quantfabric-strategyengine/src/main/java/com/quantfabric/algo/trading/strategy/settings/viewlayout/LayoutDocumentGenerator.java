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
package com.quantfabric.algo.trading.strategy.settings.viewlayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.quantfabric.algo.trading.strategy.settings.StrategySetting;

public class LayoutDocumentGenerator implements LayoutDefinitionProvider{

	private final LayoutDefinition layoutDefinition;
	private final Map<String, StrategySetting> strategySettings;
	private Document document;
	
	public LayoutDocumentGenerator(LayoutDefinition layoutDefinition, Map<String, StrategySetting> strategySettings)
	{
		this.layoutDefinition = layoutDefinition;
		this.strategySettings = strategySettings;
		document = generateLayoutDefinitionDocument();
	}
	
	@Override
	public Document getLayoutDefinition() {
		
		return document;
	}
	private Document generateLayoutDefinitionDocument()
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.newDocument();
		}
		catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		Element rootElem = document.createElement("settingsGUILayout");
		document.appendChild(rootElem);
		
		ArrayList<ParametersView> parametersViews = layoutDefinition.getParametersViews();
		//stores <viewId,Rows Element>
		Map<String, Element> rowsElementsMap = new LinkedHashMap<String, Element>();
		for(ParametersView view : parametersViews)
		{
			//create root element for paramsView
			Element viewRootElem = document.createElement(view.getRootElementName());
			HashMap<String, String> rootElemAttributes = view.getRootElementAttributes();
			//set root element attributes
			for (Map.Entry<String, String> entry : rootElemAttributes.entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();
			    viewRootElem.setAttribute(key, value);
			}
			//create columns element
			Element columns = document.createElement("Columns");
			List<String> columnNames = view.getColumns();
			//add column elements to columns element
			for(String columnName : columnNames)
			{
				Element column = document.createElement("Column");
				Element colName = document.createElement("Name");
				colName.appendChild(document.createTextNode(columnName));
				column.appendChild(colName);
				columns.appendChild(column);
			}
			viewRootElem.appendChild(columns);
			//create Rows element
			Element rows = document.createElement("Rows");
			rowsElementsMap.put(view.getId(), rows);
			viewRootElem.appendChild(rows);
			rootElem.appendChild(viewRootElem);
		}
		//stores viewId and set of <RowName,RowElement>
		Map<String, Map<String, Element>> rowNamesRowElementMap = new LinkedHashMap<String, Map<String,Element>>();
		for(Map.Entry<String, StrategySetting> entry : strategySettings.entrySet())
		{
			StrategySetting setting = entry.getValue();
			String parametersViewId = setting.getParametersViewId();
			if(rowsElementsMap.containsKey(parametersViewId)){
				String rowName = setting.getDisplayName();
				if(!rowNamesRowElementMap.containsKey(parametersViewId))
					rowNamesRowElementMap.put(parametersViewId, new LinkedHashMap<String, Element>());
				Map<String, Element> rowNameMap = rowNamesRowElementMap.get(parametersViewId);
				if(!rowNameMap.containsKey(rowName))
				{
					Element row = document.createElement("Row");
					Element name = document.createElement("Name");
					name.appendChild(document.createTextNode(rowName));
					row.appendChild(name);
					rowNameMap.put(rowName, row);
				}
				Element rows = rowsElementsMap.get(parametersViewId);
				Element row = rowNameMap.get(rowName);
				String groupId = setting.getGroupId();
				ParametersView view = layoutDefinition.getParametersView(parametersViewId);
				String columnName = view.getColumnName(groupId);
				Element cell = document.createElement("Cell");
				cell.setAttribute("column", columnName);
				cell.setAttribute("propertyName", entry.getKey());
				cell.setAttribute("rowName", rowName);
				Element type = document.createElement("Type");
				type.appendChild(document.createTextNode(setting.getType()));
				Element isRuntimeEditable = document.createElement("isRuntimeEditable");
				isRuntimeEditable.appendChild(document.createTextNode(setting.getModificationMode().toString()));
				cell.appendChild(type);
				cell.appendChild(isRuntimeEditable);
				row.appendChild(cell);
				rows.appendChild(row);
			}
		}
		return document;
	}
}
