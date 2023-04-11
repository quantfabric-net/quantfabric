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
package com.quantfabric.algo.cep.indicators.nlma.test;

import java.util.Arrays;
import java.util.GregorianCalendar;

import org.apache.log4j.Level;

import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.algo.cep.indicators.Log4j;
import com.quantfabric.algo.cep.indicators.nlma.NLMAPlugInView;
import com.quantfabric.algo.cep.indicators.nlma.NLMAPlugInViewFactory;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCPlugInView;
import com.quantfabric.algo.cep.indicators.ohlc.test.OHLCTestApp;

public class NLMATestApp
{
	public static EPServiceProvider getEsperService()
	{
		return EPServiceProviderManager.getDefaultProvider();		
	}
	
	private static void registerTypes(ConfigurationOperations configuration)
	{

	}
	
	private static void registerExtensions(ConfigurationOperations configuration)
	{
		configuration.addPlugInView("quantfabric", "nlma", NLMAPlugInViewFactory.class.getName());
	}
	
	private static void registerTestStatements(EPAdministrator epAdministrator)
	{
		epAdministrator.createEPL("insert into Bars select * from InputBean.quantfabric:ohlc('5 sec', timestamp, price, 'ggg') as OHLCValue"); 
		
		epAdministrator.createEPL("select * from Bars.quantfabric:nlma(barId, openSourceTimestamp, close, closed, 3, 0., 10., closed) where closed = true").addListener(
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
	
	private static void init(EPServiceProvider epServiceProvider)
	{
		OHLCTestApp.registerTypes(epServiceProvider.getEPAdministrator().getConfiguration());
		OHLCTestApp.registerExtensions(epServiceProvider.getEPAdministrator().getConfiguration());
		
		registerTypes(epServiceProvider.getEPAdministrator().getConfiguration());
		registerExtensions(epServiceProvider.getEPAdministrator().getConfiguration());
		registerTestStatements(epServiceProvider.getEPAdministrator());
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		Log4j.init();
		OHLCPlugInView.getLogger().setLevel(Level.OFF);
		NLMAPlugInView.getLogger().setLevel(Level.ALL);
				
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
		
		OHLCTestApp app = new OHLCTestApp(epServiceProvider, 100000, 1000);
		app.start();
		
		app.join();
		
		epServiceProvider.getEPAdministrator().stopAllStatements();
		System.out.println("Application Terminated.");		
	}
}
