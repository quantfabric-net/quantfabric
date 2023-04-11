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
package com.quantfabric.market.connector.xchange;

import com.quantfabric.algo.market.gateway.MarketAdapter;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.MarketConnectionImp;
import com.quantfabric.algo.market.gateway.MarketGateway;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;

import java.util.Properties;

public abstract class  XChangeMarketConnection <T extends XChange> extends MarketConnectionImp {
	protected T exchange;

	public XChangeMarketConnection(MarketGateway owner, String name, Properties adapterSettings, Properties credentials)
			throws MarketConnectionException {
		
		super(owner, name, adapterSettings, credentials);
		ExchangeSpecification exSpec = createExchangeSpec();
		this.exchange = (T)getExchangeFromSpec(exSpec);
	}

	@Override
	protected void initializeAdapter(Properties adapterSettings, Properties credentials) {
		
		this.setMarketAdapter(createMarketAdapter(adapterSettings, credentials));
	}
	
	protected abstract MarketAdapter createMarketAdapter(Properties adapterSettings, Properties properties);

	protected abstract ExchangeSpecification createExchangeSpec();

	public Exchange getExchangeFromSpec(ExchangeSpecification exSpec) {
		return StreamingExchangeFactory.INSTANCE.createExchange(exSpec);
	}

	public T getExchange() {

		return this.exchange;
	}


}
