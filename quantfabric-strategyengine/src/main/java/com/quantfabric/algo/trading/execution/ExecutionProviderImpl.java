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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.commands.Command;
import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.gateway.LoanCancelProvider;
import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.algo.market.gateway.commands.CancelOrderCommand;
import com.quantfabric.algo.market.gateway.commands.ConfirmTradeCommand;
import com.quantfabric.algo.market.gateway.commands.RejectTradeCommand;
import com.quantfabric.algo.market.gateway.commands.ReplaceOrderCommand;
import com.quantfabric.algo.market.gateway.commands.SubmitOrderCommand;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.algo.trading.Messages;
import com.quantfabric.algo.trading.execution.commands.CancelStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ConfirmTradeStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ManageStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.RejectTradeStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ReplaceStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.report.InterruptedStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.RejectedManageStrategyOrderExecution;
import com.quantfabric.algo.trading.execution.report.TradeReport;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.ExecutionStatus;
import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.TradingStatus;
import com.quantfabric.algo.trading.execution.tradeMonitor.TradeMonitor;
import com.quantfabric.algo.trading.strategy.ExecutionPoint;
import com.quantfabric.algo.trading.strategy.ExecutionPointImpl;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategy.exceptions.QuantfabricStrategyRuntimeException;
import com.quantfabric.algo.trading.strategyrunner.StrategyRunner;
import com.quantfabric.messaging.Subscriber;
import com.quantfabric.messaging.SubscriberBuffer;
import com.quantfabric.net.stream.DataStreamer;
import com.quantfabric.util.Converter;


public class ExecutionProviderImpl implements ExecutionProvider,Subscriber<Object> 
{
	public class GatewayExecutionService
	{
		public CommandExecutor commandExecutor;
		public LoanCancelProvider loanCancelProvider;
		public MarketFeeder marketFeeder;
	}
	
	private boolean isStarted;
	private final TradingStrategy strategy;
	private final SubscriberBuffer<Object> subscriberBuffer;
	
	private final StrategyRunner runtime;
	private final Map<String, GatewayExecutionService> gatewayExecutionServices = new HashMap<String, GatewayExecutionService>();
	private static final Logger log = LoggerFactory.getLogger(ExecutionProviderImpl.class);
	private final TradeMonitor tradeMonitor;
	
	private DataStreamer strategyDataStreamer = null;
	
	public ExecutionProviderImpl(TradingStrategy strategy, StrategyRunner runtime)
	{	
		this(strategy, runtime, null);		
	}
	
	public ExecutionProviderImpl(TradingStrategy strategy, StrategyRunner runtime, DataStreamer strategyDataStreamer)
	{	
		super();
		this.strategy = strategy;
		this.subscriberBuffer = 
			new SubscriberBuffer<Object>("ExecutionPovider-" + strategy.getName(), this);
		this.tradeMonitor = new TradeMonitor(strategy.getName(), this);
		this.runtime=runtime;
		this.strategyDataStreamer = strategyDataStreamer;
	}
	
	public void setStrategyDataStreamer(DataStreamer strategyDataStreamer)
	{
		this.strategyDataStreamer = strategyDataStreamer;
	}

	@Override
	public synchronized void start()
	{
		if (!isStarted)
		{
			if (strategyDataStreamer != null)
			{
				strategyDataStreamer.start();
			}
			
			gatewayExecutionServices.clear();			
			for (ExecutionPoint ep : strategy.getExecutionEndPoints())
			{
				try
				{
					if (Boolean.parseBoolean(
							((ExecutionPointImpl) ep).getExecutionSettingByName("fillCheck")
					))
						tradeMonitor.setDoubleCheckRequired(true);
					
					GatewayExecutionService gatewayExecutionService = new GatewayExecutionService();
					
					gatewayExecutionService.marketFeeder = 
						runtime.getMarketGateway().getMarketFeeder(ep.getConnection());					
					gatewayExecutionService.marketFeeder.subscribe(subscriberBuffer, new FeedNameImpl("exec_reports"));					
									
					gatewayExecutionService.commandExecutor = 
						runtime.getMarketGateway().getMarketCommandExecutor(ep.getConnection());					
					
					gatewayExecutionService.loanCancelProvider=
						runtime.getMarketGateway().getLoanCancelProvider(ep.getConnection());
					
					gatewayExecutionServices.put(ep.getTargetMarket(), gatewayExecutionService);
				}
				catch (Exception ex)
				{
					String errMsg = String.format(
							Messages.ERR_STR_EXECPROV_START,
							strategy.getName(), ep.getTargetMarket());
					log.error(errMsg, ex);
					throw new QuantfabricStrategyRuntimeException(errMsg, ex);
				}
			}
			isStarted = true;
			log.info("ExecutionProvider (strategy=" + strategy.getName()
					+ ") started.");
		}
	}
	@Override
	public synchronized void stop()
	{
		if (isStarted)
		{
			if (strategyDataStreamer != null)
				strategyDataStreamer.stop();
			
			for (ExecutionPoint ep : strategy.getExecutionEndPoints())
			{
				try
				{
					MarketFeeder marketFeeder = runtime.getMarketGateway().getMarketFeeder(ep.getConnection());
					marketFeeder.unSubscribe(subscriberBuffer, new FeedNameImpl("exec_reports"));
				}
				catch (Exception ex)
				{
					log.error(String.format(Messages.ERR_STR_EXECPROV_STOP,
							strategy.getName(), ep.getTargetMarket()), ex);
				}
			}
			isStarted = false;
			log.info("ExecutionProvider (strategy=" + strategy.getName()
					+ ") was stoped.");
		}
	}
	
