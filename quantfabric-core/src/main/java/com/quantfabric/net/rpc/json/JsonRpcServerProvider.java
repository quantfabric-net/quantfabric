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
package com.quantfabric.net.rpc.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.quantfabric.net.rpc.RpcServer;

public class JsonRpcServerProvider extends RpcServer
{
	private static final Logger logger = LoggerFactory.getLogger(JsonRpcServerProvider.class);
	
	private final JsonRpcServer jsonRpcServer;
	
	public JsonRpcServerProvider(Object service)
	{
		super(service);
		jsonRpcServer = new JsonRpcServer(getService());
	}
	
	@Override
	public boolean handle(InputStream input, OutputStream output)
	{
		try
		{
			ByteArrayOutputStream in = new ByteArrayOutputStream(); 

			int b = 0;
			StringBuilder sb = new StringBuilder("In:");
			while ((b = input.read()) != -1) 	
			{					
				in.write(b);
				sb.append((char) b);
			}
			logger.debug(sb.toString());
			
			ByteArrayOutputStream out = new ByteArrayOutputStream(); 
			
			jsonRpcServer.handle(new ByteArrayInputStream(in.toByteArray()), out);
						
			output.write(out.toByteArray());
			logger.debug("Out:" + out);
			out.close();
			in.close();
		}
		catch (Exception e)
		{
			logger.error("Exception during handle request.", e);
			return false;
		}
		return true;
	}

}
