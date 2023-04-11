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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ProxyTransceiver implements Transceiver
{
	private PipedInputStream input;
	private OutputStream output;
	private boolean isReady;
	
	@Override
	public boolean isReadyToTransmit()
	{
		return isReady;
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

	@Override
	public InputStream getInput() throws Exception
	{			
		return input;
	}

	@Override
	public void endInput() throws Exception
	{
		input.close();				
	}

	@Override
	public void connect() throws Exception
	{			
		input = new PipedInputStream();		
		output = new PipedOutputStream(input);
		isReady = true;
	}

	@Override
	public void disconnect() throws Exception
	{
		isReady = false;
		endInput();
		endOutput();	
	}
	
}
