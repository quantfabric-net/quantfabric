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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.zeromq.ZMQ;

import com.quantfabric.net.Receiver;

public class ZMQReceiver implements Receiver
{
	public static class ZMQInputStream extends InputStream
	{
		private final ZMQ.Socket socket;
		
		private ByteArrayInputStream inputStream;
		
		private ZMQInputStream(ZMQ.Socket socket)
		{			
			super();
			this.socket = socket;
		}
		
	
		@Override
		public int read() throws IOException
		{
			if (inputStream == null)
			{				
				byte[] buffer = new byte[40980];
				int bufferSize = read(buffer);
				inputStream = new ByteArrayInputStream(buffer,0, bufferSize);				
			}
			
			int value = inputStream.read();
			if (value == -1)
			{
				inputStream.close();
				inputStream = null;
			}
			
			return value;
		}

		@Override
		public int read(byte[] clBuf) throws IOException
		{
			if (inputStream != null)
			{
				int size = inputStream.read(clBuf);
				inputStream.close();
				inputStream = null;
				return size;
			}
			byte[] buf = socket.recv(0);
            System.arraycopy(buf, 0, clBuf, 0, buf.length);
			
			return buf.length;
		}
	}
	
	private final ZMQ.Socket socket;
	private final InputStream input;
	
	public ZMQReceiver(String host, int port)
	{
		ZMQ.Context context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.PULL);
		this.socket.connect ("tcp://" + host + ":" + port);
		this.input = new ZMQInputStream(socket);
	}
	
	@Override
	public InputStream getInput() throws Exception
	{
		return input;
	}

	@Override
	public void endInput() throws Exception
	{
		input.close();
		socket.close();		
	}

}
