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
package com.quantfabric.algo.trading.strategyrunner;


import java.util.Collection;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.trading.execution.ExecutionProvider;
import com.quantfabric.algo.trading.strategy.DataSink;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.cep.ICEPProvider;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;


public interface StrategyRunner {
	boolean DEFAULT_AUTO_RUN_STRATEGY = false;
	
	ExecutionProvider getExecutionProvider(TradingStrategy strategy);
		
	ICEPProvider getCEPProvider(String strategyName, String cepName, Configuration config);
	ICEPProvider getCEPProvider(String strategyName, String cepName, Configuration config,
                                       Collection<PersistingUpdateListenerConfig> persisterConfigs);
	MarketGateway getMarketGateway();
	void activateSink(TradingStrategy strategy, DataSink sink) throws Exception;
	void deActivateSink(TradingStrategy strategy, DataSink sink);
	void connectionStatusChanged(TradingStrategy recepient, StatusChanged event);
}
