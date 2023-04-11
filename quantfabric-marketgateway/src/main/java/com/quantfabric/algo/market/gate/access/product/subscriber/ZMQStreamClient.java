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
package com.quantfabric.algo.market.gate.access.product.subscriber;

import java.nio.channels.ClosedByInterruptException;

import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.gateway.access.product.Connector;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;
import com.quantfabric.algo.market.gate.access.product.publisher.ZMQPublisher;
import com.quantfabric.net.Receiver;
import com.quantfabric.net.stream.Event;
import com.quantfabric.net.stream.StreamClient;
import com.quantfabric.net.stream.TypeRegistrator;
import com.quantfabric.net.stream.ZMQReceiver;
import com.quantfabric.net.stream.kryo.KryoStreamClient;

public class ZMQStreamClient implements Connector
{
	private Logger logger; 
	private Subscriber subscriber;
	
	private Receiver receiver;
	private StreamClient streamClient;	
	private Thread streamReader;
	
	private boolean isClosed;
	
	public ZMQStreamClient()
	{
		this.isClosed = false;
	}

	@Override
	public void connect(PublisherAddress endpoint, Subscriber subscriber)
			throws Exception
	{
		if (isClosed)
			throw new Exception("Couldn't connect because connector closed.");
		
		this.logger = LoggerFactory.getLogger(String.format("StreamReader[%s:%d/%s]", 
				endpoint.getHost(), endpoint.getPort(), endpoint.getContentType().toString()));
		
		this.subscriber = subscriber;
		
		this.receiver = new ZMQReceiver(endpoint.getHost(), endpoint.getPort());
		this.streamClient = new KryoStreamClient(receiver);
		
		registerTypes(streamClient, endpoint.getContentType());
		
		this.streamReader = new Thread(
			new Runnable() 
			{				
				@Override
				public void run()
				{
					try {
						while (!Thread.currentThread().isInterrupted())
						{
							Event event = null;
							
							try
							{
								event = streamClient.read();
							}
							
							catch (ClosedByInterruptException e) {
								
								logger.info("Reading is interrupted.");
								continue;
							}
							catch (Exception e)
							{
								logger.error("Read event failed.", e);
								continue;
							}
							
							if (event != null && ZMQStreamClient.this.subscriber != null)
								ZMQStreamClient.this.subscriber.update(event);					
						}
					}
					finally {
						try
						{
							streamClient.stop();	
							streamClient = null;
						}
						catch (Exception e)
						{
							logger.error("Can't stop stream client.", e);
						}
					}					
				}
			});

		streamReader.setDaemon(true);
		streamReader.start();
	}
	
	@Override
	public void close() throws Exception
	{
		if (streamReader != null && !streamReader.isInterrupted())
			streamReader.interrupt();	
		
		//if (streamReader != null)
			streamReader.join(1000);
		
		isClosed = true;
		
		subscriber = null;
		receiver = null;
		//streamClient = null;
		streamReader = null;
		//logger = null;	
	}
	
	private static void registerTypes(TypeRegistrator typeRegistrator,
			ContentType contentType)
	{
		ZMQPublisher.registerTypes(typeRegistrator, contentType);	
	}	
}
