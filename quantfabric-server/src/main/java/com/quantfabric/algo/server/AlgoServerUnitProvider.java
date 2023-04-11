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

public interface AlgoServerUnitProvider
{
	String getName();
	String getDescription();
	
	Set<String> getPipelines(String unitName);
	Map<String, String> getPipelineFeeds(String unitName, String pipeline);
	boolean isPipelineStarted(String unitName, String pipeline);
	
	Set<String> getStrategies(String unitName);
	Map<String, String> getStrategyInfo(String unitName, String strategy);
	Properties getStrategyState(String unitName, String strategy);
	
	String getStrategySettingsLayoutDefinition(String unitName, String strategy);
	
	Properties getStrategyProperties(String unitName, String strategy);
	void setStrategyProperties(String unitName, String strategy, Properties properties);
	Set<String> getStrategyPropetiesList(String unitName, String strategy);
	String getStrategyPropertyValue(String unitName, String strategy, String property);
	void setStrategyPropertyValue(String unitName, String strategy, String property, String value);
	ModificationMode getStrategyPropertyModificationMode(String unitName, String strategy, String property);
	
	boolean startStrategy(String unitName, String strategy);
	boolean stopStrategy(String unitName, String strategy);
	
	void submitMarketOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, TimeInForceMode timeInForce,int expireSec);
	void submitLimitOrder(String unitName, String strategy, String source, String executionPoint,
			OrderSide side, String instrumentId, int size, int price,TimeInForceMode timeInForce,int expireSec);
	
	void submitOCOOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, 
			OCOSettings ocoSettings);
	void submitStopLimitOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, int price, 
			StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec);
	void submitStopLossOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, 
			StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec);
	void submitTrailingStopOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side, String instrumentId, int size, int price, 
			StopSides stopSide, int stopPrice, int trailBy, int maxSlippage, int initialTriggerRate,TimeInForceMode timeInForce,int expireSec);
	void submitCustomOrder(String unitName, String strategy,
			String source, String executionPoint, TradeOrder tradeOrder);	
	
	void cancelOrder(String unitName, String strategy, String source, String executionPoint,
			String originalOrderReference);
	void replaceOrder(String unitName, String strategy, String source, String executionPoint, String originalOrderReference, String newOrderReference,
			int size, int price);
	
	Set<String> getStrategyExecutionPoints(String unitName, String strategy);
	String getExecutionPointMarketConnection(String unitName, String strategy, String executionPoint);
	boolean getExecutionPointIsActive(String unitName, String strategy, String executionPoint);
	
	Set<String> getStrategyDataSinks(String unitName, String strategy);
	boolean getStrategyDataSinkIsActive(String unitName, String strategy, String dataSink);
	String getStrategyDataSinkPipeline(String unitName, String strategy, String dataSink);
	
	Set<String> getInstruments(String unitName);
	String getSymbol(String unitName, String instrumentId);
	double castPriceToDecimal(String unitName, String instrumentId, long price);
	long castPriceToLong(String unitName, String instrumentId, double price);
	
	Set<String> getMarketConnections(String unitName);
	Set<String> getTradingMarketConnections(String unitName);
	boolean isTradingMarketConnection(String unitName, String marketConnection);
	boolean isConnectedMarketConnection(String unitName, String marketConnection);
	String getMarketConnectionDisplayName(String unitName, String marketConnection);
	Set<String> getCreditLimitedInstruments(String unitName, String marektConnection);
	double getInstrumentCreditLimit(String unitName, String marketConnection, String instrumentId);
	void setInstrumentCreditLimit(String unitName, String marketConnection, String instrumentId, double creditLimit);
	String getMarketConnectionIdentifier(String unitName, String marketConnection);
	int getMarketConnectionMode(String unitName, String marketConnection);
	Set<String> getMarketDataFeeds(String unitName, String marketConnection);
	String getMarketDataFeedInstrument(String unitName, String marketConnection, String feedName);
	String getMarketDataFeedMarketDepth(String unitName, String marketConnection, String feedName);
	Set<String> getExecutionFeeds(String unitName, String marketConnection);
	
	boolean sendCommand(String command, Map<String, Object> args);
	
	String getViewModel();
}
