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
package com.quantfabric.algo.trading.execution.tradeMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.report.Trade;
import com.quantfabric.algo.trading.execution.RoutedTradeOrder;
import com.quantfabric.algo.trading.execution.report.ComplexOrderExecution;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.ExecutionStatus;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.TradingStatus;

public class ExecutionBatch
{
	private static final Logger log = LoggerFactory.getLogger(ExecutionBatch.class);
	
	private final Map<String, OrderExecutionState> deals = new HashMap<String, OrderExecutionState>();
	private final Map<String, ComplexOrderExecution> complexExecutions =
		new HashMap<String, ComplexOrderExecution>();
	private final Map<String, Map<String, ComplexOrderExecution>> complexExecutionsByInstrument =
		new HashMap<String, Map<String, ComplexOrderExecution>>();
	private final Map<String, Double> positions = new HashMap<String, Double>();
	
	private final Timer collectorTimer = new Timer();
	
	public ExecutionBatch()
	{
		setupCompletedOrderCollector(10000L);
	}
	
	private Set<String> getDealsOrderReferences()
	{
		synchronized (deals)
		{		
			return new HashSet<String>(deals.keySet());
		}
	}
	
	private void setupCompletedOrderCollector(final long millisecondsOld)
	{
		TimerTask collector = new TimerTask()
		{
			@Override
			public void run()
			{
				log.debug("Start completed orders collector");
				Set<String> orderReferences = getDealsOrderReferences();
				for (String orderReference : orderReferences)
				{
					OrderExecutionState orderExecutionState = getOrderExecutionState(orderReference);
					if (orderExecutionState != null && orderExecutionState.isCompleted())
					{
						long old = GregorianCalendar.getInstance().getTime().getTime() - 
							orderExecutionState.getTimeOfComplete().getTime();
						if (old > millisecondsOld)
						{
							log.debug("remove completed order (" + orderReference + ")");
							deleteOrderExecutionState(orderReference);
						}
					}					
				}
			}
		};
		
		collectorTimer.schedule(collector, 30000, 30000);
	}

	public ComplexOrderExecution getComplexOrderExecution(String complexOrderReference)
	{
		if (!complexExecutions.containsKey(complexOrderReference))
			complexExecutions.put(complexOrderReference, 
					new ComplexOrderExecution(complexOrderReference));
		
		return complexExecutions.get(complexOrderReference);
	}
	
	public Collection<ComplexOrderExecution> getComplexOrderExecutions()
	{
		return complexExecutions.values();
	}
	
	public void deleteComplexOrderExecution(String complexOrderReference)
	{
		complexExecutions.remove(complexOrderReference);
	}
		
	public ComplexOrderExecution getComplexOrderExecution(
			String instrumentId, String complexOrderReference)
	{
		if (!complexExecutionsByInstrument.containsKey(instrumentId))
			complexExecutionsByInstrument.put(instrumentId, 
					new HashMap<String, ComplexOrderExecution>());

		Map<String, ComplexOrderExecution> complexOrderExecutions =
			complexExecutionsByInstrument.get(instrumentId);
		
		if (!complexOrderExecutions.containsKey(complexOrderReference))
			complexOrderExecutions.put(complexOrderReference, 
					new ComplexOrderExecution(complexOrderReference));
		
		return complexOrderExecutions.get(complexOrderReference);
	}
	
	public Collection<ComplexOrderExecution> getComplexOrderExecutions(
			String instrumentId)
	{
		if (!complexExecutionsByInstrument.containsKey(instrumentId))
			complexExecutionsByInstrument.put(instrumentId, 
					new HashMap<String, ComplexOrderExecution>());

		return complexExecutionsByInstrument.get(instrumentId).values();
	}
	
	public void deleteComplexOrderExecution(String instrumentId, String complexOrderReference)
	{
		complexExecutionsByInstrument.get(instrumentId).remove(complexOrderReference);
	}
	
	public OrderExecutionState newOrderExecution(RoutedTradeOrder routedTradeOrder)
	{
		synchronized (deals)
		{	
			return deals.put(routedTradeOrder.getOrder().getOrderReference(), new OrderExecutionState(routedTradeOrder));
		}
	}
	
	public OrderExecutionState newOrderExecution(RoutedTradeOrder order, 
			String institutionOrderReference, OrderExecutionState replacedOrderExecutionState)
	{
		synchronized (deals)
		{
			return deals.put(order.getOrder().getOrderReference(), 
				new OrderExecutionState(order, institutionOrderReference, 
						replacedOrderExecutionState));
		}
	}
	
	public OrderExecutionState getOrderExecutionState(String orderReference)
	{
		synchronized (deals)
		{
			return deals.get(orderReference);
		}
	}
	
	public OrderExecutionState getOrderExecutionStateByComplexAttr(
			String complexOrderReference, int complexOrderLegId)
	{
		for (OrderExecutionState orderExecutionState : getOrdersExecutionState())
			if (orderExecutionState.getOrder().getComplexOrderReference().
					equals(complexOrderReference) &&
				orderExecutionState.getOrder().getComplexOrderLegId() == complexOrderLegId)
				return orderExecutionState;
		
		return null;		
	}
	
	public List<OrderExecutionState> getOppositeActiveOrders(TradeOrder tradeOrder)
	{
		ArrayList<OrderExecutionState> listOfOpositeActiveOrder = 
			new ArrayList<OrderExecutionState>();
		
		for (OrderExecutionState orderExecutionState : getActiveOrders(false, false))
		{
			TradeOrder activeOrder = orderExecutionState.getOrder();
			if (activeOrder.getInstrumentId().equals(tradeOrder.getInstrumentId()) && 
					activeOrder.getOrderSide() != tradeOrder.getOrderSide())
				listOfOpositeActiveOrder.add(orderExecutionState);					
		}
		
		return listOfOpositeActiveOrder;
	}
	
