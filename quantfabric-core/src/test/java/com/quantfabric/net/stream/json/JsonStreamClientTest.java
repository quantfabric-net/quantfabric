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

import com.quantfabric.net.Receiver;
import com.quantfabric.net.stream.Client;
import com.quantfabric.net.stream.ZMQReceiver;

public class JsonStreamClientTest
{
	public static void main(String[] args) throws Exception
	{
		Receiver receiver = new ZMQReceiver("localhost", 7777);		
		JsonStreamClient streamClient = new JsonStreamClient(receiver);
		
		Client client = new Client(streamClient, false);
		client.start();
	}
}
