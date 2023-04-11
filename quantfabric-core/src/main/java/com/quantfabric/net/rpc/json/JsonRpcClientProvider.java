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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcClient;
import com.quantfabric.net.Transceiver;
import com.quantfabric.net.rpc.RpcClient;

public class JsonRpcClientProvider extends RpcClient
{
	public static class JsonRpcAdvClient extends JsonRpcClient
	{
		private static final Logger logger = LoggerFactory.getLogger(JsonRpcAdvClient.class);
		private final Transceiver transceiver;
		
		public JsonRpcAdvClient(Transceiver transceiver)
		{
			this.transceiver = transceiver;
		}
		
		
		public Object invoke(String methodName, Object[] arguments,
				Type returnType) throws Exception
		{			
			try
			{
				synchronized (transceiver)
				{					
					transceiver.connect();
					
					InputStream input = transceiver.getInput();
					OutputStream output = transceiver.getOutput();
					
					ByteArrayOutputStream out = new ByteArrayOutputStream(); 
					
					super.invoke(methodName, arguments, out);
					
					output.write(out.toByteArray());
					transceiver.endOutput();
					logger.debug("Out:" + out);
					
					ByteArrayOutputStream in = new ByteArrayOutputStream(); 
		
					StringBuilder sb = new StringBuilder("In:");
					int b = 0;
					while ((b = input.read()) != -1) 
					{
						in.write(b);
						sb.append((char)b);
					}
					logger.debug(sb.toString());
					transceiver.endInput();
					
					Object result = null;
					
					if (returnType != Void.TYPE)
					{
						try
						{
							result = super.readResponse(returnType, new ByteArrayInputStream(in.toByteArray()));
						}
						catch (Throwable e)
						{
							logger.error(String.format("read response failed (method:%s, args:%s)", methodName, ArrayUtils.toString(arguments, "null")), e);
							result = null;
						}
					}
					
					out.close();
					in.close();		
					return result;
				}
			}
			finally
			{
				transceiver.disconnect();
			}
			
		}
	}

	
	private final JsonRpcAdvClient jsonRpcClient;

	public JsonRpcClientProvider(Transceiver transceiver)
	{
		super(transceiver);
		jsonRpcClient = new JsonRpcAdvClient(transceiver);
	}

	@Override
	public <T> T getProxyObject(Class<T> proxyInterface) throws Exception
	{
		return createProxy(getClass().getClassLoader(), proxyInterface,
				jsonRpcClient);
	}

	@SuppressWarnings("unchecked")
	private static <T> T createProxy(ClassLoader classLoader,
			Class<T> proxyInterface, final JsonRpcAdvClient client)
	{

		// create and return the proxy
		return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class<?>[] { proxyInterface }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable
					{
						return client.invoke(method.getName(), args,
								method.getGenericReturnType());
					}
				});
	}
}
