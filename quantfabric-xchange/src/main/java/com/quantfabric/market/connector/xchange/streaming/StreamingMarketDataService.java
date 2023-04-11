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
package com.quantfabric.market.connector.xchange.streaming;

import com.quantfabric.algo.market.datamodel.MDTopOfBook;
import com.quantfabric.algo.market.datamodel.MDTrade;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;

import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import com.quantfabric.algo.instrument.Instrument;

public interface StreamingMarketDataService {
    default Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        throw new NotYetImplementedForExchangeException("getOrderBook");
    }

    default Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
        if (instrument instanceof CurrencyPair) {
            return getOrderBook((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getOrderBook");
    }

    default Observable<MDTrade> getTrades(CurrencyPair currencyPair, Object... args) {
        throw new NotYetImplementedForExchangeException("getTrades");
    }

    default Observable<MDTrade> getTrades(Instrument instrument, Object... args) {
        if (instrument instanceof CurrencyPair) {
            return getTrades((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getTrades");
    }

    default Observable<MDTopOfBook> getTopOfBook(CurrencyPair currencyPair, Object... args) {
        throw new NotYetImplementedForExchangeException("getTrades");
    }

    default Observable<MDTopOfBook> getTopOfBook(Instrument instrument, Object... args) {
        if (instrument instanceof CurrencyPair) {
            return getTopOfBook((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getTrades");
    }
}
