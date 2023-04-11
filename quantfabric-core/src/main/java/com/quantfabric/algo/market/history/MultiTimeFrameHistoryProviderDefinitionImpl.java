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
package com.quantfabric.algo.market.history;

import java.util.Properties;

public class MultiTimeFrameHistoryProviderDefinitionImpl implements MultiTimeFrameHistoryProviderDefinition
{
	private final Class<? extends MultiTimeFrameHistoryProviderFactory> factroyClass;
	
	private final String storage;
	private final Properties properties;
	private final String name;
	
	public MultiTimeFrameHistoryProviderDefinitionImpl(String name, String factoryClass, String storage, Properties properties) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{		
		factroyClass = Class.forName(factoryClass).asSubclass(MultiTimeFrameHistoryProviderFactory.class);
		this.storage = storage;
		this.properties = properties;
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.history.MultiT#getFactoryClass()
	 */
	@Override
	public Class<? extends MultiTimeFrameHistoryProviderFactory> getFactoryClass()
	{
		return factroyClass;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.history.MultiT#getStorage()
	 */
	@Override
	public String getStorage()
	{
		return storage;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.history.MultiT#getProperties()
	 */
	@Override
	public Properties getProperties()
	{
		return properties;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.history.MultiT#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

}
