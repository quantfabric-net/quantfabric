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

import java.util.GregorianCalendar;

import com.quantfabric.net.Transmitter;

public class StreamServer implements TypeRegistrator
{
	private final Serializer serializer;
	private final Transmitter transmitter;
	
	public StreamServer(Serializer serializer, Transmitter transmitter)
	{
		this.serializer = serializer;
		this.transmitter = transmitter;
	}
	
	public void send(Event event) throws Exception
	{
		synchronized (transmitter)
		{
			
			if (transmitter.isReadyToTransmit()) try
			{
				event.setSendingTime(GregorianCalendar.getInstance().getTime().getTime());
				transmitter.getOutput().write(serializer.serialize(event));
				transmitter.getOutput().flush();
			}
			catch (Exception e)
			{			
				e.printStackTrace();
				transmitter.endOutput();
			}
		}
	}

	@Override
	public void registerType(String name, Class<?> type)
	{
		serializer.registerType(name, type);		
	}
}
