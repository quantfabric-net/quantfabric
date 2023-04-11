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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDMessageInfo;
import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.util.Converter;

public class Client
{
	private final StreamClient streamClient;
	private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private boolean printBeans = false; 
	
	public Client(StreamClient streamClient, boolean printBeans)
	{
		this.streamClient = streamClient;
		this.streamClient.registerType("Event", Event.class);
		this.streamClient.registerType("MDOrderBook", MDOrderBook.class);
		this.streamClient.registerType("MDPrice", MDPrice.class);
		this.streamClient.registerType("MDItem$MDItemType", MDItem.MDItemType.class);
		this.streamClient.registerType("MDMessageInfo$MDMessageType", MDMessageInfo.MDMessageType.class);
		this.streamClient.registerType("MDPrice$PriceType", MDPrice.PriceType.class);	
		this.printBeans = printBeans;
	}
	
	public void start()
	{
		int count = 0;
		int thousandsCount = 0;
		while (true)
		{
			try
			{
				Object o = streamClient.read();			
				
				if(printBeans)
					System.out.println(Converter.toString(((Event)o).getEventBean()));
				
				if (o != null)
					count++;
				
				if (count == 1000)
				{
					System.out.println(dateFormat.format(GregorianCalendar.getInstance().getTime()) + " - " + (++thousandsCount));
					count = 0;
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}
}
