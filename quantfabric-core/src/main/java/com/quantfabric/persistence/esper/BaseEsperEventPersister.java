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
package com.quantfabric.persistence.esper;

import com.espertech.esper.client.EventBean;
import com.quantfabric.persistence.DataAdapter.AdaptationException;
import com.quantfabric.persistence.StorageProvider;
import com.quantfabric.persistence.StorageProvider.StoragingException;

public class BaseEsperEventPersister implements EsperEventPersister
{
	private final StorageProvider storage;
	
	public BaseEsperEventPersister(StorageProvider storage)
	{
		this.storage = storage;
	}
	
	public void persistEvent(EventBean eventBean) throws StoragingException
	{
		try
		{
			storage.store(storage.getDataApdapter().adapt(eventBean));
		}
		catch (AdaptationException e)
		{
			throw new StoragingException(e);
		}	
	}

	public void dispose() throws StoragingException
	{
		storage.dispose();		
	}

	@Override
	protected void finalize() throws Throwable
	{
		try
		{
			dispose();
		}
		finally
		{
			super.finalize();
		}
	}
}