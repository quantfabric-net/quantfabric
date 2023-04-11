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
package com.quantfabric.algo.trading.execution;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;

import com.quantfabric.net.TcpClient;
import com.quantfabric.net.stream.Event;
import com.quantfabric.net.stream.StreamClient;
import com.quantfabric.net.stream.kryo.KryoStreamClient;

public class KryoAlgoServerListener
{
	
		public static StreamClient create(String host, int port, String unit, String sourceName) throws Exception
		{
			TcpClient tcpClient = new TcpClient(
					new InetSocketAddress(host, port));
			
			tcpClient.connect();
			
			DataOutputStream outToServer = new DataOutputStream(tcpClient.getOutput());
			
			outToServer.writeBytes(String.format("StrategyDataStreamingRequest(unitName=\"%s\", strategyName=\"%s\")" + "\n",
					unit, sourceName));
								
			KryoStreamClient streamClient = new KryoStreamClient(tcpClient);
			streamClient.registerType("Event", Event.class);
			StrategyEventsStreamer.registerUsingTypes(streamClient);
			
			return streamClient;
		}

	
	public static void main(String[] args) throws Exception
	{
								
		StreamClient streamClient = create("localhost", 4123, "AlgoHost(actSynth)", "TRADEAR FX");
				
		while(true)
		{
			System.out.println(streamClient.read());
		}
		
	}
}
