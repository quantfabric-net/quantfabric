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

import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.market.connector.xchange.commands.XChangeSubmitOrder;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

public class BinanceSubmitOrder extends XChangeSubmitOrder {

    public BinanceSubmitOrder(TradeOrder order) {
        super(order);
    }

    @Override
    public String getDescription() {
        return "BINANCE - OrderSubmitRequest";
    }

    @Override
    public LimitOrder createOrder() throws Exception {

        int sizeCorrector = 100;
        DecimalFormat format = new DecimalFormat("#.##");
        format.setRoundingMode(RoundingMode.HALF_EVEN);

        TradeOrder order = getOrder();

        Order.OrderType type = VirtualCoinMarketAdapter.BTCTypeConverter.toBtcSide(order.getOrderSide());
        BigDecimal tradableAmount = BigDecimal.valueOf(order.getSize() / sizeCorrector);
        CurrencyPair currencyPair = VirtualCoinMarketAdapter.CurrencyConverter.toCurrencyPair(order.getInstrument().getSymbol());
        String id = "";
        Date timestamp = new Date();
        BigDecimal limitPrice = new BigDecimal(format.format(order.getInstrument().castToDecimal(order.getPrice())));

        LimitOrder limitOrder = new LimitOrder(type, tradableAmount, currencyPair, id, timestamp, limitPrice);

        return limitOrder;
    }
}
