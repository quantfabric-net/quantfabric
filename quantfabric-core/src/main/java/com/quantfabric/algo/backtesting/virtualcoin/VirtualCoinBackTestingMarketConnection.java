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
package com.quantfabric.algo.backtesting.virtualcoin;

import java.util.Properties;

import com.quantfabric.algo.backtesting.BackTestingMarketConnection;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.MarketGateway;


public class VirtualCoinBackTestingMarketConnection extends BackTestingMarketConnection {

	public VirtualCoinBackTestingMarketConnection(MarketGateway parent, String name, Properties adapterSettings, Properties credentials)
			throws MarketConnectionException {
		super(parent, name, adapterSettings, credentials);
	}

	@Override
	protected void initializeAdapter(Properties adapterSettings,
			Properties credentals) throws MarketConnectionException
	{
		getLogger().debug("Invoked initializeAdapter on VirtualCoinBackTestingMarketConnection");
		setMarketAdapter(new VirtualCoinBackTestingMarketAdapter());
	}
}
