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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.Accepted;
import com.quantfabric.algo.order.report.Filled;
import com.quantfabric.algo.order.report.InterruptFailed;
import com.quantfabric.algo.order.report.Interrupted;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.algo.order.report.OrderExecutionReport.OrderStatus;
import com.quantfabric.algo.order.report.PartialFilled;
import com.quantfabric.algo.order.report.Rejected;
import com.quantfabric.algo.order.report.Replaced;
import com.quantfabric.algo.order.report.SoftFilled;
import com.quantfabric.algo.order.report.SoftPartialFilled;
import com.quantfabric.algo.order.report.Trade;
import com.quantfabric.algo.trading.execution.ExecutionProviderImpl;
import com.quantfabric.algo.trading.execution.RoutedTradeOrder;
import com.quantfabric.algo.trading.execution.commands.CancelStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ReplaceStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.report.AcceptStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.ComplexOrderExecution;
import com.quantfabric.algo.trading.execution.report.DoneStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.ExecutionReport;
import com.quantfabric.algo.trading.execution.report.InstrumentRiskValue;
import com.quantfabric.algo.trading.execution.report.InterruptedStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.RejectedManageStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.ReplacedStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.SoftFillStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.ExecutionStatus;

public class TradeMonitor
{
	public interface TripMeterMXBean
	{
		int getMesurementCount();
		long getTotalTripTime();
		long getMeanValue();
		long getMaxValue();
		long getMinValue();
	}
	
	public static class TripMeter implements TripMeterMXBean
	{
		private int mesurementCount = 0;
		private long totalTripTime = 0L;
		private long minValue = 0L;
		private long maxValue = 0L;
		
		public void addMesurement(long value)
		{
			mesurementCount++;
			totalTripTime += value;
			
			if (value > maxValue)
				maxValue = value;
			
			if (minValue <= 0 || value < minValue)
				minValue = value;				
		}
		
		@Override
		public int getMesurementCount()
		{
			return mesurementCount;
		}
		
		@Override
		public long getTotalTripTime()
		{
			return totalTripTime;
		}
		
		@Override
		public long getMeanValue()
		{
			if (mesurementCount == 0)
				return 0L;
			
			return totalTripTime / mesurementCount;
		}

		@Override
		public long getMaxValue()
		{
			return maxValue;
		}

		@Override
		public long getMinValue()
		{
			return minValue;
		}
	}
	
	private final Vector<ExecutionBatch> orderBatches = new Vector<ExecutionBatch>();
	private final static Logger log = LoggerFactory.getLogger(TradeMonitor.class);
	private final TripMeter tripMeter = new TripMeter();
	private final String name;
	private final ExecutionProviderImpl executionProvider;
	private boolean isDoubleCheckRequired = false;
	
	public TradeMonitor(String name, ExecutionProviderImpl executionProvider)
	{
		this.name = name;
		this.executionProvider = executionProvider;
	}
	
	public final TripMeter getTripMeter()
	{
		return tripMeter;
	}

	public String getName()
	{
		return name;
	}
	
	public ExecutionBatch newOrderBatch()
	{
		synchronized (orderBatches)
		{
			orderBatches.add(new ExecutionBatch());
			return getCurrentOrderBatch();
		}		
	}
	
	public ExecutionBatch getCurrentOrderBatch()
	{
		synchronized (orderBatches)
		{
			if (orderBatches.isEmpty())
				//add default OrderBatch
				newOrderBatch();
			
			return orderBatches.lastElement();
		}
	}
	
	public void addNewOrder(RoutedTradeOrder routedTradeOrder)
	{
		synchronized (this)
		{	
			getCurrentOrderBatch().newOrderExecution(routedTradeOrder);
		}
	}
	
	public void addNewOrder(RoutedTradeOrder order, String institutionOrderReference, 
			OrderExecutionState replacedOrderExecutionState)
	{
		synchronized (this)
		{
			getCurrentOrderBatch().newOrderExecution(
				order, institutionOrderReference, replacedOrderExecutionState);
		}			
	}
	
	public void orderCanceling(TradeOrder order)
	{
		orderCanceling(order.getOrderReference());
	}
		
	public void orderCanceling(String orderReference)
	{
		synchronized (this)
		{
			getCurrentOrderBatch().getOrderExecutionState(orderReference).orderCancelPending();
		}
	}
	
