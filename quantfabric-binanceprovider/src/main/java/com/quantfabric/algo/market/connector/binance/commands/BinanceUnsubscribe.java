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
package com.quantfabric.algo.market.connector.binance.commands;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.binance.BinanceXChangeAdapter;
import com.quantfabric.algo.market.gateway.commands.UnsubscribeCommand;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinanceUnsubscribe extends UnsubscribeCommand implements ConcreteCommand
{
	private static final Logger log = LoggerFactory.getLogger(BinanceUnsubscribe.class);
	
	public BinanceUnsubscribe(MarketDataFeed feed) {
		
		super(feed);
	}

	@Override
	public void execute(CommandExecutor commandExecuter) {
		((BinanceXChangeAdapter)commandExecuter).unsubscribeMarketData(getInstrument().getSymbol());
	}

	@Override
	public String getDescription() {
		return "BINANCE - UNSUBSCRIBE";
	}
}
