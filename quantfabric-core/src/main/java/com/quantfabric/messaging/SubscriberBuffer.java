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
package com.quantfabric.messaging;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriberBuffer<T> implements NamedMapSubscriber<T>
{
	private static class DataItem<T>
	{
		private final boolean isBean;
		private T dataBean;
		private Map<?, ?> dataMap;
		private String mapTypeName;
		
		public boolean isBean() {
			return isBean;
		}
		public T getDataBean() {
			return dataBean;
		}
		public Map<?, ?> getDataMap() {
			return dataMap;
		}
		public String getMapTypeName() {
			return mapTypeName;
		}
				
		public DataItem(Map<?, ?> dataMap, String mapTypeName) {
			this.isBean=false;
			this.dataMap = dataMap;
			this.mapTypeName = mapTypeName;
		}
		
		public DataItem(T dataBean) {
			this.isBean=true;
			this.dataBean = dataBean;
		}		
	}
	
	private static final Logger log = LoggerFactory.getLogger(SubscriberBuffer.class);
	
	private final NativeSubscriberBuffer buffer;
	private final Subscriber<T> subscriber;
	
	private final String name;
	
	public SubscriberBuffer(String name, Subscriber<T> subscriber) 
	{
		this.name = name;
		
		int queueCapacity = 
			Integer.parseInt(System.getProperty("com.quantfabric.fwk.pubsub.subscriber-buffer-size",
					"1000"));
		
		this.subscriber = subscriber;
		
		Subscriber<Object> objectSubscriber = new Subscriber<Object>() 
		{			
			@SuppressWarnings("unchecked")
			@Override
			public void sendUpdate(Object data)
			{
				publish((DataItem<T>) data);				
			}
			
			@Override
			public void sendUpdate(Object[] data)
			{
				for (Object obj : data)
					sendUpdate(obj);
			}			
		};
		
		buffer = new NativeSubscriberBuffer(name, objectSubscriber, queueCapacity);
		
		buffer.start();
	}
	
	
	@Override
	public void sendUpdate(T data) 
	{
		enqueue(new DataItem<T>(data));
	}
	
	@Override
	public void sendUpdate(T[] data) 
	{
		for (int i = 0; i < data.length; i++) 
		{
			enqueue(new DataItem<T>(data[i]));
		}
	}
	@Override
	public void update(@SuppressWarnings("rawtypes") Map data, String dataTypeName) 
	{
		enqueue(new DataItem<T>(data,dataTypeName));
	}
	
	@Override
	public void update(@SuppressWarnings("rawtypes") Map[] data, String dataTypeName) 
	{
		for (int i = 0; i < data.length; i++) 
		{
			enqueue(new DataItem<T>(data[i],dataTypeName));
		}
		
	}	
	
	private void enqueue(DataItem<T> eventBean)
	{
		buffer.sendUpdate(eventBean);
	}
	
	private void publish(DataItem<T> item)
	{
		try
		{
			if(item.isBean())
				subscriber.sendUpdate(item.getDataBean());
			else
				((NamedMapSubscriber<T>)subscriber).update(
						item.getDataMap(), item.getMapTypeName());
		}
		catch (Exception e)
		{
			log.error("name - " + name , e);
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		dispose();
		super.finalize();
	}


	public void dispose()
	{
		buffer.dispose();
	}
	
}
