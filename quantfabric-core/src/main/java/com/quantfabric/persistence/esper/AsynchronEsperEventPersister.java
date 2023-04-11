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
import com.quantfabric.messaging.NativeSubscriberBuffer;
import com.quantfabric.messaging.Subscriber;
import com.quantfabric.persistence.StorageProvider;
import com.quantfabric.persistence.StorageProvider.StoragingException;

public class AsynchronEsperEventPersister extends BaseEsperEventPersister
{	
	private static final Logger log = LoggerFactory.getLogger(AsynchronEsperEventPersister.class);
		
	private static int buffersCount = 0;
	private synchronized static int getBufferNumber()
	{
		return buffersCount++;		
	}
	
	private final NativeSubscriberBuffer buffer;
	
	
	public AsynchronEsperEventPersister(StorageProvider storageProvider)
	{	
		super(storageProvider);	
	
		int queueCapacity = 
			Integer.parseInt(System.getProperty("com.quantfabric.persistence.asynchron-persister-buffer-size",
					Integer.valueOf(1000).toString()));
		
		Subscriber<Object> subscriber = new Subscriber<Object>() 
		{			
			@Override
			public void sendUpdate(Object data) {

				doPersist((EventBean) data);
			}
			@Override
			public void sendUpdate(Object[] data)
			{
				for (Object obj : data)
					sendUpdate(obj);
			}		
		};
		
		buffer = new NativeSubscriberBuffer("Persister-" +  getBufferNumber(), 
				subscriber, queueCapacity);
		
		buffer.start();
	}
		
	@Override
	protected void finalize() throws Throwable
	{
		dispose();
		super.finalize();
	}

	@Override
	public void dispose() throws StoragingException
	{
		buffer.dispose();
		super.dispose();
	}

	@Override
	public void persistEvent(EventBean eventBean)
	{
		buffer.sendUpdate(eventBean);		
	}
	
	private void doPersist(EventBean eventBean)
	{
		try
		{
			super.persistEvent(eventBean);
		}
		catch (StoragingException e)
		{
			log.error("persisting error", e);
		}
	}	
}