	public void orderCanceling(CancelStrategyOrderCommand cancelCommand)
	{
		orderCanceling(cancelCommand.getOriginalOrderReference());
	}
		
	public void appendExecutionReport(OrderExecutionReport orderExecutionReport)
	{
		synchronized (this)
		{		
			log.debug("Incoming execution report : " + orderExecutionReport);
			
			if (orderExecutionReport instanceof Accepted)
				handleAccptedReport((Accepted)orderExecutionReport);
			else
				if (orderExecutionReport instanceof Interrupted)
					handleInterruptedReport((Interrupted)orderExecutionReport);
				else
					if (orderExecutionReport instanceof Trade)
						handleTradeReport((Trade)orderExecutionReport);
					else
						if (orderExecutionReport instanceof Replaced)
							handleReplacedOrder((Replaced)orderExecutionReport);
						else
							if (orderExecutionReport instanceof InterruptFailed)
								handleInterruptFailed((InterruptFailed)orderExecutionReport);
		}		
	}

	private void handleInterruptFailed(InterruptFailed interruptFailedRep)
	{
		String orderRef = interruptFailedRep.getOriginalLocalOrderReference();
		/*if (orderRef == null)
			orderRef = interruptFailedRep.getLocalOrderReference();*/
		
		OrderExecutionState executionState = 
			getCurrentOrderBatch().getOrderExecutionState(orderRef);		
		
		if (executionState != null)
		{		
			TradeOrder order = executionState.getOrder();
			
			RejectedManageStrategyOrderExecution report = 
				new RejectedManageStrategyOrderExecution(
						order.getOrderReference(), 
						order.getComplexOrderReference(), 
						order.getComplexOrderLegId(),
						calculateRoundTripLatency(order, interruptFailedRep),
						order.getSignalSourceTimestamp(),
						interruptFailedRep.getSourceTimestamp());
			
			report.setVenueText(interruptFailedRep.getText());
			report.setRejectReason(interruptFailedRep.getRejectReason());
								
			if (executionState.getReplaceCommand() !=null)
			{
				ReplaceStrategyOrderCommand replaceCommand = executionState.getReplaceCommand();
				double failedReplaceSize = replaceCommand.getSize();
				executionProvider.getLoanCancelProvider(executionState.getExecutionPoint()).
				cancelLoanByReplaceFailed(order, interruptFailedRep, failedReplaceSize);
				executionState.orderReplaceRejected();
			}			
		
			sendReportToStrategy(report);
			
			sendInstrumentRisk(order.getInstrument());
				
		}
		else
		{
			log.warn("Can't find execution state of order for InterruptFailed report. Order maybe done early.");
			sendReportToStrategy(
					new RejectedManageStrategyOrderExecution(
						interruptFailedRep.getOriginalLocalOrderReference(), 
						null, 
						0,
						0L,
						0L,
						interruptFailedRep.getSourceTimestamp()));
		}
		
	}

	private void handleReplacedOrder(Replaced replacedRep)
	{
		String orderReference = replacedRep.getOriginalLocalOrderReference();
		OrderExecutionState executionState = 
			getCurrentOrderBatch().getOrderExecutionState(orderReference);
		
		if (executionState == null)
			log.error("Obtained Replaced Report (ExecutionId=" + replacedRep.getExecutionID() + 
					" for unknown Order (" + orderReference + "). Replaced Report will skipped.");
		else
		{
			RoutedTradeOrder newRoutedOrder = executionState.orderReplaced(replacedRep);
			
			addNewOrder(newRoutedOrder, replacedRep.getInstitutionOrderReference(), executionState);
			
			TradeOrder newOrder = newRoutedOrder.getOrder();
			
			sendReportToStrategy(
					new ReplacedStrategyOrderExecution(
							newOrder.getOrderReference(), 
							executionState.getOrder().getComplexOrderReference(), 
							executionState.getOrder().getComplexOrderLegId(), 
							calculateRoundTripLatency(newOrder, replacedRep),
							newOrder.getSignalSourceTimestamp(),
							replacedRep.getSourceTimestamp(),
							newOrder.getPrice(), 
							newOrder.getStopPrice(), 
							newOrder.getSize(), 
							executionState.getOrder().getOrderReference()));
			
			sendInstrumentRisk(newOrder.getInstrument());			
		}
	}

