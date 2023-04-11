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
package com.quantfabric.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpClient implements Transceiver
{
	private final InetSocketAddress serverAddress;
	private Socket connectionSocket = null;
	
	public TcpClient(InetSocketAddress serverAddress)
	{
		this.serverAddress = serverAddress;
	}

	@Override
	public void connect() throws IOException
	{
		if (connectionSocket == null || connectionSocket.isClosed())
		{
			connectionSocket = new Socket();
			connectionSocket.setReuseAddress(true);
		}
		
		if (!connectionSocket.isConnected())
			connectionSocket.connect(serverAddress);		
	}

	@Override
	public void disconnect() throws IOException
	{
		if (!connectionSocket.isClosed())
		{
			connectionSocket.close();
		}
	}
	
	@Override
	public InputStream getInput() throws IOException
	{
		return connectionSocket.getInputStream();
	}

	@Override
	public OutputStream getOutput() throws IOException
	{
		return connectionSocket.getOutputStream();
	}

	@Override
	public void endOutput() throws IOException
	{
		connectionSocket.shutdownOutput();		
	}

	@Override
	public void endInput() throws IOException
	{
		connectionSocket.shutdownInput();			
	}

	@Override
	public boolean isReadyToTransmit()
	{
		if (connectionSocket == null || connectionSocket.isClosed())
			return false;
		
		return connectionSocket.isConnected();
	}

	public void setTimeout(int timeout) throws Exception
	{
		connectionSocket.setSoTimeout(timeout);
		
	}
}
