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
package com.quantfabric.algo.trading.execution;

import com.quantfabric.algo.instrument.InstrumentImpl;
import com.quantfabric.algo.market.datamodel.BaseLightweightMDFeedEvent;
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
import com.quantfabric.algo.order.IFDSettings;
import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.PeggedSettings;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.trading.execution.report.AcceptStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.DoneStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.InstrumentRiskValue;
import com.quantfabric.algo.trading.execution.report.InterruptedStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.RejectedManageStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.ReplacedStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.TradeReport;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState;
import com.quantfabric.algo.trading.strategy.events.StrategyEvent;
import com.quantfabric.algo.trading.strategy.events.StrategyInfoChangedEvent;
import com.quantfabric.algo.trading.strategy.events.StrategySettingChangedEvent;
import com.quantfabric.algo.trading.strategy.events.StrategyStateChangedEvent;
import com.quantfabric.net.stream.DataFilter;
import com.quantfabric.net.stream.DataStreamer;
import com.quantfabric.net.stream.StreamServer;
import com.quantfabric.net.stream.TypeRegistrator;

public class StrategyEventsStreamer extends DataStreamer
{

	public StrategyEventsStreamer(ExecutionProviderImpl executionProvider, StreamServer streamServer)
	{
		this(executionProvider.getTradeMonitor().getName(), streamServer);
		executionProvider.setStrategyDataStreamer(this);
	}
	
	public StrategyEventsStreamer(String dataSource, StreamServer streamServer)
	{
		super(dataSource, streamServer);
	}

	@Override
	protected DataFilter createDataFilter(DataStreamer dataStreamer)
	{		
		return 
			new DataFilter(this) {
				
				@Override
				protected boolean filter(Object data)
				{				
					if (data instanceof RoutedTradeOrder)
						return true;
					if (data instanceof TradeOrder)
						return true;
					if (data instanceof AcceptStrategyOrderExecution)
						return true;
					if (data instanceof DoneStrategyOrderExecution)
						return true;
					if (data instanceof InterruptedStrategyOrderExecution)
						return true;
					if (data instanceof ReplacedStrategyOrderExecution)
						return true;
					if (data instanceof RejectedManageStrategyOrderExecution)
						return true;					
					if (data instanceof InstrumentRiskValue)
						return true;					
					if (data instanceof ComplexMarketView)
						return true;
					if (data instanceof MDOrderBook)
						return true;					
					if (data instanceof StrategyEvent)
						return true;					
					if (data instanceof TradeReport)
						return true;
                    return data instanceof BaseLightweightMDFeedEvent;
                }
			};
	}

	@Override
	protected void registerTypes(TypeRegistrator typeRegistrator)
	{		
		registerUsingTypes(typeRegistrator);
	}
	
	public static void registerUsingTypes(TypeRegistrator typeRegistrator)
	{
		typeRegistrator.registerType("MDPrice", MDPrice.class);
		typeRegistrator.registerType("MDItem$MDItemType", MDItem.MDItemType.class);
		typeRegistrator.registerType("MDMessageInfo$MDMessageType", MDMessageInfo.MDMessageType.class);
		typeRegistrator.registerType("MDPrice$PriceType", MDPrice.PriceType.class);			
		typeRegistrator.registerType("RoutedTradeOrder", RoutedTradeOrder.class);
		typeRegistrator.registerType("TradeOrder", TradeOrder.class);
		typeRegistrator.registerType("TradeOrder", TradeOrder.ExecutionInstructions.class);
		typeRegistrator.registerType("TradeOrder$OrderSide", TradeOrder.OrderSide.class);
		typeRegistrator.registerType("TradeOrder$OrderType", TradeOrder.OrderType.class);
		typeRegistrator.registerType("TradeOrder$StopSides", TradeOrder.StopSides.class);
		typeRegistrator.registerType("TradeOrder$TimeInForceMode", TradeOrder.TimeInForceMode.class);
		typeRegistrator.registerType("TradeOrder$ExecutionInstructions", TradeOrder.ExecutionInstructions.class);
		typeRegistrator.registerType("TradeOrder$ExecutionInstructions[]", TradeOrder.ExecutionInstructions[].class);	
		typeRegistrator.registerType("OCOSettings", OCOSettings.class);	
		typeRegistrator.registerType("OCOSettings$LegSide", OCOSettings.LegSide.class);	
		typeRegistrator.registerType("OCOSettings$StopSide", OCOSettings.StopSide.class);	
		typeRegistrator.registerType("OCOSettings$LegType", OCOSettings.LegType.class);	
		typeRegistrator.registerType("IFDSettings", IFDSettings.class);	
		typeRegistrator.registerType("PeggedSettings", PeggedSettings.class);	
		typeRegistrator.registerType("PeggedSettings$OrdSides", PeggedSettings.OrdSides.class);	
		typeRegistrator.registerType("PeggedSettings$OrdTypes", PeggedSettings.OrdTypes.class);
		
		typeRegistrator.registerType("AcceptStrategyOrderExecution", AcceptStrategyOrderExecution.class);
		typeRegistrator.registerType("DoneStrategyOrderExecution", DoneStrategyOrderExecution.class);
		typeRegistrator.registerType("InterruptedStrategyOrderExecution", InterruptedStrategyOrderExecution.class);
		typeRegistrator.registerType("ReplacedStrategyOrderExecution", ReplacedStrategyOrderExecution.class);	
		typeRegistrator.registerType("RejectedManageStrategyOrderExecution", RejectedManageStrategyOrderExecution.class);	
		typeRegistrator.registerType("InstrumentRiskValue", InstrumentRiskValue.class);
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
		typeRegistrator.registerType("OrderExecutionState$TradingStatus", OrderExecutionState.TradingStatus.class);
		typeRegistrator.registerType("OrderExecutionState$ExecutionStatus", OrderExecutionState.ExecutionStatus.class);
		
		typeRegistrator.registerType("StrategyEvent", StrategyEvent.class);
		typeRegistrator.registerType("StrategyStateChangedEvent", StrategyStateChangedEvent.class);
		typeRegistrator.registerType("StrategyInfoChangedEvent", StrategyInfoChangedEvent.class);
		typeRegistrator.registerType("StrategySettingChangedEvent", StrategySettingChangedEvent.class);
		
		typeRegistrator.registerType("TradeReport", TradeReport.class);
		
		typeRegistrator.registerType("BaseLightweightMDFeedEvent", BaseLightweightMDFeedEvent.class);		
	}

}