	public LoanCancelProvider getLoanCancelProvider(String executionPoint)
	{
		LoanCancelProvider loanCancelProvider = 
			getGatewayExecutionService(executionPoint).loanCancelProvider;
		
		if (loanCancelProvider == null)
			log.error("Can't find LoanCancelProvider for execution point \"" + executionPoint + "\"");
			
		return loanCancelProvider;		
	}
	
	@Override
	public void sendUpdate(Object data)
	{	try 
		{	
			if (data instanceof OrderExecutionReport)
			{					
				tradeMonitor.appendExecutionReport((OrderExecutionReport)data);
				
				sendToStrategy(data);
			}
			else
				if (data instanceof StatusChanged)
				{
					sendToStrategy(data);
				}
		}
		catch(Exception ex) 
		{
			log.error(String.format(Messages.ERR_STR_EXEC_NOTIFICATION,strategy.getName()),ex);
		}
	}
	@Override
	public void sendUpdate(Object[] data)
	{	
		for (Object objData: data)
			sendUpdate(objData);
	}
	
	public void sendToStrategy(Object data)
	{
		strategy.sendUpdate(data);	
		sendToExecutionDataStream(data);
	}
	
	public void sendToStrategyDataStream(Object data)
	{		
		if (strategyDataStreamer != null)	
			strategyDataStreamer.getDataSubscriber().sendUpdate(data);
	}
		
	public void sendToExecutionDataStream(Object data)
	{		
		sendToStrategyDataStream(data);
	}
	
	protected GatewayExecutionService getGatewayExecutionService(String executionPoint)
	{
		if (gatewayExecutionServices.containsKey(executionPoint))
			return gatewayExecutionServices.get(executionPoint);
		else
		{
			log.error("Can't find Gateway Execution Service for execution point \"" + executionPoint + "\"");
			return null;
		}
	}
	
	private void rejectOrder(RoutedTradeOrder routedTradeOrder, String reason)
	{
		TradeOrder order = routedTradeOrder.getOrder();
		
		sendToExecutionDataStream(order);
		
		sendToStrategy(new InterruptedStrategyOrderExecution(
				order.getOrderReference(), order.getComplexOrderReference(), 
				order.getComplexOrderLegId(),
				0,
				order.getSignalSourceTimestamp(),
				System.currentTimeMillis(),
				0,
				TradingStatus.Empty, reason));						
	}
	
	private void rejectManagement(ManageStrategyOrderCommand command, String reason, int reasonCode)
	{
		RejectedManageStrategyOrderExecution report = 
				new RejectedManageStrategyOrderExecution(
						command.getOriginalOrderReference(), 
						command.getComplexOrderReference(), 
						command.getComplexOrderLegId(),
						0,
						0,
						System.currentTimeMillis());
			
			report.setVenueText(reason);
			report.setRejectReason(reasonCode);
								
		sendToStrategy(report);		
	}
	
	protected void sendToExecutor(String executionPoint, Command command)
	{
		CommandExecutor commandExecutor = gatewayExecutionServices.get(executionPoint).commandExecutor;
		if (commandExecutor != null)
			commandExecutor.execute(command);
		else
			log.error("Can't find CommandExecutior for execution point \"" + executionPoint + "\"");
	}
	
	@Override
	public void update(String executionPoint, TradeOrder order)
	{
		if (order == null)
		{
			log.error("Incoming order is null");
			return;
		}
		
		RoutedTradeOrder routedTradeOrder = new RoutedTradeOrder(executionPoint, order);
		
		if(isStarted && strategy.isExecutionAllowed())
		{			
			log.debug(String.format("Strategy sent TradeOrder : %s", Converter.toString(order)));
			try 
			{					
				tradeMonitor.addNewOrder(routedTradeOrder);
				
				Command command = new SubmitOrderCommand(order);				
				sendToExecutor(executionPoint, command);				
				sendToExecutionDataStream(routedTradeOrder);
			}
			catch(Exception ex) {
				log.error(String.format(Messages.ERR_STR_EXEC_COMM,strategy.getName(),"SubmitOrderCommand"),ex);
			}
			
		}
		else
			rejectOrder(routedTradeOrder, "Execution is inactive");
	}
		
