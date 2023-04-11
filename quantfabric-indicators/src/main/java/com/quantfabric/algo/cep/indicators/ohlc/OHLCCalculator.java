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
package com.quantfabric.algo.cep.indicators.ohlc;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.util.timeframes.Interval;
import com.quantfabric.util.timeframes.Timeframe;
import com.quantfabric.util.timeframes.TimeframeFactory;



public class OHLCCalculator
{
	protected OHLCValue currentOhlcValue = null;
	protected Timeframe timeframe;
	protected Interval currentTimeframeInterval = null;
	protected Timer timer;
	protected final static int LATE_EVENT_SLACK_MILLS = 30000;
	protected volatile OHLCUpdateListener listener; 
	protected int timeOffset;
	
	public OHLCCalculator(String period) {
		
		this(period, 0);
	}
	
	public OHLCCalculator(String period, int timeOffSet)
	{
		this.timeframe = TimeframeFactory.getTimeframe(period);
		this.timeOffset = timeOffSet;	
	}		
	
	public int getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(int timeOffset) {
		this.timeOffset = timeOffset;
	}

	public Timeframe getTimeframe()
	{
		return timeframe;
	}

	public void update(long timestamp, long price) throws Exception
	{
		update(new Date(timestamp), price);
	}
	
	public synchronized void update(Date timestamp, long price) throws Exception
	{
		if (currentOhlcValue == null)
		{
			init(timestamp);
		}
					
		if (timestamp.compareTo(this.currentTimeframeInterval.getEnd()) >= 0)
		{
			currentOhlcValue.close(false);
			postCurrentState();
			init(timestamp);
			/*when the history provider includes the data on a intersection of summer and winter time
			 *the intervals become negative.
			 *28.10.2012 03:00:00.000,128.232,128.232,128.232,128.232,0.00
			  28.10.2012 03:01:00.000,128.232,128.232,128.232,128.232,0.00
			  ......
			  28.10.2012 03:00:00.000,128.232,128.232,128.232,128.232,0.00
			  28.10.2012 03:01:00.000,128.232,128.232,128.232,128.232,0.00
			  ...... 
			 * 
			 * */
			try {
				setupTimeout(calculateDelay(currentTimeframeInterval.getEnd(), timestamp));
			} catch (Exception e) {
				System.out.printf("ERROR (TF %s). Inconsistency in time intervals [%s, %s]. Exception message: %s%n",
						timeframe, currentTimeframeInterval.getEnd(), timestamp, e.getMessage());
			}
		}
		
		currentOhlcValue.update(timestamp.getTime(), price);
		
		postCurrentState();
	}	
	
	public void setListener(OHLCUpdateListener listener)
	{		
		synchronized (this)
		{
			this.listener = listener;
		}
	}
	
	public void removeListener()
	{
		setListener(null);
	}
	
	protected void postCurrentState()
	{		
		if (listener != null)
		{
			synchronized (this)
			{
				listener.update(currentOhlcValue.clone());
			}
		}
	}

	protected void setupTimeout(long delay)
	{
		if (timer != null)
			timer.cancel();

		timer = new Timer(true);
		
		timer.schedule(
				new TimerTask() 
				{
					@Override
					public void run()
					{
						currentOhlcValue.close(true);
						postCurrentState();
						currentOhlcValue = null;
					}
				}, delay);
	}

	public void init(OHLCValue value)
	{
		currentTimeframeInterval = this.timeframe.interval(new Date(value.getOpenSourceTimestamp()), this.timeOffset);
		currentOhlcValue = value;
	}
	
	protected void init(Date timestamp)
	{
		currentTimeframeInterval = this.timeframe.interval(timestamp, this.timeOffset);
		currentOhlcValue = new OHLCValue(this.timeframe.getLengthInSeconds());
	}
	
	protected static long calculateDelay(Date endIntervalTime, Date lastUpdateTime)
	{
		return endIntervalTime.getTime() - lastUpdateTime.getTime() + LATE_EVENT_SLACK_MILLS; // leave some time for late comers
	}
	
	public boolean isInitialized()
	{
		return currentOhlcValue != null;
	}
	
}
