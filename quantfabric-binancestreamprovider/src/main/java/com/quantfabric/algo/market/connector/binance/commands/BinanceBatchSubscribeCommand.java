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
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class BinanceBatchSubscribeCommand extends com.quantfabric.algo.market.gateway.commands.BatchSubscribeCommand implements ConcreteCommand {
    private static final Logger log = LoggerFactory.getLogger(BinanceBatchSubscribeCommand.class);

    public BinanceBatchSubscribeCommand(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) {
       super(mdFeeds, exFeeds);
    }

    @Override
    public void execute(CommandExecutor commandExecuter) {
        try {
            ((XChangeMarketAdapter)commandExecuter).subscribeMarketData(getMdFeeds(), getExFeeds());
        } catch (ExchangeException e) {
            log.error("Generic ExchangeException ", e);
            e.printStackTrace();
        } catch (NotAvailableFromExchangeException e) {
            log.error("Function is not supported by API ", e);
            e.printStackTrace();
        } catch (NotYetImplementedForExchangeException e) {
            log.error("Not implemented ", e);
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "BINANCE - BATCH SUBSCRIBE";
    }
}
