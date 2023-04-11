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

import com.quantfabric.algo.market.connector.commands.VirtualCoinManageAcceptedOrder;
import com.quantfabric.algo.market.gateway.commands.CancelOrder;
import com.quantfabric.algo.order.TradeOrder;

public class BinanceCancelOrder extends VirtualCoinManageAcceptedOrder implements CancelOrder {

    public BinanceCancelOrder(TradeOrder order) {
        super(order);
    }

    public BinanceCancelOrder(TradeOrder order, String institutionOrderReference) {
        super(order, institutionOrderReference);
    }

    @Override
    public String getDescription() {
        return "BINANCE - OrderCancelRequest";
    }
}