	private void handleTradeReport(Trade tradeRep) {
		ExecutionBatch currentBatch = getCurrentOrderBatch();
		String orderReference = getOrderReference(tradeRep);
		OrderExecutionState executionState = currentBatch.getOrderExecutionState(orderReference);
		
		if (executionState == null) {
			log.error("Obtained Trade Report (ExecutionId=" + tradeRep.getExecutionID() + 
					" for unknown Order (" + orderReference + "). Trade Report will skipped.");
			return ;
		}
		/*else if (isDoubleCheckRequired())
			if (executionState.isCompleted())
				log.error("Obtained Trade Report (ExecutionId=" + tradeRep.getExecutionID() + " for already completed Order ("
						+ orderReference + "). Trade Report will skipped.");*/
		
		if (tradeRep.getClass() == Filled.class || tradeRep.getClass() == PartialFilled.class) {
			
			executionState.addTrade(tradeRep);
			TradeOrder order = executionState.getOrder();
			String complexOrderReference = order.getComplexOrderReference();
			String instrumentId = order.getInstrumentId();
			
			currentBatch.updateOrderState(order, tradeRep);
			
			ComplexOrderExecution complexOrderExecution = currentBatch.getComplexOrderExecution(complexOrderReference);
			if(complexOrderExecution != null) {
				sendReportToStrategy(complexOrderExecution);
			}
			
			currentBatch.checkAndDeleteComplexOrderExecution(instrumentId, complexOrderReference);
			
			if (executionState.getExecutionStatus() == ExecutionStatus.Done) {
				executionState.markCompleted();
				executionProvider.getLoanCancelProvider(executionState.getExecutionPoint()).cancelLoanByTrade(order, tradeRep);					
				
				Integer price = tradeRep.getAveragePrice();
				Double quantity = tradeRep.getCumulativeQty();
				if(price == null ||  quantity == null){
					price = complexOrderExecution.getAvaragePrice();
					quantity = complexOrderExecution.getCumulativeQty();
				}
				sendReportToStrategy(new DoneStrategyOrderExecution(
						order.getOrderReference(), complexOrderReference, 
						order.getComplexOrderLegId(),
						calculateRoundTripLatency(order, tradeRep),
						order.getSignalSourceTimestamp(),
						tradeRep.getSourceTimestamp(),
						executionState.getAckRoundTrip(),
						price, quantity, 
						order.getOrderSide(), extractText(tradeRep)));			
									
				sendInstrumentRisk(order.getInstrument());
			}
		}
		else if (tradeRep.getClass() == SoftFilled.class || tradeRep.getClass() == SoftPartialFilled.class) {
				TradeOrder order = executionState.getOrder();
				sendReportToStrategy(new SoftFillStrategyOrderExecution(
						order.getOrderReference(), order.getComplexOrderReference(), 
						order.getComplexOrderLegId(),
						calculateRoundTripLatency(order, tradeRep),
						order.getSignalSourceTimestamp(),
						tradeRep.getSourceTimestamp(),
						executionState.getAckRoundTrip(),
						tradeRep.getExecutionID(),
						tradeRep.getPrice(), tradeRep.getQuantity(),
						tradeRep.getOrderStatus() == OrderStatus.ConfirmPartialFillPending));
			}
	}

	private String extractText(Trade tradeRep) {
		String text = "";
		if (tradeRep.getText() != null){
			text = tradeRep.getText();
		}
		return text;
	}
	
