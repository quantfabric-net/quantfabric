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
package com.quantfabric.algo.server.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlgoServerWatchdog
{
	private static final Logger log = LoggerFactory.getLogger( AlgoServerWatchdog.class );
	
	private Timer timer;
	private TimerTask timerTask;
	private final List<AlgoServerWatchdogListener> listeners
		= new ArrayList<AlgoServerWatchdogListener>();
	
	public AlgoServerWatchdog(String connectionURL)
	{
		final com.quantfabric.algo.server.jmx.AlgoServerMgmtMBean algoServerMgmtMBean =
				com.quantfabric.algo.server.jmx.JMXAlgoServerService.getAlgoServerMgmtMBean(
						connectionURL);

		if (algoServerMgmtMBean != null)
			log.info("Connect to Algo Server JMX service is complete.");
		else
		{
			log.error("Connect to Algo Server JMX service is failed.");
			return;
		}
		
		timerTask = new TimerTask() {
			@Override
			public void run()
			{
				try
				{
					algoServerMgmtMBean.ping();
				}
				catch (Exception e)
				{
					log.error("Can't ping Algo Server. ", e);
					log.info("Connect to Algo Server JMX service is terminated.");
					watchdogTriggered();
				}
			}
		};
		
		timer = new Timer();		
	}
	
	public void start()
	{
		if ((timer != null) && (timerTask != null))
				timer.scheduleAtFixedRate(timerTask, 0, 1000);
	}
	
	public void stop()
	{
		if (timer != null)
			timer.cancel();
	}
	
	public void addAlgoServerWatchdogListener(AlgoServerWatchdogListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeAlgoServerWatchdogListener(AlgoServerWatchdogListener listener)
	{
		listeners.add(listener);
	}

	private void watchdogTriggered()
	{
		for(AlgoServerWatchdogListener listener : listeners)
			listener.watchdogTriggered();
	}
}
