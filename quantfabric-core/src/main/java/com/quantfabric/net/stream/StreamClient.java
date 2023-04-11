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

import com.quantfabric.net.Receiver;

public class StreamClient implements TypeRegistrator
{
	private final Receiver receiver;
	private InputStreamObjectReader deserializer;
	
	public StreamClient(InputStreamObjectReaderFactory readerFactory, Receiver receiver)
	{
		super();		
		this.receiver = receiver;
		
		try
		{
			this.deserializer = readerFactory.create(receiver.getInput());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Event read() throws Exception
	{		
		Object readedObject = deserializer.read();
		return (Event) readedObject;		
	}
	
	public void stop() throws Exception
	{
		receiver.endInput();
	}

	@Override
	public void registerType(String name, Class<?> type)
	{
		deserializer.registerType(name, type);		
	}
}
