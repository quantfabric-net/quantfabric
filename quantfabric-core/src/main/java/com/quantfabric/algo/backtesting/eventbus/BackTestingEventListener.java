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

import java.util.EventListener;

import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;

public interface BackTestingEventListener extends EventListener
{
	void clearPersisterStrorages(BackTestingEvent event);
	void playBackTestingMarketData(BackTestingEvent event);
	void stopBackTestingMarketData(BackTestingEvent event);
	void reloadExecution(BackTestingEvent event);
	void updateRunId(BackTestingEvent event);
}
