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

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.wizard.DisruptorWizard;

public class NativeSubscriberBuffer implements Subscriber<Object>
{
	private static final Logger log = LoggerFactory.getLogger(NativeSubscriberBuffer.class);
	
	private final Subscriber<Object> subscriber;
	private final Executor executor;
	private final DisruptorWizard<ObjectValueEvent> disruptorWizard;
	private RingBuffer<ObjectValueEvent> ringBuffer;
	
	public NativeSubscriberBuffer(String name, Subscriber<Object> subscriber)
	{
		this(name, subscriber, 
				Integer.parseInt(System.getProperty("com.quantfabric.fwk.pubsub.subscriber-buffer-size",
						"1000")));
	}
	
	@SuppressWarnings("unchecked")
	public NativeSubscriberBuffer(final String name, Subscriber<Object> subscriber, final int queueCapacity)
	{				
		this.subscriber = subscriber;
		
		executor = new Executor() {
			
			@Override
			public void execute(Runnable r)
			{
				Thread t = new Thread(r, String.format("SubscriberBufferThread%s-%s",
						(queueCapacity == Integer.MAX_VALUE ? "" : "-" + queueCapacity), name));
				t.setDaemon(true);
				t.start();				
			}
		}; 
		
		disruptorWizard = new DisruptorWizard<ObjectValueEvent>(
					ObjectValueEvent.EVENT_FACTORY, queueCapacity, executor);
		
		disruptorWizard.handleEventsWith(
				new EventHandler<ObjectValueEvent>()
				{
					@Override
					public void onEvent(ObjectValueEvent event,
							boolean endOfBatch) throws Exception
					{
						publish(event.getValue());						
					}			
				});		
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		dispose();
		super.finalize();
	}

	public void dispose()
	{
		stop();
	}
	
	public void start()
	{
		ringBuffer = disruptorWizard.start();
	}
	
	public void stop()
	{
		disruptorWizard.halt();
		ringBuffer = null;
	}
	
	@Override
	public void sendUpdate(Object data)
	{
		if (ringBuffer != null)
		{
			ObjectValueEvent event = ringBuffer.nextEvent();
			event.setValue(data);
			ringBuffer.publish(event);
		}
		else
			log.error("SubscriberBuffer doesn't started");
	}

	@Override
	public void sendUpdate(Object[] data)
	{
		if (ringBuffer != null)
		{
			for (int i = 0; i < data.length; i++)
				sendUpdate(data[i]);
		}
		else
			log.error("SubscriberBuffer doesn't started");
	}

	private void publish(Object item)
	{
		try
		{
			subscriber.sendUpdate(item);

		}
		catch (Exception e)
		{
			log.error("sendUpdate error", e);
		}
	}
}