	@Override
	public void update(String executionPoint, CancelStrategyOrderCommand command)
	{
		if (command == null)
		{
			log.error("Incoming command is null");
			return;
		}
		
		if(isStarted && strategy.isExecutionAllowed())
		{
			if (command.isIndetifiedByComplexAttributes())
				log.debug(String.format("Strategy sent cancel order (%s, %d) command", 
						command.getComplexOrderReference(), command.getComplexOrderLegId()));
			else
				log.debug(String.format("Strategy sent cancel order (%s) command", 
					command.getOriginalOrderReference()));
			try 
			{
				OrderExecutionState orderExecutionState = null;
				
				if (command.isIndetifiedByComplexAttributes())
				{
					orderExecutionState = tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionStateByComplexAttr(
								command.getComplexOrderReference(), 
								command.getComplexOrderLegId());
					if (orderExecutionState != null)
						command.setOriginalOrderReference(
							orderExecutionState.getOrder().getOrderReference());
				}
				else
					orderExecutionState = tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionState(command.getOriginalOrderReference());	
				
				if (orderExecutionState != null)
				{
					if (orderExecutionState.getExecutionStatus() != ExecutionStatus.Done &&
						orderExecutionState.getExecutionStatus() != ExecutionStatus.Interrupted)
					{
						if (orderExecutionState.getExecutionStatus() != ExecutionStatus.OrderCancelPending &&
							orderExecutionState.getExecutionStatus() != ExecutionStatus.OrderReplacePending)
						{						
							tradeMonitor.orderCanceling(command);				
										
							sendToExecutor(executionPoint, 
									new CancelOrderCommand(orderExecutionState.getOrder(), 
											orderExecutionState.getInstitutionOrderReference()));
						}
						else
						{
							log.error(String.format("Order (%s) is currently pending Cancel/Replace. Current execution status - %s", 
									command.getOriginalOrderReference(), orderExecutionState.getExecutionStatus()));
							
							rejectManagement(command, "Order already in pending status", 3/*OrderAlreadyInPendingStatus*/);
						}
					}
					else
					{
						log.error(String.format("Order (%s) isn't Active. Current execution status - %s", 
								command.getOriginalOrderReference(), orderExecutionState.getExecutionStatus()));
						
						rejectManagement(command, "Order isn't active", 0/*TooLateToCancel*/);
					}
				}
				else
				{
					log.error(String.format("Order (%s) does not exist in current batch", 
							command.isIndetifiedByComplexAttributes() ? 
									(command.getComplexOrderReference() + ", " + 
											command.getComplexOrderLegId()) :
								command.getOriginalOrderReference()));
					
					rejectManagement(command, "Unknown order", 1/*Unknown order*/);
				}
			}
			catch(Exception ex) 
			{
				log.error(String.format(Messages.ERR_STR_EXEC_COMM,strategy.getName(),"CancelOrderCommand"),ex);
			}
		}
		else
			rejectManagement(command, "Execution is inactive", 99/*Other*/);
	}

