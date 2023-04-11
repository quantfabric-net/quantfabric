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
package com.quantfabric.persistence;

import java.util.Properties;

import org.w3c.dom.Node;

import com.quantfabric.util.XMLConfigParser;

public abstract class StorageProviderSettings implements PersisterSettingsBlock
{
	public static StorageProviderSettings getFromXML(
			Class<? extends StorageProviderSettings> settingsClass,
			Node storageProviderSettingsNode)
	{
		if (storageProviderSettingsNode != null && 
			storageProviderSettingsNode.getNodeName().equals("storageProvider-settings"))
		{
			Properties properties = 
				XMLConfigParser.parseSettingsNode(storageProviderSettingsNode);
			
			if (!properties.isEmpty())
			{
				try
				{
					StorageProviderSettings settingsObject = 
						settingsClass.getConstructor().newInstance();
					settingsObject.initialize(properties);
					return settingsObject;
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		return null;		
	}
	
	protected abstract void initialize(Properties properties);
}