	private void handleInterruptedReport(Interrupted interruptedRep)
	{
		ExecutionBatch currentBatch = getCurrentOrderBatch();
		String orderReference = interruptedRep.getLocalOrderReference();
		OrderExecutionState executionState = 
			currentBatch.getOrderExecutionState(orderReference);
		
		if (executionState == null)
			log.error("Obtained Interrupted Report (ExecutionId=" + interruptedRep.getExecutionID() + 
					" for unknown Order (" + orderReference + "). Interrupted Report will skipped.");
		else
		{						
			executionState.orderInterrupted(interruptedRep);
			
			TradeOrder order = executionState.getOrder();	
			
			String text = "";
			
			if (interruptedRep instanceof Rejected)
			{
				Rejected rejectedRep = (Rejected)interruptedRep;
				if (rejectedRep.getReason() != Rejected.DEFAULT_REASON)
					text = rejectedRep.getReason();
			}
			
			if (interruptedRep.getText() != null)
				text += interruptedRep.getText();
			
			executionState.markCompleted();
			
			executionProvider.getLoanCancelProvider(executionState.getExecutionPoint()).
				cancelLoanByInterrupted(order, interruptedRep);
			
			sendReportToStrategy(new InterruptedStrategyOrderExecution(
					order.getOrderReference(), order.getComplexOrderReference(), 
					order.getComplexOrderLegId(),
					calculateRoundTripLatency(order, interruptedRep),
					order.getSignalSourceTimestamp(),
					interruptedRep.getSourceTimestamp(),
					executionState.getAckRoundTrip(),
					executionState.getTradingStatus(), text));			
						
			sendInstrumentRisk(order.getInstrument());
			
			tripMeter.addMesurement(executionState.getAckRoundTrip());
		}
	}	
	
	private void handleAccptedReport(Accepted acceptedRep)
	{
		String orderReference = getOrderReference(acceptedRep);   
		OrderExecutionState executionState = 
			getCurrentOrderBatch().getOrderExecutionState(orderReference);
		
		if (executionState == null)
			log.error("Obtained Accepted Report (ExecutionId=" + acceptedRep.getExecutionID() + 
					" for unknown Order (" + orderReference + "). Accepted Report will skipped.");
		else
		{
			executionState.orderAccepted(acceptedRep);
					
			TradeOrder order = executionState.getOrder();
			
			String text = "";
			if (acceptedRep.getText() != null)
				text = acceptedRep.getText();
			
			sendReportToStrategy(new AcceptStrategyOrderExecution(
					order.getOrderReference(), order.getComplexOrderReference(),
					order.getComplexOrderLegId(),
					calculateRoundTripLatency(order, acceptedRep),
					order.getSignalSourceTimestamp(),
					acceptedRep.getSourceTimestamp(), text));		
			
			sendInstrumentRisk(order.getInstrument());
		}
	}
	
	private void sendReportToStrategy(ExecutionReport report)
	{
		log.debug("send report to strategy : " + report);
		executionProvider.sendToStrategy(report);
	}
		
	private void sendInstrumentRisk(Instrument instrument)
	{
		double riskValue = 0;
		for (OrderExecutionState executionState : getCurrentOrderBatch().getActiveOrders(instrument, false, false))
			if (executionState != null)
				riskValue += executionState.getOrder().getSize();
			
		sendReportToStrategy(new InstrumentRiskValue(instrument.getId(), riskValue));
	}
	
	private static String getOrderReference(OrderExecutionReport orderExecutionReport)
	{
		return orderExecutionReport.getLocalOrderReference();
	}
	
	public boolean isDoubleCheckRequired() {
		return isDoubleCheckRequired;
	}

	public void setDoubleCheckRequired(boolean isDoubleCheckRequired) {
		this.isDoubleCheckRequired = isDoubleCheckRequired;
	}

	private static long calculateRoundTripLatency(
			TradeOrder order,
			OrderExecutionReport orderExecutionReport)
	{
		long latency = orderExecutionReport.getSourceTimestamp() - order.getSignalSourceTimestamp();
		log.debug(String.format("RoundTrip Latency (Order:%s, ExecutionReport:%s) report.sourceTimestamp (%d) - order.signalSourceTimestamp (%d) = %d" , 
				orderExecutionReport.getLocalOrderReference(), 
				orderExecutionReport.getExecutionReportType().toString(),
				orderExecutionReport.getSourceTimestamp(),
				order.getSignalSourceTimestamp(),
				latency));
		return latency; 
	}

	public void orderReplacing(String originalOrderReference, ReplaceStrategyOrderCommand replaceCommmand)
	{
		synchronized (this)
		{
			getCurrentOrderBatch().getOrderExecutionState(originalOrderReference).
				orderReplacePending(replaceCommmand);	
		}
	}
	
	public void orderReplacing(ReplaceStrategyOrderCommand replaceCommmand)
	{
		orderReplacing(replaceCommmand.getOriginalOrderReference(),
				replaceCommmand);		
	}
}
