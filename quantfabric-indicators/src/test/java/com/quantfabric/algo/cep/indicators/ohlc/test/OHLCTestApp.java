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
package com.quantfabric.algo.cep.indicators.ohlc.test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCPlugInView;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCPlugInViewFactory;

public class OHLCTestApp extends Thread
{
	public static class InputBean
	{
		private long timestamp;
		private int price;
			
		public InputBean(long timestamp, int price)
		{
			super();
			this.timestamp = timestamp;
			this.price = price;
		}
		
		public long getTimestamp()
		{
			return timestamp;
		}
		public void setTimestamp(long timestamp)
		{
			this.timestamp = timestamp;
		}
		public int getPrice()
		{
			return price;
		}
		public void setPrice(int price)
		{
			this.price = price;
		}
	}	
	
	private static final Logger logger = LoggerFactory.getLogger(OHLCTestApp.class);
	
	private final EPServiceProvider epServiceProvider;
	private final long frequency;
	private final long numberOfUpdates;
	
	public OHLCTestApp(EPServiceProvider epServiceProvider, long numberOfUpdates, long frequency)
	{
		this.epServiceProvider = epServiceProvider;
		this.frequency = frequency;
		this.numberOfUpdates = numberOfUpdates;
	}	
		
	public long getFrequency()
	{
		return frequency;
	}

	public long getNumberOfUpdates()
	{
		return numberOfUpdates;
	}



