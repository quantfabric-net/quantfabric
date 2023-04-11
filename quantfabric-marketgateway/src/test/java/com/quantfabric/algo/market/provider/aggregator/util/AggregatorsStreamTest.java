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
package com.quantfabric.algo.market.provider.aggregator.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.quantfabric.algo.instrument.InstrumentImpl;
import com.quantfabric.algo.market.datamodel.BaseMDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.provider.aggregator.util.AggregatorUpdatesDataReceiver.EventListener;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeedImpl;
import com.quantfabric.net.stream.Event;
import com.quantfabric.net.stream.ZMQReceiver;
import com.quantfabric.net.stream.ZMQTransmitter;
import com.quantfabric.net.stream.kryo.KryoStreamClient;
import com.quantfabric.net.stream.kryo.KryoStreamServer;


public class AggregatorsStreamTest
{
	public static void main(String[] args) 
	{
		final AggregatorUpdatesDataStreamer streamer = new AggregatorUpdatesDataStreamer("TestDS", 
				new KryoStreamServer(new ZMQTransmitter(9999)));
		
		final AggregatorUpdatesDataReceiver receiver = new AggregatorUpdatesDataReceiver(
				new KryoStreamClient(new ZMQReceiver("localhost", 9999)));
				
		
		receiver.addEventListener(new EventListener() {
			
			private int i = 0;
			
			@Override
			public void onEvent(Event event)
			{

				i++;
				System.out.println(i + " " + event);
				
				if (i == 2000 || i == 2010)
				{
					receiver.removeListener(this);
				
					try
					{
						TimeUnit.SECONDS.sleep(5);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					receiver.addEventListener(this);
				}
			}
		});
		
		
		final MDDealableQuote quote = new MDDealableQuote(new BaseMDFeedEvent(new MarketDataFeedImpl(
				new FeedNameImpl("XXXYYY"), new InstrumentImpl("XXX", "YYY", 1000))));		
		
		Executor e = Executors.newFixedThreadPool(2);
		
		
		while (true)
		{			
			e.execute(new Runnable() {
				
				@Override
				public void run()
				{
					streamer.getDataSubscriber().sendUpdate(
							new OHLCUpdate("1 min", new OHLCValue(i, 123, 123223123, 234, 323243423, 134, 312321, 300, 2321321, false, true), quote));	
				}
			});			
			i++;
		}
		
	}
	private static int i = 0;
}
