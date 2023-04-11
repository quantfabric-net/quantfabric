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

import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.binance.commands.BinanceCancelOrder;
import com.quantfabric.algo.market.connector.binance.commands.BinanceSubmitOrder;
import com.quantfabric.algo.market.connector.binance.commands.BinanceSubscribe;
import com.quantfabric.algo.market.connector.binance.commands.BinanceUnsubscribe;
import com.quantfabric.algo.market.gateway.commands.MarketAdapterCommandFactory;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;

public class BinanceCommandFactory extends MarketAdapterCommandFactory {

    @Override
    public ConcreteCommand createSubscribe(MarketDataFeed feed) throws CommandFactoryException
    {
        return new BinanceSubscribe(feed);
    }
    @Override
    public ConcreteCommand createUnsubscribe(MarketDataFeed feed) throws CommandFactoryException
    {
        return new BinanceUnsubscribe(feed);
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