	@Override
	public void update(String executionPoint, ReplaceStrategyOrderCommand command)
	{
		if (command == null)
		{
			log.error("Incoming command is null");
			return;
		}
		
		if(isStarted && strategy.isExecutionAllowed())
		{
			if (command.isIndetifiedByComplexAttributes())
				log.debug(String.format("Strategy's sended replace order (complexAttrs:%s, %d, suffix:%s) command", 
						command.getComplexOrderReference(), command.getComplexOrderLegId(),
						command.getReplacingOrderReferenceSuffix()));
			else
				log.debug(String.format("Strategy's sended replace order (original:%s, new:%s) command", 
						command.getOriginalOrderReference(), command.getOrderReference()));
			try {
				OrderExecutionState orderExecutionState = null;
				
				if (command.isIndetifiedByComplexAttributes())
				{
					orderExecutionState = tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionStateByComplexAttr(
								command.getComplexOrderReference(), 
								command.getComplexOrderLegId());
					if (orderExecutionState != null)
					{
						command.setOrderReference(
								orderExecutionState.getOrder().getOrderReference() + 
									command.getReplacingOrderReferenceSuffix());
						command.setOriginalOrderReference(
								orderExecutionState.getOrder().getOrderReference());
					}
				}
				else
					orderExecutionState = tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionState(command.getOriginalOrderReference());			
												
				if (orderExecutionState != null)
				{
					if (orderExecutionState.getExecutionStatus() != ExecutionStatus.Done &&
						orderExecutionState.getExecutionStatus() != ExecutionStatus.Interrupted)
					{
						if (orderExecutionState.getExecutionStatus() != ExecutionStatus.OrderCancelPending &&
								orderExecutionState.getExecutionStatus() != ExecutionStatus.OrderReplacePending)
						{
							tradeMonitor.orderReplacing(command);					
							
							ReplaceOrderCommand gatewayCommand =
								new ReplaceOrderCommand(orderExecutionState.getOrder());
							
							if (orderExecutionState.getInstitutionOrderReference() != null)
							{
								gatewayCommand.setInstitutionOrderReference(
										orderExecutionState.getInstitutionOrderReference());
								gatewayCommand.setRequiredInstitutionOrderReference(true);
							}
							else
								gatewayCommand.setRequiredInstitutionOrderReference(false);
							
							gatewayCommand.setReplacementOrderReference(command.getOrderReference());
							gatewayCommand.setReplacementPrice(command.getPrice());
							gatewayCommand.setReplacementStopPrice(command.getStopPrice());
							gatewayCommand.setReplacementSize(command.getSize());
							
							sendToExecutor(executionPoint, gatewayCommand);
						}
						else
						{
							log.error(String.format("Order (%s) is currently pending Cancel/Replace. Current execution status - %s", 
									command.getOriginalOrderReference(), orderExecutionState.getExecutionStatus()));
							
							rejectManagement(command, "Order already in pending status", 3/*OrderAlreadyInPendingStatus*/);
						}
					}
					else 
					{
						log.error(String.format("Order (%s) isn't Active. Current execution status - %s", 
								command.getOriginalOrderReference(), orderExecutionState.getExecutionStatus()));
						
						rejectManagement(command, "Order isn't active", 0/*TooLateToCancel*/);
					}
				}
				else
				{
					log.error(String.format("Order (%s) does not exist in current batch", 
							command.isIndetifiedByComplexAttributes() ? 
									(command.getComplexOrderReference() + ", " + 
											command.getComplexOrderLegId()) :
								command.getOriginalOrderReference()));
					
					rejectManagement(command, "Unknown order", 1/*Unknown order*/);
				}
			}
			catch(Exception ex) 
			{
				log.error(String.format(Messages.ERR_STR_EXEC_COMM,strategy.getName(),"ReplaceOrderCommand"),ex);
			}
		}
		else
			rejectManagement(command, "Execution is inactive", 99/*Other*/);
	}
		
	@Override
	public void update(String executionPoint,
			ConfirmTradeStrategyOrderCommand command)
	{
		if (command == null)
		{
			log.error("Incoming command is null");
			return;
		}
		
		if(isStarted && strategy.isExecutionAllowed())
		{
			log.debug(String.format("Strategy sent trade confirmation (%s) command", 
					command.getOriginalOrderReference()));
			try {
				OrderExecutionState orderExecutionState =  
					tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionState(command.getOriginalOrderReference());													
							
				sendToExecutor(executionPoint, 
						new ConfirmTradeCommand(orderExecutionState.getOrder(), 
								orderExecutionState.getInstitutionOrderReference(),
								command.getFillExecutionId()));
			}
			catch(Exception ex) 
			{
				log.error(String.format(Messages.ERR_STR_EXEC_COMM,strategy.getName(),"ConfirmTradeCommand"),ex);
			}	
		}
		else
			rejectManagement(command, "Execution is inactive", 99/*Other*/);
	}

	@Override
	public void update(String executionPoint,
			RejectTradeStrategyOrderCommand command)
	{			
		if (command == null)
		{
			log.error("Incoming command is null");
			return;
		}
		
		if(isStarted && strategy.isExecutionAllowed())
		{
			log.debug(String.format("Strategy sent trade rejection (%s) command", 
					command.getOriginalOrderReference()));
			try {
				OrderExecutionState orderExecutionState =  
					tradeMonitor.getCurrentOrderBatch().
						getOrderExecutionState(command.getOriginalOrderReference());													
							
				sendToExecutor(executionPoint, 
						new RejectTradeCommand(orderExecutionState.getOrder(), 
								orderExecutionState.getInstitutionOrderReference(),
								command.getFillExecutionId()));
			}
			catch(Exception ex) 
			{
				log.error(String.format(Messages.ERR_STR_EXEC_COMM,strategy.getName(),"RejectTradeCommand"),ex);
			}	
		}
		else
			rejectManagement(command, "Execution is inactive", 99/*Other*/);
	}

	@Override
	public String toString()
	{
		return super.toString() + " - (strategyName=" + strategy.getName() + 
			"; tradeMonitor=" + tradeMonitor.toString() + ")";
	}

	@Override
	public TradeMonitor getTradeMonitor()
	{
		return tradeMonitor;
	}

	@Override
	public void update(TradeReport tradeReport) {
		if (tradeReport == null) {
			log.error("Incoming tradeReport is null");
			return;
		}
		
		sendToStrategyDataStream(tradeReport);
	}
}
