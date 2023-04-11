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

import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDMessageInfo;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDPrice.PriceType;
import com.quantfabric.messaging.Subscriber;
import com.quantfabric.net.Transmitter;

public class Server extends Thread
{
	private final DataStreamer dataStreamer;
	
	public Server(Transmitter transmitter, StreamServer streamServer)
	{	
		this.dataStreamer = new DataStreamer("TEST_SERV", streamServer) 
		{			
			@Override
			protected void registerTypes(TypeRegistrator typeRegistrator)
			{
				typeRegistrator.registerType("MDOrderBook", MDOrderBook.class);		
				typeRegistrator.registerType("MDPrice", MDPrice.class);
				typeRegistrator.registerType("MDItem$MDItemType", MDItem.MDItemType.class);
				typeRegistrator.registerType("MDMessageInfo$MDMessageType", MDMessageInfo.MDMessageType.class);
				typeRegistrator.registerType("MDPrice$PriceType", MDPrice.PriceType.class);	
			}
			
			@Override
			protected DataFilter createDataFilter(DataStreamer dataStreamer)
			{				
				return new DataFilter(dataStreamer) {
					
					@Override
					protected boolean filter(Object data)
					{
						return true;
					}
				};
			}
		};
	}

	@Override
	public void run()
	{
		dataStreamer.start();		
		Subscriber<Object> subscriber = dataStreamer.getDataSubscriber();
		
		while (true)
		{
			MDPrice price =				
				new MDPrice(123, MDMessageType.INCREMENTAL_REFRESH, "CURRENEX", 
					System.currentTimeMillis(), 3, 1, MDItemType.BID, "EUR/USD", 1311231321, 
					12345678, 1000000, PriceType.DEALABLE, true);
			
			price.setFeedName("eurusdFeed");
			price.setInstrumentId("12");
			
			subscriber.sendUpdate(price);
		}
	}
}
