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
package com.quantfabric.algo.backtesting.eventbus;

import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.util.ListenersGateway;

public class BackTestingEventBus implements BackTestingEventListener
{
	private final ListenersGateway<BackTestingEventListener> listenersGateway =
			new ListenersGateway<BackTestingEventListener>();
	private final BackTestingEventListener listenersProxy = ListenersGateway.getGatewayProxy(listenersGateway,
			BackTestingEventListener.class);
		
	public void attachListener(BackTestingEventListener listener)
	{
		listenersGateway.attachListener(listener);
	}
	
	public void dettachListener(BackTestingEventListener listener)
	{
		listenersGateway.detachListener(listener);
	}

	@Override
	public void clearPersisterStrorages(BackTestingEvent event)
	{
		listenersProxy.clearPersisterStrorages(event);		
	}

	@Override
	public void playBackTestingMarketData(BackTestingEvent event)
	{
		listenersProxy.playBackTestingMarketData(event);		
	}

	@Override
	public void stopBackTestingMarketData(BackTestingEvent event)
	{
		listenersProxy.stopBackTestingMarketData(event);		
	}

	@Override
	public void reloadExecution(BackTestingEvent event)
	{
		listenersProxy.reloadExecution(event);		
	}
	
	@Override
	public void updateRunId(BackTestingEvent event)
	{
		listenersProxy.updateRunId(event);		
	}

}

