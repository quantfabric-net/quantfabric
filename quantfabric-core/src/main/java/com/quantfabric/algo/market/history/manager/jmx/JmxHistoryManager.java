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
package com.quantfabric.algo.market.history.manager.jmx;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.history.TimeFrame;
import com.quantfabric.algo.market.history.manager.HistoryManager;
import com.quantfabric.util.timeframes.TimeframeFactory;

public class JmxHistoryManager implements HistoryManagerMXBean
{
	public static final String dateFormatPattern = "dd.MM.yyyy kk:mm:ss.SSS z"; //28.01.2013 02:00:00.000 GMT
	public static final DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern); 
	
	private final HistoryManager historyManager;
	private final String name;
	private final Map<MBeanServer, ObjectName> registrations = new HashMap<MBeanServer, ObjectName>();
	
	public JmxHistoryManager(String name, HistoryManager historyManager) 
	{
		this.historyManager = historyManager;
		this.name = name;
	}
	public ObjectName registerJmxBeans(String rootJmxGroup) throws JmxHistoryManagerException
	{		
		try
		{
			return registerJmxBeans(ManagementFactory.getPlatformMBeanServer(), rootJmxGroup);
		}
		catch (InstanceAlreadyExistsException e)
		{
			throw new JmxHistoryManagerException("JMX management bean for HistoryManger already registered.", e);
		}
		catch (MBeanRegistrationException e)
		{
			throw new JmxHistoryManagerException("Registration JMX bean for HistoryManger failed.", e);
		}
		catch (NotCompliantMBeanException e)
		{
			throw new JmxHistoryManagerException("JMX compatibility error", e);
		}
	}
	
	public ObjectName registerJmxBeans(MBeanServer mbeanServer, String rootJmxGroup) 
			throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, JmxHistoryManagerException
	{
		ObjectName newObjectName = null;
		try
		{
			newObjectName = new ObjectName(rootJmxGroup + ",historyManager=" + name);
		}
		catch (MalformedObjectNameException e)
		{
			throw new JmxHistoryManagerException("Wrong rootJmxGroup", e);
		}
		
		if (registrations.containsKey(mbeanServer))
			throw new InstanceAlreadyExistsException();
					
		mbeanServer.registerMBean(this, newObjectName);
		
		registrations.put(mbeanServer, newObjectName);
		
		return newObjectName;
	}
	
	public void unregisterJmxBean() throws JmxHistoryManagerException 
	{
		try
		{
			unregisterJmxBean(ManagementFactory.getPlatformMBeanServer());
		}
		catch (MBeanRegistrationException e)
		{
			throw new JmxHistoryManagerException("Unregister of JMX bean for HistoryManger failed.", e);
		}
		catch (InstanceNotFoundException e)
		{
			throw new JmxHistoryManagerException("JMX bean not registered.", e);
		}
	}
	
	public void unregisterJmxBean(MBeanServer mbeanServer) 
			throws InstanceNotFoundException, MBeanRegistrationException
	{
		if (registrations.containsKey(mbeanServer))
			mbeanServer.unregisterMBean(registrations.get(mbeanServer));
		else
			throw new InstanceNotFoundException();
	}
		
	public String getName()
	{
		return name;
	}
	
	@Override
	public List<OHLC> exportHistory(String timeFrame, int depth) throws ParseException
	{
		return getLightweightBeans(historyManager.exportHistory(getTimeFrame(timeFrame), depth));
	}
	@Override
	public List<OHLC> exportHistory(String timeFrame) throws ParseException
	{		
		return getLightweightBeans(historyManager.exportHistory(getTimeFrame(timeFrame)));
	}
	
	@Override
	public void replaceBar(String timeFrame, OHLC ohlc) throws ParseException 
	{
		TimeFrame parsedTimeFrame = getTimeFrame(timeFrame);
		historyManager.replaceBar(parsedTimeFrame, convertToOHLCValue(parsedTimeFrame, ohlc));	
	}
	
	@Override
	public void replaceBar(String timeFrame, String openTime, int open, int high, int low, int close) throws ParseException
	{
		Date parsedOpenTime = dateFormat.parse(openTime);		
		replaceBar(timeFrame, new OHLC(parsedOpenTime, open, high, low, close));		
	}	

	
	@Override
	public void addBar(String timeFrame, OHLC ohlc) throws ParseException
	{
		TimeFrame parsedTimeFrame = getTimeFrame(timeFrame);	
		historyManager.addBar(parsedTimeFrame, convertToOHLCValue(parsedTimeFrame, ohlc));		
	}
	
	@Override
	public void addBar(String timeFrame, String openTime, int open, int high,
			int low, int close) throws ParseException
	{
		Date parsedOpenTime = dateFormat.parse(openTime);		
		addBar(timeFrame, new OHLC(parsedOpenTime, open, high, low, close));
	}
	
	protected static TimeFrame getTimeFrame(String timeFrame) throws ParseException
	{
		try
		{
			return TimeFrame.getTimeFrame(TimeframeFactory.getTimeframe(timeFrame).getLengthInSeconds());
		}
		catch (Exception e)
		{
			throw new ParseException(timeFrame, 0);
		}
	}
	
	protected static List<OHLC> getLightweightBeans(Collection<OHLCValue> ohlcValues)
	{
		return getLightweightBeans(ohlcValues.toArray(new OHLCValue[]{}));
	}
	
	protected static List<OHLC> getLightweightBeans(OHLCValue... ohlcValues)
	{
		List<OHLC> ohlcList = new LinkedList<OHLC>();
		for (OHLCValue ohlcValue : ohlcValues)
			ohlcList.add(new OHLC(ohlcValue));
		
		return ohlcList;
	}
	
	protected static OHLCValue convertToOHLCValue(TimeFrame timeframe, OHLC ohlc)
	{
		int timeFrameInSeconds = timeframe.getSeconds();
		int timeFrameInMSec = timeFrameInSeconds * 1000;
		int thirdPeriod = timeFrameInMSec / 3;
		
		return new OHLCValue(
				timeFrameInSeconds, 
				ohlc.getOpen(), ohlc.getOpenTime().getTime(), 
				ohlc.getHigh(), ohlc.getOpenTime().getTime() + thirdPeriod,
				ohlc.getLow(), ohlc.getOpenTime().getTime() + (2 * thirdPeriod),
				ohlc.getClose(), ohlc.getOpenTime().getTime() + (timeFrameInMSec - 2000),
				true, false);
	}
}
