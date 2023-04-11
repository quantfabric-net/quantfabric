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
import java.util.Date;
import java.util.GregorianCalendar;

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.Accepted;
import com.quantfabric.algo.order.report.Filled;
import com.quantfabric.algo.order.report.Interrupted;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.algo.order.report.PartialFilled;
import com.quantfabric.algo.order.report.Replaced;
import com.quantfabric.algo.order.report.Trade;
import com.quantfabric.algo.trading.execution.RoutedTradeOrder;
import com.quantfabric.algo.trading.execution.commands.ReplaceStrategyOrderCommand;

public class OrderExecutionState
{
	public enum ExecutionStatus
	{
		OrderAcceptPending,
		Active,			
		OrderCancelPending,
		OrderReplacePending,
		Replaced,
		Done,
		Interrupted
	}
	
	public enum TradingStatus
	{
		Empty,
		PartialFilled,
		Filled
	}
	
	private final RoutedTradeOrder order;
	private String executionPoint;
	
	private String institutionOrderReference;
	
	private String replacedOrderReference;
	private String replacerOrderReference;
	private ReplaceStrategyOrderCommand replaceCommand;
	
	private boolean isReplaced;
	private boolean isReplacer;

	private boolean completed;
	private Date timeOfComplete;
	
	private final ArrayList<OrderExecutionReport> executionHistory = new ArrayList<OrderExecutionReport>();
	private ExecutionStatus executionStatus;
	private TradingStatus tradingStatus;
	private long ackRoundTrip;
	
	public OrderExecutionState(RoutedTradeOrder routedTradeOrder)
	{
		this.order = routedTradeOrder;
		setExecutionPoint(routedTradeOrder.getExecutionPoint());
		setExecutionStatus(ExecutionStatus.OrderAcceptPending);
		setTradingStatus(TradingStatus.Empty);
		setReplaced(false);
	}
	
	public OrderExecutionState(RoutedTradeOrder order, String institutionOrderReference,
			OrderExecutionState replacedOrderExecutionState)
	{
		this.order = order;
		setInstitutionOrderReference(institutionOrderReference);
		setExecutionStatus(ExecutionStatus.Active);
		setTradingStatus(replacedOrderExecutionState.getTradingStatus());		
		setReplaced(false);
		setReplacer(true);
		setReplacedOrderReference(replacedOrderExecutionState.getOrder().getOrderReference());
		setAckRoundTrip(replacedOrderExecutionState.getAckRoundTrip());
		setExecutionPoint(replacedOrderExecutionState.getExecutionPoint());
	}
	
	private void addExecutionReport(OrderExecutionReport orderExecutionReport)
	{
		executionHistory.add(orderExecutionReport);
	}	
	
	public long getAckRoundTrip()
	{
		return ackRoundTrip;
	}
	
	private void setAckRoundTrip(long ackRoundTrip)
	{
		this.ackRoundTrip = ackRoundTrip;
	}
	
	public TradeOrder getOrder()
	{
		return order.getOrder();
	}
	
	public String getOrderExecutionPoint()
	{
		return order.getExecutionPoint();
	}
	
	public String getExecutionPoint()
	{
		return executionPoint;
	}

	private void setExecutionPoint(String executionPoint)
	{
		this.executionPoint = executionPoint;
	}

	private void setExecutionStatus(ExecutionStatus dealStatus)
	{
		this.executionStatus = dealStatus;
	}
	
	public ExecutionStatus getExecutionStatus()
	{
		return executionStatus;
	}
		
	public TradingStatus getTradingStatus()
	{
		return tradingStatus;
	}

	private void setTradingStatus(TradingStatus tradingStatus)
	{
		this.tradingStatus = tradingStatus;
	}
	
	public String getInstitutionOrderReference()
	{
		return institutionOrderReference;
	}

	private void setInstitutionOrderReference(String institutionOrderReference)
	{
		this.institutionOrderReference = institutionOrderReference;
	}

