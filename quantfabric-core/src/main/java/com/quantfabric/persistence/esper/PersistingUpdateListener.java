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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.algo.runtime.ShutdownListener;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.persistence.StorageProvider.StoragingException;

public class PersistingUpdateListener implements UpdateListener, ShutdownListener
{	
	private static final Logger log = LoggerFactory.getLogger(PersistingUpdateListener.class);
	private final String name;
	
	EsperEventPersister persister;	
	public PersistingUpdateListener(String name, EsperEventPersister persister)
	{
		this.persister = persister;
		this.name = name;
		QuantfabricRuntime.addShutdownListener(this);
	}
	public void update(EventBean[] newEvents, EventBean[] oldEvents)
	{	
		if (newEvents == null)
		{
			log.debug("(" + name + ") newEvents: " + newEvents + "; oldEvents: " + oldEvents);
			return;
		}
		
		try
		{
			for (EventBean newEvent : newEvents) persister.persistEvent(newEvent);
		}
		catch (StoragingException e)
		{
			log.error("persisting error", e);
		}
	}
	
	public void dispose() throws StoragingException
	{
		log.info("dispose");
		persister.dispose();
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		log.info("finalizing");
		try
		{
			dispose();
		}
		finally
		{
			super.finalize();
		}
	}
	@Override
	public void shutdown(String sessionId)
	{
		try
		{
			finalize();
		}
		catch (Throwable e)
		{
			log.error("finalizing fail.", e);
		}		
	}
}
