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

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ParametersMatrixView extends ParametersView{
	
	LinkedHashMap<String,String> groups = new LinkedHashMap<String, String>();
	
	public ParametersMatrixView(String name, String id, String rootElementName) {
		super(name, id, rootElementName);
	}
	public void addGroup(String groupId, String columnName)
	{
		groups.put(groupId, columnName);
	}
	public String getColumnNameByGroupId(String groupId)
	{
		if(groups.containsKey(groupId))
			return groups.get(groupId);
		else return null;
	}
	@Override
	LinkedList<String> getColumns() {
		LinkedList<String> list = new LinkedList<String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6158966920083473425L;

			{
				for(Object value : groups.values())
					add(value.toString());
			}
		};
		return list;
	}
	@Override
	String getColumnName(String groupId) {
		if(groups.containsKey(groupId))
			return groups.get(groupId);
		else
			return null;
	}
}
