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

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.io.Output;
import com.quantfabric.net.Transmitter;
import com.quantfabric.net.stream.Serializer;
import com.quantfabric.net.stream.StreamServer;

public class KryoStreamServer extends StreamServer
{
	public static class KryoSerializer extends KryoSupport implements Serializer
	{
		public KryoSerializer()
		{
			super();			
		}
		
		@Override
		public byte[] serialize(Object bean)
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			Output output = new Output(buffer);			
			getKryo().writeClassAndObject(output, bean);
			byte[] result = output.toBytes();
			output.close();
			
			return result;
		}
	}
	
	public KryoStreamServer(Transmitter transmitter)
	{
		this(new KryoSerializer(), transmitter);
	}
	
	private KryoStreamServer(KryoSerializer serializer, Transmitter transmitter)
	{
		super(serializer, transmitter);
	}
}
