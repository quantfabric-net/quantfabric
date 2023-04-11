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
package com.quantfabric.algo.market.connector.binance;

import com.quantfabric.algo.market.gateway.MarketAdapter;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.market.connector.xchange.XChangeMarketConnection;
import org.knowm.xchange.ExchangeSpecification;

import java.util.Properties;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;


public class BinanceMarketConnection extends XChangeMarketConnection {

	public BinanceMarketConnection(MarketGateway owner, String name, Properties adapterSettings, Properties credentials)
			throws MarketConnectionException {
		super(owner, name, adapterSettings, credentials);
	}

	@Override
	protected MarketAdapter createMarketAdapter(Properties adapterSettings, Properties credentials) {
		BinanceXChangeAdapter adapter = null;
		adapter = new BinanceXChangeAdapter(this, adapterSettings, credentials);
		return adapter;
	}

	@Override
	public ExchangeSpecification createExchangeSpec() {
		ExchangeSpecification exSpec;
		if(settings.getProperty(POSTFIX).equalsIgnoreCase("com")) {
			exSpec = new BinanceXChange().getDefaultExchangeSpecification();
		} else {
			exSpec = new BinanceXChange().getDefaultUSExchangeSpecification();
		}

		if (credentials.containsKey(USERNAME))
			exSpec.setUserName(credentials.getProperty(USERNAME));
		if (credentials.containsKey(PASSWORD))
			exSpec.setPassword(credentials.getProperty(PASSWORD));
		if (credentials.containsKey(API_KEY))
			exSpec.setApiKey(credentials.getProperty(API_KEY));
		if (credentials.containsKey(SECRET_KEY))
			exSpec.setSecretKey(credentials.getProperty(SECRET_KEY));

		exSpec.setExchangeSpecificParametersItem("adapter", this.getAdapter());

		return exSpec;
	}

}
