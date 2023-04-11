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
package com.quantfabric.algo.trading.strategy;

import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;

public interface ManualTradingProvider
{
	void submitMarketOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size,TimeInForceMode timeInForce,int expireSec );
	void submitLimitOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size, int price,TimeInForceMode timeInForce,int expireSec);
	void submitOCOOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size, OCOSettings ocoSettings);
	void submitStopLimitOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size, int price, 
			StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec);
	void submitStopLossOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size, 
			StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec);
	void submitTrailingStopOrder(String source, String executionPoint, OrderSide side, String instrumentId, int size,
			int price, StopSides stopSide, int stopPrice, int trailBy, int maxSlippage, int initialTriggerRate,TimeInForceMode timeInForce,int expireSec);
	
	//TradeOrder in not ready for JMX
	//void submitCustomOrder(String source, String executionPoint, TradeOrder tradeOrder);
	
	void cancelOrder(String source, String executionPoint, String originalOrderReference);
	void replaceOrder(String source, String executionPoint, String originalOrderReference, String newOrderReference,
		int size, int price);
	
	
}