	@Override
	public void run()
	{
		logger.info("Started");
		
		try
		{		
			//double price = 86.456;
			
			/*for (long i = 0; i < numberOfUpdates; i++)
			{
				
				if (i % 10 == 0)
					price -= 0.001;
				
				epServiceProvider.getEPRuntime().sendEvent(new InputBean(System.currentTimeMillis(), (int)(price * 100000)));
				Thread.sleep(frequency);
			}*/
			//SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			//epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("11:54:08").getTime(), 132027));
			//DateFormat format =DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);


			
			/*epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:01 GMT").getTime(), 131720));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:05 GMT").getTime(), 131745));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:08 GMT").getTime(), 131703));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:09 GMT").getTime(), 131705));

			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:55:59 GMT").getTime(), 131707));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:56:59 GMT").getTime(), 131688));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:57:59 GMT").getTime(), 131695));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:58:59 GMT").getTime(), 131705));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:59:59 GMT").getTime(), 131715));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:00:59 GMT").getTime(), 131698));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:01:59 GMT").getTime(), 131750));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:02:59 GMT").getTime(), 131777));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:03:59 GMT").getTime(), 131757));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:04:59 GMT").getTime(), 131787));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:05:59 GMT").getTime(), 131776));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:06:59 GMT").getTime(), 131788));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:07:59 GMT").getTime(), 131845));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:08:59 GMT").getTime(), 131809));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:09:59 GMT").getTime(), 131764));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:10:59 GMT").getTime(), 131755));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:11:59 GMT").getTime(), 131773));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:12:59 GMT").getTime(), 131791));*/
			//long timeStamp = format.parse("Nov 4, 2003 8:14 PM").getTime();
			//System.out.println(timeStamp);
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:57:01").getTime(), 1324560));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:57:02").getTime(), 1324560));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:57:03").getTime(), 1324430));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:57:04").getTime(), 1324440));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:01").getTime(), 1324560));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:02").getTime(), 1324560));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:03").getTime(), 1324430));
			/*epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:04").getTime(), 1324440));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:05").getTime(), 1324440));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:06").getTime(), 1324440));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:07").getTime(), 1324440));*/
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:04").getTime(), 1324300));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:04").getTime(), 1324240));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:04").getTime(), 1324340));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:58:04").getTime(), 1324330));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:59:04").getTime(), 1324210));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("16:59:05").getTime(), 1324230));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:00:04").getTime(), 1324240));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:00:04").getTime(), 1324250));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:01:04").getTime(), 1324240));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:01:04").getTime(), 1324245));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:02:04").getTime(), 1324330));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:02:04").getTime(), 1324335));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:03:04").getTime(), 1324250));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:03:04").getTime(), 1324253));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:04:04").getTime(), 1324570));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:04:04").getTime(), 1324573));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:05:04").getTime(), 1324540));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:05:04").getTime(), 1324542));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:06:04").getTime(), 1324310));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:06:04").getTime(), 1324311));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:07:04").getTime(), 1324260));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:07:04").getTime(), 1324268));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:08:04").getTime(), 1324230));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:08:04").getTime(), 1324235));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:09:04").getTime(), 1324130));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:10:04").getTime(), 1323910));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:11:04").getTime(), 1323720));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:12:04").getTime(), 1323840));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:13:04").getTime(), 1323850));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:14:04").getTime(), 1323640));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:15:04").getTime(), 1323730));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:16:04").getTime(), 1323750));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:17:04").getTime(), 1323690));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:18:04").getTime(), 1323600));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:19:04").getTime(), 1323650));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:20:04").getTime(), 1323660));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:21:04").getTime(), 1323770));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:22:04").getTime(), 1323840));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:23:04").getTime(), 1323840));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:24:04").getTime(), 1323740));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:25:04").getTime(), 1323660));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:26:04").getTime(), 1323740));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:27:04").getTime(), 1323660));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:28:04").getTime(), 1323930));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:29:04").getTime(), 1323870));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:30:04").getTime(), 1323830));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:31:04").getTime(), 1323890));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:32:04").getTime(), 1323770));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:33:04").getTime(), 1323770));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:34:04").getTime(), 1323800));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:35:04").getTime(), 1323840));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:36:04").getTime(), 1323720));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:37:04").getTime(), 1323770));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:38:04").getTime(), 1323450));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:39:04").getTime(), 1323550));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:40:04").getTime(), 1323710));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:41:04").getTime(), 1323780));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:42:04").getTime(), 1323760));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:43:04").getTime(), 1323940));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:44:04").getTime(), 1323910));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:45:04").getTime(), 1323890));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:46:04").getTime(), 1323910));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:47:04").getTime(), 1324050));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:48:04").getTime(), 1323930));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:49:04").getTime(), 1324150));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:50:04").getTime(), 1324110));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:51:04").getTime(), 1324240));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:52:04").getTime(), 1324050));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:53:04").getTime(), 1324040));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:54:04").getTime(), 1323840));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:55:04").getTime(), 1323710));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("17:56:04").getTime(), 1323840));
			
			
			
			
			
			
			/*epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:13:59 GMT").getTime(), 132384));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:14:59 GMT").getTime(), 132374));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:15:59 GMT").getTime(), 132376));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:16:59 GMT").getTime(), 132387));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:17:59 GMT").getTime(), 132385));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:18:59 GMT").getTime(), 132372));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:19:59 GMT").getTime(), 132366));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:20:59 GMT").getTime(), 132366));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:21:59 GMT").getTime(), 132366));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:22:59 GMT").getTime(), 132362));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:23:59 GMT").getTime(), 132351));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:24:59 GMT").getTime(), 132352));*/
			
			
			/*epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:03 GMT").getTime(), 132637));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:05 GMT").getTime(), 132598));
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:54:59 GMT").getTime(), 132612));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:55:59 GMT").getTime(), 132612));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:56:59 GMT").getTime(), 132636));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:57:59 GMT").getTime(), 132600));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:58:59 GMT").getTime(), 132599));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 11:59:59 GMT").getTime(), 132584));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:00:59 GMT").getTime(), 132567));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:01:59 GMT").getTime(), 132567));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:02:59 GMT").getTime(), 132535));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:03:01 GMT").getTime(), 132568));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:04:59 GMT").getTime(), 132561));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:05:01 GMT").getTime(), 132565));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:06:59 GMT").getTime(), 132579));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:07:59 GMT").getTime(), 132566));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:08:59 GMT").getTime(), 132580));
			
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:09:59 GMT").getTime(), 132584));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:10:01 GMT").getTime(), 132584));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:11:59 GMT").getTime(), 132608));
			
			
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:12:59 GMT").getTime(), 131681));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:13:59 GMT").getTime(), 132604));
			
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:14:59 GMT").getTime(), 132603));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:15:59 GMT").getTime(), 132615));
			
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:16:59 GMT").getTime(), 132613));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:17:59 GMT").getTime(), 132568));
			
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:18:59 GMT").getTime(), 132541));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:19:59 GMT").getTime(), 132553));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:20:59 GMT").getTime(), 132536));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:21:59 GMT").getTime(), 132500));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:22:59 GMT").getTime(), 132400));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:23:59 GMT").getTime(), 132300));
			
			epServiceProvider.getEPRuntime().sendEvent(new InputBean(format.parse("Fri, 03 Oct 2012 12:24:59 GMT").getTime(), 132100));*/
		}
		catch (Exception e)
		{
			logger.error("Crashed", e);
		}
		finally
		{
			logger.info("Stopped");
		}
}

	public static EPServiceProvider getEsperService()
	{
		return EPServiceProviderManager.getDefaultProvider();		
	}
	
	public static void registerTypes(ConfigurationOperations configuration)
	{
		configuration.addEventType(InputBean.class);			
	}
	
	public static void registerExtensions(ConfigurationOperations configuration)
	{
		configuration.addPlugInView("quantfabric", "ohlc", OHLCPlugInViewFactory.class.getName());		
	}
	
	public static void registerTestStatements(EPAdministrator epAdministrator)
	{
		epAdministrator.createEPL("select * from InputBean.quantfabric:ohlc('5 s', timestamp, price, 'some additional propery 1', 2, 3.3, price) " +
								  "where closed = true").addListener(
			new UpdateListener() 
			{				
				@Override
				public void update(EventBean[] newEvents, EventBean[] oldEvents)
				{
					for (EventBean eb : newEvents)
					{
						System.out.println();
						String header = null;
						System.out.println(header = "-- " + GregorianCalendar.getInstance().getTime() + " --------------------------------");
						System.out.println(" EventBean Type : " + eb.getClass().getName());
						System.out.println(" Underlying Object Type : " + eb.getUnderlying().getClass().getName());
						System.out.println(" EventBean value : " + eb.getUnderlying());
						
						char[] footerArr = new char[header.length()];
						Arrays.fill(footerArr, '-');
						System.out.println(new String(footerArr));
					}
				}
			});	
	}
	
	public static void init(EPServiceProvider epServiceProvider)	
	{
		registerTypes(epServiceProvider.getEPAdministrator().getConfiguration());
		registerExtensions(epServiceProvider.getEPAdministrator().getConfiguration());
		registerTestStatements(epServiceProvider.getEPAdministrator());
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		OHLCPlugInView.getLogger().setLevel(Level.WARN);
				
		EPServiceProvider epServiceProvider = getEsperService();
		
		if (epServiceProvider != null)
		{
			System.out.println("EPServiceProvider was created.");		
			init(epServiceProvider);
		}
		else
		{
			System.err.println("EPServiceProvider not created");
			return;
		}
		
		OHLCTestApp app = new OHLCTestApp(epServiceProvider, 100000, 10);
		app.start();
		
		app.join();
		
		epServiceProvider.getEPAdministrator().stopAllStatements();
		System.out.println("Application Terminated.");		
	}

}
