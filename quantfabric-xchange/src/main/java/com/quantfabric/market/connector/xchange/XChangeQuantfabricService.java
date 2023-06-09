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

import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;


public interface XChangeQuantfabricService {
    default void subscribe(MarketDataFeed feed, MarketDataSingleEventHandler handler) {}
    default void subscribe(HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap) {}

    default int getAndPublishOrderBook(long snapshotId,MarketDataFeed feed,long timestamp, MarketDataSingleEventHandler handler) throws XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException, IOException {
        throw new NotYetImplementedForExchangeException();
    }
    default int getAndPublishTrade(long snapshotId,MarketDataFeed feed,long timestamp, MarketDataSingleEventHandler handler) throws XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException, IOException {
        throw new NotYetImplementedForExchangeException();
    }
}