	public String getReplacedOrderReference()
	{
		return replacedOrderReference;
	}

	private void setReplacedOrderReference(String replacementOrderReference)
	{
		this.replacedOrderReference = replacementOrderReference;
	}
	
	public boolean isReplaced()
	{
		return isReplaced;
	}

	private void setReplaced(boolean isReplaced)
	{		
		this.isReplaced = isReplaced;
	}

	
	public String getReplacerOrderReference()
	{
		return replacerOrderReference;
	}

	public void setReplacerOrderReference(String replacerOrderReference)
	{
		this.replacerOrderReference = replacerOrderReference;
	}

	public boolean isReplacer()
	{
		return isReplacer;
	}

	public void setReplacer(boolean isReplacer)
	{
		this.isReplacer = isReplacer;
	}

	private static long calculateRoundTripLatency(
			TradeOrder order,
			OrderExecutionReport orderExecutionReport)
	{
		return orderExecutionReport.getSourceTimestamp() - 
			order.getSignalSourceTimestamp();
	}
	
	public void orderAccepted(Accepted acceptedRep)
	{
		addExecutionReport(acceptedRep);
		setExecutionStatus(ExecutionStatus.Active);
		setInstitutionOrderReference(acceptedRep.getInstitutionOrderReference());
		setAckRoundTrip(calculateRoundTripLatency(getOrder(), acceptedRep));
	}
	
	public void orderCancelPending()
	{
		setExecutionStatus(ExecutionStatus.OrderCancelPending);
	}
			
	public ReplaceStrategyOrderCommand getReplaceCommand()
	{
		return replaceCommand;
	}

	public void orderReplacePending(ReplaceStrategyOrderCommand replaceCommand)
	{
		setExecutionStatus(ExecutionStatus.OrderReplacePending);
		
		this.replaceCommand = replaceCommand;
	}
	
	public void orderReplaceRejected()
	{
		if (getExecutionStatus() == ExecutionStatus.OrderReplacePending)
			setExecutionStatus(ExecutionStatus.Active);
		
		this.replaceCommand = null;
	}
	
	public void orderInterrupted(Interrupted interruptedRep)
	{
		addExecutionReport(interruptedRep);
		setExecutionStatus(ExecutionStatus.Interrupted);
	}
	
	public void addTrade(Trade tradeRep)
	{
		addExecutionReport(tradeRep);
		
		if (tradeRep instanceof Filled)
		{
			setExecutionStatus(ExecutionStatus.Done);
			setTradingStatus(TradingStatus.Filled);
		}
		else
			if (tradeRep instanceof PartialFilled)
				setTradingStatus(TradingStatus.PartialFilled);
	}
 
	/**
	 * @return new TradeOrder which replaced current in OrderExecutionState
	 */
	public RoutedTradeOrder orderReplaced(Replaced replacedRep)
	{
		addExecutionReport(replacedRep);
		setReplacerOrderReference(replacedRep.getLocalOrderReference());
		setReplaced(true);
				
		TradeOrder replacedOrder = (TradeOrder)getOrder().clone();
		
		replacedOrder.setPrice(replacedRep.getPrice());
		replacedOrder.setStopPrice(replacedRep.getStopPrice());
		replacedOrder.setSize(replacedRep.getQuantity());
		replacedOrder.setOrderReference(replacedRep.getLocalOrderReference());		
		
		return new RoutedTradeOrder(getExecutionPoint(), replacedOrder);
	}

	public void markCompleted()
	{
		synchronized (this)
		{
			this.completed = true;
			this.timeOfComplete = GregorianCalendar.getInstance().getTime();
		}
	}

	public boolean isCompleted()
	{
		synchronized (this)
		{
			return completed;
		}
	}
	
	public Date getTimeOfComplete()
	{
		synchronized (this)
		{
			return timeOfComplete;
		}
	}
}
