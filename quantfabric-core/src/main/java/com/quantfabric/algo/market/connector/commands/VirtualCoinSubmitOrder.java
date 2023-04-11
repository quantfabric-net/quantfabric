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
package com.quantfabric.algo.market.connector.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter;
import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter.BTCTypeConverter.BTCConversionError;
import com.quantfabric.algo.market.gateway.MarketAdapter.MarketAdapterException;
import com.quantfabric.algo.market.gateway.commands.SubmitOrder;
import com.quantfabric.algo.order.TradeOrder;
import org.knowm.xchange.dto.trade.LimitOrder;

public abstract class VirtualCoinSubmitOrder implements ConcreteCommand, SubmitOrder {

	private static final Logger log = LoggerFactory.getLogger(VirtualCoinSubmitOrder.class);
	private TradeOrder order;

	public VirtualCoinSubmitOrder(TradeOrder order) {
		
		super();
		this.order = order;
	}
	
	@Override
	public TradeOrder getOrder()
	{
		return order;
	}

	@Override
	public void setOrder(TradeOrder order)
	{
		this.order = order;
	}

	@Override
	public void execute(CommandExecutor commandExecuter) {
		
		try {
			VirtualCoinMarketAdapter marketAdapter = (VirtualCoinMarketAdapter)commandExecuter;
			LimitOrder limitOrder = createOrder(marketAdapter);		
			String orderId = marketAdapter.sendMessage(limitOrder, getOrder());
			onSend(marketAdapter, orderId, getOrder());
		} catch (BTCConversionError e) {
			log.error("can't convert : " + this, e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Can't execute command.", e);
		}		
	}
	
	protected abstract LimitOrder createOrder(VirtualCoinMarketAdapter marketAdapter) throws Exception;
	protected void onSend(VirtualCoinMarketAdapter marketAdapter, String orderId, TradeOrder tradeOrder) throws MarketAdapterException	{	
		
		if (orderId != null)
			marketAdapter.addOrder(orderId, tradeOrder);
	}
}
