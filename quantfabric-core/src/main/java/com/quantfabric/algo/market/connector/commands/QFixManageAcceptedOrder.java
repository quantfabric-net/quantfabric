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

import com.quantfabric.algo.market.gateway.commands.ManageAcceptedOrder;
import com.quantfabric.algo.order.TradeOrder;

public abstract class QFixManageAcceptedOrder 
	extends SendQFixMessage implements ManageAcceptedOrder
{
	private TradeOrder order;
	private String institutionOrderReference;
	
	public QFixManageAcceptedOrder(TradeOrder order)
	{
		this(order, null);
	}
	
	public QFixManageAcceptedOrder(TradeOrder order, String institutionOrderReference)
	{
		super();
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
}
