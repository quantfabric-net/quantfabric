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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.net.Transmitter;

public class ZMQTransmitter implements Transmitter
{
	private static final Logger logger = LoggerFactory.getLogger(ZMQTransmitter.class);
	public static class ZMQOutputStream extends BufferedOutputStream
	{
		private final ZMQ.Socket socket;

		public ZMQOutputStream(ZMQ.Socket socket)
		{
			super(new ByteArrayOutputStream());
			this.socket = socket;
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			socket.send(b, 0);
		}

		@Override
		public void close() throws IOException
		{
			super.close();
			socket.close();			
		}
	}
	
	private boolean readyToTransmit = false;
	private final OutputStream output;
	
	public ZMQTransmitter(int port)	
	{
		try (ZContext context = new ZContext()) {
			try (ZMQ.Socket socket = context.createSocket(ZMQ.STREAMER)) {
				logger.info("Binding tcp://*: {}", port);
				socket.bind("tcp://*:" + port);

				this.output = new ZMQOutputStream(socket);

				readyToTransmit = true;
			}
		}
	}
	
	
	@Override
	public boolean isReadyToTransmit()
	{
		return readyToTransmit;
	}

	@Override
	public OutputStream getOutput() throws Exception
	{
		return output;
	}

	@Override
	public void endOutput() throws Exception
	{		
		output.flush();
		output.close();
	}

}
