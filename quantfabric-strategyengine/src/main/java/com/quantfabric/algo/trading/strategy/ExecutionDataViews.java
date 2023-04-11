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

import java.util.ArrayList;

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.trading.execution.report.ComplexOrderExecution;
import com.quantfabric.algo.trading.execution.tradeMonitor.ExecutionBatch;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState;

public class ExecutionDataViews
{
	public static ComplexOrderExecution[] getComplexOrderExecutions(String strategyId)
	{
		ExecutionBatch executionBatch = BaseTradingStrategy.getStrategy(strategyId).
			getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
				
		return executionBatch.getComplexOrderExecutions().toArray(new ComplexOrderExecution[]{}) ;		
	}
	
	public static ComplexOrderExecution[] getComplexOrderExecutionsByInstrument(String strategyId, String instrumentId)
	{
		ExecutionBatch executionBatch = BaseTradingStrategy.getStrategy(strategyId).
			getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
				
		return executionBatch.getComplexOrderExecutions(instrumentId).toArray(new ComplexOrderExecution[]{}) ;		
	}
	
	public static ComplexOrderExecution getComplexOrderExecution(
			String strategyId, String complexOrderReference)
	{
		ExecutionBatch executionBatch = BaseTradingStrategy.getStrategy(strategyId).
			getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
				
		return executionBatch.getComplexOrderExecution(complexOrderReference);		
	}
	
	public static ComplexOrderExecution getComplexOrderExecutionByInstrument(String strategyId,
			String instrumentId, String complexOrderReference)
	{
		ExecutionBatch executionBatch = BaseTradingStrategy.getStrategy(strategyId).
			getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
		
		return executionBatch.getComplexOrderExecution(instrumentId, complexOrderReference);
	}
		
	public static int getCountActiveOrdersByComplexOrderAndLegId(
			String strategyId, String complexOrderReference, int complexLegId, 
			boolean includeAcceptPendingOrders,
			boolean includeReplacePendingOrders) throws Exception
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		int activeOrdersCount = 0;
		
		for (OrderExecutionState orderExecutionState : 
			strategy.getExecutionProvider().getTradeMonitor().
				getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (orderExecutionState.getOrder().getComplexOrderReference().
					equals(complexOrderReference) &&
				orderExecutionState.getOrder().getComplexOrderLegId() == complexLegId)
			{
				activeOrdersCount++;
			}
		
		return activeOrdersCount;		
	}
	
	public static int getCountActiveOrdersByInstrumentAndComplexLegId(
			String strategyId, String instrumentId, int complexOrderLegId,
			boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders) throws Exception
	{	
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		int activeOrdersCount = 0;
		
		for (OrderExecutionState orderExecutionState : 
			strategy.getExecutionProvider().getTradeMonitor().
				getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (orderExecutionState.getOrder().getInstrumentId().equals(instrumentId) && 
					orderExecutionState.getOrder().getComplexOrderLegId() == complexOrderLegId)
			{
				activeOrdersCount++;
			}
		
		//System.out.println("Count active orders for instrumentId=" + instrumentId + " complexOrderLegId=" + complexOrderLegId + " : " + activeOrdersCount);
		
		return activeOrdersCount;		
	}
	
	public static int getCountActiveOrdersByComplexOrder(
			String strategyId, String complexOrderReference, 
			boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders) throws Exception
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		int activeOrdersCount = 0;
		
		for (OrderExecutionState orderExecutionState : 
			strategy.getExecutionProvider().getTradeMonitor().
				getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (orderExecutionState.getOrder().getComplexOrderReference().
					equals(complexOrderReference))
			{
				activeOrdersCount++;
			}
		
		return activeOrdersCount;		
	}
	
	public static TradeOrder[] getActiveOrdersByComplexOrderAndInstrument(
			String strategyId, String complexOrderReference, String instrumentId, 
			boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders) throws Exception
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		ArrayList<TradeOrder> activeOrders = new ArrayList<TradeOrder>();
		
		if (instrumentId == null || instrumentId.equalsIgnoreCase("all"))
		{
			for (OrderExecutionState orderExecutionState : 
				strategy.getExecutionProvider().getTradeMonitor().
					getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
				if (orderExecutionState.getOrder().getComplexOrderReference().
						equals(complexOrderReference))
				{
					activeOrders.add(orderExecutionState.getOrder());
				}
		}
		else
		{
			for (OrderExecutionState orderExecutionState : 
				strategy.getExecutionProvider().getTradeMonitor().
					getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
				if (orderExecutionState.getOrder().getComplexOrderReference().
						equals(complexOrderReference) && 
					orderExecutionState.getOrder().getInstrumentId().equals(instrumentId))
				{
					activeOrders.add(orderExecutionState.getOrder());
				}
		}
		
		return activeOrders.toArray(new TradeOrder[]{});		
	}
	
	public static TradeOrder[] getActiveOrdersByInstrument(
			String strategyId, String instrumentId, boolean includeAcceptPendingOrders,
			boolean includeReplacePendingOrders) throws Exception
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		ArrayList<TradeOrder> activeOrders = new ArrayList<TradeOrder>();
		
		for (OrderExecutionState orderExecutionState : 
			strategy.getExecutionProvider().getTradeMonitor().
				getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (orderExecutionState.getOrder().getInstrumentId().equals(instrumentId))
			{
				activeOrders.add(orderExecutionState.getOrder());
			}
		
		return activeOrders.toArray(new TradeOrder[]{});		
	}
	
	public static TradeOrder[] getActiveOrdersByInstrumentAndComplexLegId(
			String strategyId, String instrumentId, int complexOrderLegId, 
			boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders) throws Exception
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
		
		ArrayList<TradeOrder> activeOrders = new ArrayList<TradeOrder>();
		
		for (OrderExecutionState orderExecutionState : 
			strategy.getExecutionProvider().getTradeMonitor().
				getCurrentOrderBatch().getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (orderExecutionState.getOrder().getInstrumentId().equals(instrumentId) && 
				orderExecutionState.getOrder().getComplexOrderLegId() == complexOrderLegId)
			{
				activeOrders.add(orderExecutionState.getOrder());
			}
		
		return activeOrders.toArray(new TradeOrder[]{});		
	}
	
	public static double getPosistionByInstrument(String strategyId, String instrumentId)
	{
		ExecutionBatch executionBatch = BaseTradingStrategy.getStrategy(strategyId).
		getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
		
		return executionBatch.getPositionByInstrument(instrumentId);
	}
	
	public static TradeOrder getTradeOrderByOrderReference(String strategyId, String orderReference)
	{
		BaseTradingStrategy strategy = BaseTradingStrategy.getStrategy(strategyId);
				
		if (strategy == null)
			return null;
		
		ExecutionBatch executionBatch = 
				strategy.getExecutionProvider().getTradeMonitor().getCurrentOrderBatch();
		
		if (executionBatch == null)
			return null;
		
		OrderExecutionState orderExecState = executionBatch.getOrderExecutionState(orderReference);
		
		if (orderExecState == null)
			return null;
		
		return orderExecState.getOrder();		
		
	}
}
