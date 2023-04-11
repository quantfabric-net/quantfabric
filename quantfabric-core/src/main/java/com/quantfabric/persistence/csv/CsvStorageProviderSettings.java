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
package com.quantfabric.persistence.csv;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.quantfabric.persistence.StorageProviderSettings;

public class CsvStorageProviderSettings extends StorageProviderSettings
{
	public static final int PARTITION_NOT_REQUIRED = 0;
	public static final int DEFAULT_PARTITION_SIZE = PARTITION_NOT_REQUIRED;
	
	private String pathToStorage = null;
	private int partitionSize = DEFAULT_PARTITION_SIZE;

	public String getPathToStorage()
	{
		return pathToStorage;
	}

	public void setPathToStorage(String pathToStorage)
	{
		this.pathToStorage = appendPostfixIfNeeded(pathToStorage);
	}

	private String appendPostfixIfNeeded(String pathToStorage)
	{
		String postfix = 
			System.getProperty("com.quantfabric.persistence.csv.storage-name-postfix");
		if (postfix != null)
			return pathToStorage.concat(postfix);
		else
			return pathToStorage;
	}

	public int getPartitionSize()
	{
		return partitionSize;
	}

	public void setPartitionSize(int partitionSize)
	{
		this.partitionSize = partitionSize;
	}

	@Override
	public Map<String, String> getSettingsInfo()
	{
		HashMap<String, String> settingsInfo = new HashMap<String, String>();
		settingsInfo.put("pathToStorage", getPathToStorage());
		settingsInfo.put("partitionSize", String.valueOf(getPartitionSize()));
		return null;
	}

	@Override
	protected void initialize(Properties properties)
	{
		try
		{
		for (Map.Entry<Object, Object> property: properties.entrySet())
		{
			if (property.getKey().equals("pathToStorage"))
			{
				setPathToStorage(String.valueOf(property.getValue()));
			}
			if (property.getKey().equals("partitionSize"))
			{
				setPartitionSize(Integer.parseInt(String.valueOf(property.getValue())));
			}
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
