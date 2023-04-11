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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.persistence.BaseStorageProvider;

public abstract class CsvStorageProvider extends BaseStorageProvider
{
	private static final Logger log = LoggerFactory.getLogger(CsvStorageProvider.class);
	
	private final Map<String, CsvStorageWriter> storageWriters =
		new HashMap<String, CsvStorageWriter>();
	
	private final CsvStorageProviderSettings settings;
	
	public CsvStorageProvider(CsvStorageProviderSettings settings)
	{
		this.settings = settings;
	}
		
	public CsvStorageProviderSettings getSettings()
	{
		return settings;
	}

	@Override
	public void store(Object object) throws StoragingException
	{
		if (object instanceof CsvBean)
		{
			CsvBean bean = (CsvBean)object;
			try
			{
				storageWriters.get(bean.getPersistingClassName()).saveBean(bean);
			}
			catch (IOException e)
			{
				throw new StoragingException("can't store CvsBean", e);
			}
		}
	}

	@Override
	public void dispose() throws StoragingException
	{
		for (CsvStorageWriter storageWriter : storageWriters.values())
			try
			{
				storageWriter.close();
			}
			catch (IOException e)
			{
				throw new StoragingException("dispose failed", e);
			}	
		
		super.dispose();
	}

	public void newPersistingClassName(String origianlName, String newName)
	{
		if (storageWriters.containsKey(origianlName))
		{
			storageWriters.put(newName, storageWriters.get(origianlName));
		}
	}
	
	public void importMetadata(String persistingClassName, CsvMetadata metadata) throws StoragingException
	{
		CsvStorageWriter storageWriter = null;
		
		try
		{
			storageWriter = new CsvStorageWriter(settings, metadata);
		}
		catch (IOException e)
		{
			throw new StoragingException("Can't create CsvStorageWriter", e);
		}
		
		storageWriters.put(persistingClassName, storageWriter);
		
		log.info("Metadata (" + metadata.getSchemaName() +") for persisting \"" + 
				persistingClassName + "\" was imported");
	}
}
