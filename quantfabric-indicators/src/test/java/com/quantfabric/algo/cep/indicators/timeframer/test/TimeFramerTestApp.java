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
package com.quantfabric.algo.cep.indicators.timeframer.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCPlugInViewFactory;
import com.quantfabric.algo.cep.indicators.ohlc.test.OHLCTestApp;
import com.quantfabric.algo.cep.indicators.timeframer.TimeFramerPlugInViewFactory;

public class TimeFramerTestApp extends Thread{
private static final Logger logger = LoggerFactory.getLogger(TimeFramerTestApp.class.getName());
	
	private final EPServiceProvider epServiceProvider;
	
	public TimeFramerTestApp(EPServiceProvider epServiceProvider)
	{
		this.epServiceProvider = epServiceProvider;
	}	
		
	@Override
	public void run()
	{
		logger.info("TimeFramer Test - Started");
		
		for (long i = 0; i < 10000; i++)
		   {
		    epServiceProvider.getEPRuntime().sendEvent(new OHLCTestApp.InputBean(System.currentTimeMillis(), (int)(Math.random() * 1000000)));
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		   }
		
		logger.info("TimeFramer Test - Stopped");
	}

	public static EPServiceProvider getEsperService()
	{
		return EPServiceProviderManager.getDefaultProvider();
		
	}
	
	private static void registerTypes(ConfigurationOperations configuration)
	{
		configuration.addEventType(OHLCTestApp.InputBean.class);			
	}
	
	private static void registerExtensions(ConfigurationOperations configuration)
	{
		configuration.addPlugInView("quantfabric", "ohlc", OHLCPlugInViewFactory.class.getName());		
		configuration.addPlugInView("quantfabric", "timeframer", TimeFramerPlugInViewFactory.class.getName());
	}
	
	private static void registerTestStatements(EPAdministrator epAdministrator)
	{
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('1 min', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('5 min', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('15 min', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('30 min', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('1 hour', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('4 hours', timestamp, price)");
		epAdministrator.createEPL("insert into OHLCValues select *, (select price from InputBean.std:lastevent()) as currBidPrice from InputBean.quantfabric:ohlc('24 hours', timestamp, price)");
		
		epAdministrator.createEPL("select * from OHLCValues.quantfabric:timeframer(timeFrameInSeconds,currBidPrice,open)").addListener(
			new UpdateListener() 
			{				
				@Override
				public void update(EventBean[] newEvents, EventBean[] oldEvents)
				{
					for (EventBean eb : newEvents)
					{
						System.out.println(eb.getClass().getName());
						System.out.println(eb.getUnderlying().getClass().getName());
						System.out.println("view output - " + eb.getUnderlying());
					}
				}
			});	
	}
		
	public static void main(String[] args)
	{
		EPServiceProvider epServiceProvider = getEsperService();
		
		if (epServiceProvider != null)
		{
			System.out.println("EPServiceProvider was created.");		
			registerTypes(epServiceProvider.getEPAdministrator().getConfiguration());
			registerExtensions(epServiceProvider.getEPAdministrator().getConfiguration());
			registerTestStatements(epServiceProvider.getEPAdministrator());
		}
		else
		{
			System.err.println("EPServiceProvider not created");
			return;
		}
		
		TimeFramerTestApp app = new TimeFramerTestApp(epServiceProvider);
		app.start();
	}

}
