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
package com.quantfabric.algo.backtesting.manger;

import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.algo.backtesting.manger.jmx.BackTestingManagerMXBean;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.runtime.QuantfabricRuntimeService;
import com.quantfabric.util.RunId;

public class BackTestingManager extends QuantfabricRuntimeService implements BackTestingManagerMXBean
{
	public BackTestingManager(){
		RunId.getInstance();
	}
	
	public void clearAllPersisterStorages()
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().clearPersisterStrorages(new BackTestingEvent() {});
	}

	@Override
	public void playBackTestingMarketData()
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().playBackTestingMarketData(new BackTestingEvent() {});	
	}

	@Override
	public void stopBackTestingMarketData()
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().stopBackTestingMarketData(new BackTestingEvent() {});
		/*FIXME Subscriber buffers are usually don't manage to clear their content before press reloadExecution, so we wait 20 sec*/
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reloadExecution()
	{
		try
		{
			QuantfabricRuntime.getGlobalBackTestingEventBus().reloadExecution(new BackTestingEvent() {});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateRunId()
	{
		QuantfabricRuntime.getGlobalBackTestingEventBus().updateRunId(new BackTestingEvent() {});
	}

	@Override
	public String getRunId()
	{
		return String.valueOf(RunId.getInstance().getRunId());
	}

}
