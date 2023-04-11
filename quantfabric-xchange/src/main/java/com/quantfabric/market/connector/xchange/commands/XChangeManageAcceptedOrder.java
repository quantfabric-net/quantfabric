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
package com.quantfabric.market.connector.xchange.commands;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter;
import com.quantfabric.algo.market.gateway.commands.ManageAcceptedOrder;
import com.quantfabric.algo.order.TradeOrder;

public abstract class XChangeManageAcceptedOrder implements ManageAcceptedOrder, ConcreteCommand{

	private TradeOrder order;
	private String institutionOrderReference;
	
	public XChangeManageAcceptedOrder(TradeOrder order) {
		
		this(order, null);
	}
	
	public XChangeManageAcceptedOrder(TradeOrder order, String institutionOrderReference) {
		setOrder(order);
		setInstitutionOrderReference(institutionOrderReference);
	}
	
	public TradeOrder getOrder()
	{
		return order;
	}

	public void setOrder(TradeOrder order)
	{
		this.order = order;
	}
	
	public String getInstitutionOrderReference()
	{
		return institutionOrderReference;
	}
	
	public void setInstitutionOrderReference(String institutionOrderReference)
	{
		this.institutionOrderReference = institutionOrderReference;		
	}

	@Override
	public void execute(CommandExecutor commandExecuter) {
		
		VirtualCoinMarketAdapter marketAdapter = (VirtualCoinMarketAdapter)commandExecuter;
		String orderReference = getOrder().getOrderReference();
		
		marketAdapter.cancelOrder(orderReference);
	}
}
