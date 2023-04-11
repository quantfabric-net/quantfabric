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

import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.eventbus.BackTestingEventAdapter;
import com.quantfabric.algo.backtesting.eventbus.BackTestingEventListener;
import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.algo.runtime.QuantfabricRuntime;

public abstract class BaseStorageProvider implements StorageProvider
{	
	private final BackTestingEventListener listener =
		new BackTestingEventAdapter() 
		{					
			@Override
			public void clearPersisterStrorages(BackTestingEvent event)
			{
				try
				{
					erase();
				}
				catch (StoragingException e)
				{
					LoggerFactory.getLogger(BaseStorageProvider.this.getClass()).error("Can't erase storage.", e);
				}				
			}
		};
		
	public BaseStorageProvider()
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().attachListener(listener);				
	}
	
	@Override
	public void erase() throws StoragingException
	{
		throw new StoragingException("Erase not supported this storage.");		
	}

		
	@Override
	public void dispose() throws StoragingException
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().dettachListener(listener);		
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
