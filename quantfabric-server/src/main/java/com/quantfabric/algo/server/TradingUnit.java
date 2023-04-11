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
package com.quantfabric.algo.server;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting.ModificationMode;

public interface TradingUnit
{
	void shutdown();
	String getName();
	String getDescription();
	Set<String> getPipelines();
	Map<String, String> getPipelineFeeds(String pipeline);
	boolean isPipelineStarted(String pipeline);
	Set<String> getStrategies();
	Map<String, String> getStrategyInfo(String strategy);
	Properties getStrategyState(String strategy);
	
	String getStrategySettingsLayoutDefinition(String strategy);
	
	Properties getStrategyProperties(String strategy);
	Set<String> getStrategyPropetiesList(String strategy);
	String getStrategyPropertyValue(String strategy, String property);
	void setStrategyPropertyValue(String strategy, String property, String value);
	ModificationMode getStrategyPropertyModificationMode(String strategy, String property);
	
	
	Set<String> getStrategyDataSinks(String strategy);
	boolean getStrategyDataSinkIsActive(String strategy, String dataSink);
	String getStrategyDataSinkPipeline(String strategy, String dataSink);
	boolean startStrategy(String strategy);
	boolean stopStrategy(String strategy);
	
	void submitMarketOrder(String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size,TimeInForceMode timeInForce,int expireSec);
	void submitLimitOrder(String strategy, String source, String executionPoint, OrderSide side,
			String instrumentId, int size, int price, TimeInForceMode timeInForce,int expireSec);
	void submitOCOOrder(String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, 
			OCOSettings ocoSettings);
	void submitStopLimitOrder(String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, int price, 
			StopSides stopSide, int stopPrice, TimeInForceMode timeInForce,int expireSec);
	void submitStopLossOrder(String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, 
			StopSides stopSide, int stopPrice ,TimeInForceMode timeInForce,int expireSec);
	void submitTrailingStopOrder(String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, int price,
			StopSides stopSide, int stopPrice, int trailBy, int maxSlippage, int initialTriggerRate, TimeInForceMode timeInForce,int expireSec);
	void submitCustomOrder(String strategy,
			String source, String executionPoint, TradeOrder tradeOrder);	
	
	void cancelOrder(String strategy, String source, String executionPoint, String originalOrderReference);
	void replaceOrder(String strategy, String source, String executionPoint, String originalOrderReference, String newOrderReference,
			int size, int price);
	
	Set<String> getStrategyExecutionPoints(String strategy);
	String getExecutionPointMarketConnection(String strategy, String executionPoint);
	boolean getExecutionPointIsActive(String strategy, String executionPoint);
	
	int getStrategyDataStreamPort(String strategy);
	
	Set<String> getInstruments();
	String getSymbol(String instrumentId);
	double castPriceToDecimal(String instrumentId, long price);
	long castPriceToLong(String instrumentId, double price);
	
	Set<String> getMarketConnections();
	boolean isTradingMarketConnection(String marketConnection);
	boolean isConnectedMarketConnection(String marketConnection);
	String getMarketConnectionDisplayName(String marketConnection);
	Set<String> getCreditLimitedInstruments(String marketConnection);
	double getInstrumentCreditLimit(String marketConnection, String instrumentId);
	void setInstrumentCreditLimit(String marketConnection, String instrumentId, double creditLimit);
	String getMarketConnectionIdentifier(String marketConnection);
	int getMarketConnectionMode(String marketConnection);
	Set<String> getMarketDataFeeds(String marketConnection);
	String getMarketDataFeedInstrument(String marketConnection, String feedName);
	String getMarketDataFeedMarketDepth(String marketConnection, String feedName);
	Set<String> getExecutionFeeds(String marketConnection);
}
