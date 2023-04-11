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
package com.quantfabric.algo.market.gate.access;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.locks.LockSupport;

import com.quantfabric.algo.market.gate.access.product.publisher.ZMQPublisher;
import com.quantfabric.algo.market.gate.access.product.subscriber.ZMQStreamClient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.quantfabric.algo.market.gateway.access.product.Connector;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.net.stream.Event;

public class ZMQStreamTest
{
	private static final PublisherAddress pubAddr1 = new PublisherAddress("localhost", 10001, ContentType.TOP_OF_BOOK);
	private static Publisher pub1;

	@BeforeClass
	public static void startPublishers()
	{
		pub1 = new ZMQPublisher("pub-1", pubAddr1.getHost(), pubAddr1.getPort(), pubAddr1.getContentType());
		
	}
	
	private Object lastEvent = null;
	private final Thread rootThread = Thread.currentThread();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void connectivity() throws Exception
	{
		Subscriber sub1 = new Subscriber() {
			
			@Override
			public void update(Event event)
			{
				lastEvent = event.getEventBean();	
				LockSupport.unpark(rootThread);
			}
		};
		
		Connector con1 = new ZMQStreamClient();
		
		con1.connect(pubAddr1, sub1);	
		
		pub1.publish("Test Message");		
		LockSupport.park();		
		
		assertEquals("Test Message", lastEvent);		
		
		con1.close();
		
		exception.expect(Exception.class);
		exception.expectMessage("Couldn't connect because connector closed.");
		con1.connect(pubAddr1, sub1);	
		
		con1 = new ZMQStreamClient();
		
		pub1.publish("Test Message 2");		
		LockSupport.park();		
		
		assertEquals("Test Message 2", lastEvent);		
		
		con1.close();
	}

}
