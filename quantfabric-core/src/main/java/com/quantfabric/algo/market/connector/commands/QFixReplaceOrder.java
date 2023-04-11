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

import com.quantfabric.algo.market.gateway.commands.ReplaceOrder;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderType;

public abstract class QFixReplaceOrder extends QFixManageAcceptedOrder implements ReplaceOrder
{
	private String replacementOrderReference;
	private OrderType replacementOrderType;
	private double replacementSize;
	private double orderFilledSize;
	private int replacementPrice;
	private int replacementPrice2;
	private int replacementStopPrice;
	private boolean requiredInstitutionOrderReference;
	
	public QFixReplaceOrder(TradeOrder order, 
			String replacementOrderReference, 
			int replacementPrice, int replacementStopPrice,
			double replacementSize)
	{
		super(order);
		setRequiredInstitutionOrderReference(false);
		setReplacementOrderReference(replacementOrderReference);
		setReplacementPrice(replacementPrice);
		setReplacementStopPrice(replacementStopPrice);
		setReplacementSize(replacementSize);
	}
		
	public QFixReplaceOrder(TradeOrder order,
			String institutionOrderReference,
			String replacementOrderReference,
			int replacementPrice, int replacementStopPrice,
			double replacementSize)
	{
		super(order, institutionOrderReference);
		setRequiredInstitutionOrderReference(true);
		setReplacementOrderReference(replacementOrderReference);
		setReplacementPrice(replacementPrice);
		setReplacementStopPrice(replacementStopPrice);
		setReplacementSize(replacementSize);
	}

	public String getReplacementOrderReference()
	{
		return replacementOrderReference;
	}

	public void setReplacementOrderReference(String replacementOrderReference)
	{
		this.replacementOrderReference = replacementOrderReference;
	}

	public double getReplacementSize()
	{
		return replacementSize;
	}

	public void setReplacementSize(double replacementSize)
	{
		this.replacementSize = replacementSize;
	}

	public void setOrderFilledSize(double orderFilledSize)
	{
		this.orderFilledSize = orderFilledSize;
	}
	public double getOrderFilledSize()
	{
		return orderFilledSize;
	}
	
	public int getReplacementPrice()
	{
		return replacementPrice;
	}

	public void setReplacementPrice(int replacementPrice)
	{
		this.replacementPrice = replacementPrice;
	}
	
	public int getReplacementPrice2()
	{
		return replacementPrice2;
	}

	public void setReplacementPrice2(int replacementPrice2)
	{
		this.replacementPrice2 = replacementPrice2;
	}

	public int getReplacementStopPrice()
	{
		return replacementStopPrice;
	}

	public void setReplacementStopPrice(int replacementStopPrice)
	{
		this.replacementStopPrice = replacementStopPrice;
	}

	public boolean isRequiredInstitutionOrderReference()
	{
		return requiredInstitutionOrderReference;
	}

	public void setRequiredInstitutionOrderReference(
			boolean requiredInstitutionOrderReference)
	{
		this.requiredInstitutionOrderReference = requiredInstitutionOrderReference;
	}

	public OrderType getReplacementOrderType()
	{
		return replacementOrderType;
	}

	public void setReplacementOrderType(OrderType replacementOrderType)
	{
		this.replacementOrderType = replacementOrderType;
	}
}
