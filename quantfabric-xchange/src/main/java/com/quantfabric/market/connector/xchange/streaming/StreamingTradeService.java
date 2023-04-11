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

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import com.quantfabric.algo.instrument.Instrument;

public interface StreamingTradeService {

    default Observable<TradeOrder> getOrderChanges(CurrencyPair currencyPair, Object... args) {
        throw new NotYetImplementedForExchangeException("getOrderChanges");
    }

    default Observable<TradeOrder> getOrderChanges(Instrument instrument, Object... args) {
        if (instrument instanceof CurrencyPair) {
            return getOrderChanges((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getOrderChanges");
    }

    default Observable<OrderExecutionReport> getExecutionReport(CurrencyPair currencyPair, Object... args) {
        throw new NotYetImplementedForExchangeException("getUserTrades");
    }

    default Observable<OrderExecutionReport> getExecutionReport(Instrument instrument, Object... args) {
        if (instrument instanceof CurrencyPair) {
            return getExecutionReport((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getUserTrades");
    }
}
