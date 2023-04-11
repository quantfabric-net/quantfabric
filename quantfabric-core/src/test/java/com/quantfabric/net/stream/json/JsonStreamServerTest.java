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
package com.quantfabric.net.stream.json;

import com.quantfabric.net.Transmitter;
import com.quantfabric.net.stream.Server;
import com.quantfabric.net.stream.ZMQTransmitter;

public class JsonStreamServerTest
{
	public static void main (String[] args) throws Exception 
	{
		Transmitter transmitter = new ZMQTransmitter(7777);
		JsonStreamServer streamServer = new JsonStreamServer(transmitter);
		
		Server server = new Server(transmitter, streamServer);
		server.start();

		System.out.println("Press any ket to exit.");
		System.in.read();
	}
}