	/*public OrderExecutionState getOrderExecutionStateByReplacemetOrderReference(
			String replacemetOrderReference)
	{
		for (OrderExecutionState orderExecutionState : deals.values())	
			if (orderExecutionState.getReplacementOrderReference().equals(replacemetOrderReference))
				return orderExecutionState;
		
		return null;
	}*/
	
	public Collection<OrderExecutionState> getOrdersExecutionState()
	{
		synchronized (deals)
		{		
			return new ArrayList<OrderExecutionState>(deals.values());
		}
	}
	
	public List<OrderExecutionState> getActiveOrders(Instrument instrument, boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders)
	{
		ArrayList<OrderExecutionState> listOfActiveOrder = new ArrayList<OrderExecutionState>();
		for (OrderExecutionState deal : getActiveOrders(includeAcceptPendingOrders, includeReplacePendingOrders))
			if (deal.getOrder().getInstrument().getId().equals(instrument.getId()))
					listOfActiveOrder.add(deal);
		return listOfActiveOrder;
	}
	
	public List<OrderExecutionState> getActiveOrders(boolean includeAcceptPendingOrders, boolean includeReplacePendingOrders)
	{
		ArrayList<OrderExecutionState> listOfActiveOrder = new ArrayList<OrderExecutionState>();
		for (OrderExecutionState deal : getOrdersExecutionState())
			if (deal.getExecutionStatus() == ExecutionStatus.Active || 
				(includeAcceptPendingOrders && deal.getExecutionStatus() == ExecutionStatus.OrderAcceptPending) || 
				(includeReplacePendingOrders && deal.getExecutionStatus() == ExecutionStatus.OrderReplacePending))
			{
				listOfActiveOrder.add(deal);
			}
		return listOfActiveOrder;
	}
	
	public Collection<OrderExecutionState> getInterruptedOrders(boolean includeWithEmptyTrading)
	{
		ArrayList<OrderExecutionState> listOfActiveOrder = new ArrayList<OrderExecutionState>();
		for (OrderExecutionState deal : getOrdersExecutionState())
			if (deal.getExecutionStatus() == ExecutionStatus.Interrupted && 
					(includeWithEmptyTrading || deal.getTradingStatus() != TradingStatus.Empty))				
				listOfActiveOrder.add(deal);
		return listOfActiveOrder;
	}
	
	public int deleteOrderExecutionState(TradeOrder order)
	{
		return deleteOrderExecutionState(order.getOrderReference());
	}
	
	public int deleteOrderExecutionState(String orderReference)
	{
		synchronized (deals)
		{			
	
			OrderExecutionState deal = deals.get(orderReference);
			if (deal != null)
			{		
				log.debug("delete execution state of order (" + 
						orderReference + " from execution batch)");
				
				if (deal.isReplacer() && !orderReference.equals(deal.getReplacedOrderReference()))
					deleteOrderExecutionState(deal.getReplacedOrderReference());
							
				deals.remove(orderReference);	
			}
			return deals.size();
		}
	}

	public void addPosition(String instrumentId, double size)
	{
		positions.put(instrumentId, getPositionByInstrument(instrumentId) + size);
	}
	
	public void subPosition(String instrumentId, double size)
	{
		positions.put(instrumentId, getPositionByInstrument(instrumentId) - size);
	}
	
	public double getPositionByInstrument(String instrumentId)
	{
		if (!positions.containsKey(instrumentId))
			return 0;
		
		return positions.get(instrumentId);
	}
	
	public void updateOrderState(TradeOrder order, Trade tradeRep) {
		
		updatePosition(order, tradeRep);
		
		String instrumentId = order.getInstrumentId();
		String complexOrderReference = order.getComplexOrderReference();
		
		ComplexOrderExecution complexOrderExecutionByInstrument = getComplexOrderExecution(instrumentId, complexOrderReference);
		ComplexOrderExecution complexOrderExecution = this.getComplexOrderExecution(complexOrderReference);
		
		if (complexOrderExecutionByInstrument != null) {
			complexOrderExecutionByInstrument.update(order, tradeRep);
		}
		if (complexOrderExecution != null) {
			complexOrderExecution.update(order, tradeRep);
		}
	}
	
	public void checkAndDeleteComplexOrderExecution(String instrumentId, String reference) {
		ComplexOrderExecution complexOrderExecution = getComplexOrderExecution(reference);
		ComplexOrderExecution complexOrderExecutionByInstrument = getComplexOrderExecution(instrumentId, reference);
		if(complexOrderExecution != null && complexOrderExecution.getLeavesSize() == 0){
			deleteComplexOrderExecution(reference);
		}
		
		if(complexOrderExecutionByInstrument != null && complexOrderExecutionByInstrument.getLeavesSize() == 0){
			deleteComplexOrderExecution(instrumentId, reference);
		}
	}

	public void updatePosition(TradeOrder order, Trade tradeRep) {
		if (order.getOrderSide() == OrderSide.BUY) {
			this.addPosition(order.getInstrumentId(), tradeRep.getQuantity());
		}
		else if (order.getOrderSide() == OrderSide.SELL){
			this.subPosition(order.getInstrumentId(), tradeRep.getQuantity());
		}
	}
}
