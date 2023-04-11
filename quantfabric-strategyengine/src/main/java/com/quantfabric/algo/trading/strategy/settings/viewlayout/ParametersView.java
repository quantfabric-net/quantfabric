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

import java.util.HashMap;
import java.util.LinkedList;

public abstract class ParametersView {
	
	private String name;
	private String id;
	private final String rootElementName;
	
	public ParametersView(String name, String id, String rootElementName)
	{
		this.setName(name);
		this.setId(id);
		this.rootElementName = rootElementName;
	}
	public HashMap<String,String> getRootElementAttributes()
	{
		HashMap<String,String> list = new HashMap<String,String>(){
			/**
			 * 
			 */
			private static final long serialVersionUID = 916803576849469191L;

			{
				put("ID", getId());
			}
		};
		return list;
	}
	public String getRootElementName()
	{
		return rootElementName;
	}
	
	abstract LinkedList<String> getColumns();
	abstract String getColumnName(String groupId);
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
}
