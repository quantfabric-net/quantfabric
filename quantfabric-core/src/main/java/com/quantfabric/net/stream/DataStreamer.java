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
package com.quantfabric.net.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.messaging.NativeSubscriberBuffer;
import com.quantfabric.messaging.Subscriber;

public abstract class DataStreamer
{
	private static final Logger logger = LoggerFactory.getLogger(DataStreamer.class);
	
	private final NativeSubscriberBuffer dataBuffer ;
	private final StreamServer streamServer;
	private final String dataSource;
	
	public DataStreamer(String dataSource, StreamServer streamServer)
	{
		this.dataSource = dataSource;
		this.streamServer = streamServer;
		this.dataBuffer = new NativeSubscriberBuffer("StrategyDataStreamer-DataBuffer", createDataFilter(this));
		this.streamServer.registerType("Event", Event.class);
		this.registerTypes(streamServer);
		streamServer.registerType("Event", Event.class);
		this.dataBuffer.start();
	}
	
	protected abstract DataFilter createDataFilter(DataStreamer dataStreamer);
	protected abstract void registerTypes(TypeRegistrator typeRegistrator);

	public Subscriber<Object> getDataSubscriber()
	{
		return dataBuffer;
	}
	
	protected void push(Object data) 
	{
		Event event = new Event(dataSource, data);
		try
		{
			streamServer.send(event);
		}
		catch (Exception e)
		{
			logger.error("Transmiting of event failed", e);
		}
	}
	
	public void start()
	{
		
	}
	
	public void stop()
	{

	}
	 

}
