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

import java.util.LinkedList;

public class ParametersListView extends ParametersView{
	
	private final String columnName;
	
	public ParametersListView(String name, String id, String columnName, String rootElementName) {
		super(name, id, rootElementName);
		this.columnName = columnName;
	}
	public String getColumnName()
	{
		return columnName;
	}
	@Override
	LinkedList<String> getColumns() {
		LinkedList<String> list = new LinkedList<String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 6470964331200287566L;

			{
				add(columnName);
			}
		};
		return list;
	}
	@Override
	String getColumnName(String groupId) {
		return columnName;
	}
	
}
