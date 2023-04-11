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
package com.quantfabric.algo.server.configuration;

import java.io.Serializable;
import java.util.Properties;

public class ServiceConfiguration implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4127656266129830925L;
	private final String srvName;
	private final String srvAlias;
	private final String className;
	private final Properties properties;
	
	public ServiceConfiguration(String name,String alias,String className,Properties props){
		this.srvName = name;
		this.srvAlias = alias;
		this.className = className;
		this.properties = props;
	}

	public String getSrvName() {
		return srvName;
	}

	public String getSrvAlias() {
		return srvAlias;
	}

	public String getClassName() {
		return className;
	}

	public Properties getInitArgs(){
		return properties;
	}


}
