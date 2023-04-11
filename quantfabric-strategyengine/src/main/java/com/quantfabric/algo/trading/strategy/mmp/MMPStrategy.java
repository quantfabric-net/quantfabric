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
package com.quantfabric.algo.trading.strategy.mmp;

import com.espertech.esper.client.Configuration;
import com.espertech.esperio.socket.EsperIOSocketAdapter;
import com.espertech.esperio.socket.config.ConfigurationSocketAdapter;
import com.espertech.esperio.socket.config.DataType;
import com.espertech.esperio.socket.config.SocketConfig;
import com.quantfabric.algo.trading.strategy.BaseTradingStrategy;
import com.quantfabric.algo.trading.strategyrunner.StrategyRunner;

public class MMPStrategy extends BaseTradingStrategy {
	
	public MMPStrategy(String name, StrategyRunner runtime) {
		
		this(name, runtime, new Configuration());
	}
	
	public MMPStrategy(String name,StrategyRunner runtime, Configuration cepConfig) {
		
		super(name, runtime, cepConfig);
		
		ConfigurationSocketAdapter adapterConfig = new ConfigurationSocketAdapter();
		SocketConfig socket = new SocketConfig();
		socket.setDataType(DataType.CSV);
		socket.setPort(24079);
		adapterConfig.getSockets().put("SocketService", socket);
		EsperIOSocketAdapter socketAdapter = new EsperIOSocketAdapter(adapterConfig, "STR_" + name);
		socketAdapter.start();		
	}
	
	@Override
	public synchronized void start() throws Exception {
		
		super.start();		
	}

	@Override
	public synchronized void stop() throws Exception {
		
		super.stop();
	}
}
