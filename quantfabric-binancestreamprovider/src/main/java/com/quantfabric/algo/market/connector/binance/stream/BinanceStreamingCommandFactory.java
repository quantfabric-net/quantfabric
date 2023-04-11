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
package com.quantfabric.algo.market.connector.binance.stream;

import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.binance.commands.*;
import com.quantfabric.algo.market.gateway.commands.MarketAdapterCommandFactory;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;

import java.util.Collection;

public class BinanceStreamingCommandFactory extends MarketAdapterCommandFactory {

    @Override
    public ConcreteCommand createSubscribe(MarketDataFeed feed) throws CommandFactoryException {
        throw new CommandFactoryException("KNOWN-based streaming providers only allows BATCH subscription. " +
                "Reason: connection settings can only be set once." +
                "Please, add <setting name=\"batch_subscription\" value=\"true\"/> to the adapter setting in the gateway configuration file.\"");
    }

    @Override
    public ConcreteCommand createBatchSubscribe(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) throws CommandFactoryException
    {
        return new BinanceBatchSubscribeCommand(mdFeeds, exFeeds);
    }
    @Override
    public ConcreteCommand createUnsubscribe(MarketDataFeed feed) throws CommandFactoryException
    {
        throw new CommandFactoryException("KNOWN-based streaming providers only allows BATCH subscription. " +
                "Reason: connection settings can only be set once." +
                "Please, add <setting name=\"batch_subscription\" value=\"true\"/> to the adapter setting in the gateway configuration file.");
    }

    @Override
    public ConcreteCommand createBatchUnsubscribe(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) throws CommandFactoryException {
        return new BinanceBatchUnsubscribeCommand(mdFeeds, exFeeds);
    }

    @Override
    public ConcreteCommand createSubmitOrder(TradeOrder order) throws CommandFactoryException
    {
        return new BinanceSubmitOrder(order);
    }
    @Override
    public ConcreteCommand createCancelOrder(TradeOrder order) throws CommandFactoryException
    {
        return createCancelOrder(order, null);
    }
    @Override
    public ConcreteCommand createCancelOrder(TradeOrder order, String institutionOrderReference) throws CommandFactoryException
    {
        return new BinanceCancelOrder(order, institutionOrderReference);
    }

}
