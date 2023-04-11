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
package com.quantfabric.net.stream.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import com.esotericsoftware.kryo.io.Input;
import com.quantfabric.net.Receiver;
import com.quantfabric.net.stream.Deserializer;
import com.quantfabric.net.stream.InputStreamObjectReader;
import com.quantfabric.net.stream.InputStreamObjectReaderFactory;
import com.quantfabric.net.stream.StreamClient;

public class KryoStreamClient extends StreamClient
{	
	public static class KryoDeserializer extends KryoSupport implements Deserializer
	{	
		public KryoDeserializer()
		{
			super();
		}
		
		@Override
		public Object deserialize(byte[] data)
		{			
			return deserialize(ByteBuffer.wrap(data));
		}

		public Object deserialize(InputStream input)
		{												
			return deserialize(Channels.newChannel(input));									
		}
		
		public Object deserialize(ReadableByteChannel inputChanel)
		{						
			ByteBuffer buffer = ByteBuffer.allocateDirect(40960);
			try
			{
				inputChanel.read(buffer);
				buffer.flip();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
			
			return deserialize(buffer);	
		}
		
		public Object deserialize(ByteBuffer buffer)
		{
			
			byte[] array = new byte[buffer.remaining()];
			buffer.get(array);
			
			return getKryo().readClassAndObject(new Input(array));
		}	
	}
	
	public static class KryoInputStreamObjectReader implements InputStreamObjectReader
	{
		private final KryoDeserializer deserializer = new KryoDeserializer();
		private final ReadableByteChannel channel;
		private final InputStream input;
		private final ByteBuffer buffer;
		
		public KryoInputStreamObjectReader(InputStream input)
		{
			this.input = input;	
			this.channel = Channels.newChannel(this.input);
			this.buffer = ByteBuffer.allocateDirect(40960);
		}
		
		@Override
		public void registerType(String name, Class<?> type)
		{
			deserializer.registerType(name, type);			
		}

		@Override
		public Object read() throws IOException
		{
			channel.read(buffer);
			
			buffer.flip();
			Object obj = deserializer.deserialize(buffer);
			buffer.compact();
			return obj;
		}
		
	}
	
	public KryoStreamClient(Receiver receiver)
	{
		this(new KryoDeserializer(), receiver);		
	}
	
	private KryoStreamClient(KryoDeserializer deserializer, Receiver receiver)
	{
		super(createInputStreamObjectReaderFactory(), receiver);
	}

	private static InputStreamObjectReaderFactory createInputStreamObjectReaderFactory()
	{
		return new InputStreamObjectReaderFactory() {
			
			@Override
			public InputStreamObjectReader create(InputStream input)
			{
				return new KryoInputStreamObjectReader(input);
			}
		};
	}	
	
}
