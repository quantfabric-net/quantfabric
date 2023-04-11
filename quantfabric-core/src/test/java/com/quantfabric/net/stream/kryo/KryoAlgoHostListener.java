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
package com.quantfabric.net.stream.kryo;

import com.quantfabric.algo.instrument.InstrumentImpl;
import com.quantfabric.algo.market.datamodel.ComplexMarketView;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDMessageInfo;
import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.OWAP;
import com.quantfabric.algo.market.datamodel.VWAP;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookViewBean;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.net.Receiver;
import com.quantfabric.net.stream.Event;
import com.quantfabric.net.stream.TypeRegistrator;
import com.quantfabric.net.stream.ZMQReceiver;

public class KryoAlgoHostListener
{
	public static void main(String[] args) throws Exception
	{
		Receiver receiver = new ZMQReceiver("localhost", 5555);		
		
		KryoStreamClient streamClient = new KryoStreamClient(receiver);
		registerUsingTypes(streamClient);
		
		while(true)
		{
			System.out.println(streamClient.read());
		}
		
	}
	
	public static void registerUsingTypes(TypeRegistrator typeRegistrator)
	{
		typeRegistrator.registerType("Event", Event.class);
		typeRegistrator.registerType("MDPrice", MDPrice.class);
		typeRegistrator.registerType("MDItem$MDItemType", MDItem.MDItemType.class);
		typeRegistrator.registerType("MDMessageInfo$MDMessageType", MDMessageInfo.MDMessageType.class);
		typeRegistrator.registerType("MDPrice$PriceType", MDPrice.PriceType.class);		
		
		//typeRegistrator.registerType("RoutedTradeOrder", RoutedTradeOrder.class);
		typeRegistrator.registerType("TradeOrder", TradeOrder.class);
		typeRegistrator.registerType("TradeOrder", TradeOrder.ExecutionInstructions.class);
		typeRegistrator.registerType("TradeOrder$OrderSide", TradeOrder.OrderSide.class);
		typeRegistrator.registerType("TradeOrder$OrderType", TradeOrder.OrderType.class);
		typeRegistrator.registerType("TradeOrder$StopSides", TradeOrder.StopSides.class);
		typeRegistrator.registerType("TradeOrder$TimeInForceMode", TradeOrder.TimeInForceMode.class);
		typeRegistrator.registerType("TradeOrder$ExecutionInstructions", TradeOrder.ExecutionInstructions.class);
		typeRegistrator.registerType("TradeOrder$ExecutionInstructions[]", TradeOrder.ExecutionInstructions[].class);
		
		//typeRegistrator.registerType("AcceptStrategyOrderExecution", AcceptStrategyOrderExecution.class);
		//typeRegistrator.registerType("DoneStrategyOrderExecution", DoneStrategyOrderExecution.class);
		//typeRegistrator.registerType("InterruptedStrategyOrderExecution", InterruptedStrategyOrderExecution.class);
		//typeRegistrator.registerType("ReplacedStrategyOrderExecution", ReplacedStrategyOrderExecution.class);
		
		//typeRegistrator.registerType("InstrumentRiskValue", InstrumentRiskValue.class);
		typeRegistrator.registerType("ComplexMarketView", ComplexMarketView.class);
		typeRegistrator.registerType("MDOrderBook", MDOrderBook.class);		
		
		typeRegistrator.registerType("OVWAP", OWAP.class);
		typeRegistrator.registerType("OVWAP$OWAPSides", OWAP.OWAPSides.class);
		typeRegistrator.registerType("VWAP", VWAP.class);
		typeRegistrator.registerType("VWAP$VWAPSides", VWAP.VWAPSides.class);
		
		typeRegistrator.registerType("MDDealableQuote", MDDealableQuote.class);
		typeRegistrator.registerType("MDDealableQuote$UpdateStatuses", MDDealableQuote.UpdateStatuses.class);
		typeRegistrator.registerType("OrderBookViewBean", OrderBookViewBean.class);
		
		typeRegistrator.registerType("FeedNameImpl", FeedNameImpl.class);
		typeRegistrator.registerType("InstrumentImpl", InstrumentImpl.class);
		
		typeRegistrator.registerType("OrderBookInfo$OrderBookTypes", OrderBookInfo.OrderBookTypes.class);
		
		//typeRegistrator.registerType("OrderExecutionState$TradingStatus", OrderExecutionState.TradingStatus.class);
		//typeRegistrator.registerType("OrderExecutionState$ExecutionStatus", OrderExecutionState.ExecutionStatus.class);
	}
}
