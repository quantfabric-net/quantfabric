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
package com.quantfabric.algo.market.gateway.commands;

import com.quantfabric.algo.commands.Command;
import com.quantfabric.algo.commands.CommandFactory;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;

import java.util.Collection;

public abstract class MarketAdapterCommandFactory implements CommandFactory
{
	public ConcreteCommand createSubscribe(MarketDataFeed feed) throws CommandFactoryException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("Subscribe"));
	}

    public  ConcreteCommand createBatchSubscribe(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) throws CommandFactoryException {
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("Subscribe"));
	}

    public ConcreteCommand createUnsubscribe(MarketDataFeed feed) throws CommandFactoryException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("Unsubscribe"));
	}

	public ConcreteCommand createBatchUnsubscribe(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) throws CommandFactoryException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("Unsubscribe"));
	}
	public ConcreteCommand createSubmitOrder(TradeOrder order) throws CommandFactoryException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("SubmitOrder"));
	}
	public ConcreteCommand createCancelOrder(TradeOrder order) throws CommandFactoryException
	{
		return createCancelOrder(order, null);
	}
	public ConcreteCommand createCancelOrder(TradeOrder order, String institutionOrderReference) throws CommandFactoryException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("CancelOrder"));
	}

	public ConcreteCommand createReplaceOrder(TradeOrder order,
			String institutionOrderReference, String replacementOrderReference,
			int replacementPrice,
			int replacementStopPrice, double replacementSize) 
				throws NotSupportCommandException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("ReplaceOrder"));
	}
	
	public ConcreteCommand createReplaceOrder(TradeOrder order,
			String replacementOrderReference, int replacementPrice,
			int replacementStopPrice, double replacementSize)
			throws NotSupportCommandException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("ReplaceOrder"));
	}
	
	public ConcreteCommand createUpdatePeggedOrder(TradeOrder order,
			String institutionOrderReference, String replacementOrderReference,
			int replacementPrice,
			int replacementStopPrice, double replacementSize) 
				throws NotSupportCommandException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("UpdatePeggedOrder"));
	}
	
	public ConcreteCommand createRejectTrade(TradeOrder order,
			String institutionOrderReference, String tradeExecutionId) throws NotSupportCommandException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("TradeReject"));
	}
	public ConcreteCommand createConfirmTrade(TradeOrder order,
			String institutionOrderReference, String tradeExecutionId) throws NotSupportCommandException
	{
		throw new NotSupportCommandException(
				NotSupportCommandException.generateMessage("TradeConfirm"));
	}
	
	public ConcreteCommand create(Command command) throws CommandFactoryException
	{
		if (command.getClass() == SubscribeCommand.class)
			return createSubscribe(((SubscribeCommand)command).getFeed());

		if(command.getClass() == BatchSubscribeCommand.class)
			return createBatchSubscribe(((BatchSubscribeCommand)command).getMdFeeds(),((BatchSubscribeCommand)command).getExFeeds());

		if (command.getClass() == UnsubscribeCommand.class)
			return createUnsubscribe(((UnsubscribeCommand)command).getFeed());

		if (command.getClass() == BatchUnsubscribeCommand.class)
			return createBatchUnsubscribe(((BatchUnsubscribeCommand)command).getMdFeeds(),((BatchUnsubscribeCommand)command).getExFeeds());

		if (command.getClass() == SubmitOrderCommand.class)
			return createSubmitOrder(((SubmitOrderCommand)command).getOrder()); 
		
		if (command.getClass() == CancelOrderCommand.class)
			return createCancelOrder(((CancelOrderCommand)command).getOrder(), 
					((CancelOrderCommand)command).getInstitutionOrderReference());
		
		if (command.getClass() == ConfirmTradeCommand.class)
			return createConfirmTrade(((ConfirmTradeCommand)command).getOrder(), 
					((ConfirmTradeCommand)command).getInstitutionOrderReference(), 
					((ConfirmTradeCommand)command).getTradeExecutionId());
	
		if (command.getClass() == RejectTradeCommand.class)
			return createRejectTrade(((RejectTradeCommand)command).getOrder(), 
					((RejectTradeCommand)command).getInstitutionOrderReference(), 
					((RejectTradeCommand)command).getTradeExecutionId());
		
		if (command.getClass() == ReplaceOrderCommand.class)
		{
			ReplaceOrder replaceOrder = ((ReplaceOrderCommand)command);
			
			ReplaceOrder concreteReplaceOrder = null;
			
			if (replaceOrder.isRequiredInstitutionOrderReference())
				concreteReplaceOrder = (ReplaceOrder) createReplaceOrder(replaceOrder.getOrder(), 
					replaceOrder.getInstitutionOrderReference(),
					replaceOrder.getReplacementOrderReference(),
					replaceOrder.getReplacementPrice(),
					replaceOrder.getReplacementStopPrice(),
					replaceOrder.getReplacementSize());
			else
				concreteReplaceOrder = (ReplaceOrder) createReplaceOrder(replaceOrder.getOrder(), 
						replaceOrder.getReplacementOrderReference(),
						replaceOrder.getReplacementPrice(),
						replaceOrder.getReplacementStopPrice(),
						replaceOrder.getReplacementSize());
			
			concreteReplaceOrder.setReplacementPrice2(replaceOrder.getReplacementPrice2());
			concreteReplaceOrder.setOrderFilledSize(replaceOrder.getOrderFilledSize());
			concreteReplaceOrder.setReplacementOrderType(replaceOrder.getReplacementOrderType());
			
			return (ConcreteCommand)concreteReplaceOrder;
		}
		
		if (command.getClass() == UpdatePeggedOrderCommand.class)
		{
			UpdatePeggedOrder updatePaggedOrder = ((UpdatePeggedOrderCommand)command);
			return createUpdatePeggedOrder(updatePaggedOrder.getOrder(), 
					updatePaggedOrder.getInstitutionOrderReference(),
					updatePaggedOrder.getReplacementOrderReference(),
					updatePaggedOrder.getReplacementPrice(),
					updatePaggedOrder.getReplacementStopPrice(),
					updatePaggedOrder.getReplacementSize());
		}
		
		return null;
	}
}
