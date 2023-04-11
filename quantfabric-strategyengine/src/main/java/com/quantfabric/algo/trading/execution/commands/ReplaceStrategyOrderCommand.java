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
package com.quantfabric.algo.trading.execution.commands;

public class ReplaceStrategyOrderCommand extends ManageStrategyOrderCommand
{
	private String orderReference; 	
	private int price;
	private int stopPrice;
	private double size;
	
	private String replacingOrderReferenceSuffix;
	
	public ReplaceStrategyOrderCommand()
	{
		super();		
		this.replacingOrderReferenceSuffix = "";
	}
	
	public ReplaceStrategyOrderCommand(
			String originalOrderReference, 
			String orderReference,
			int price, double size)
	{
		super(originalOrderReference);
		this.orderReference = orderReference;
		this.price = price;
		this.size = size;
	}
	
	public ReplaceStrategyOrderCommand(
			String originalOrderReference, 
			String orderReference,
			int price, int stopPrice, double size)
	{
		super(originalOrderReference);
		this.orderReference = orderReference;
		this.price = price;
		this.stopPrice = stopPrice;
		this.size = size;
	}
	
	public ReplaceStrategyOrderCommand(
			String complexOrderReference, 
			int complexOrderLegId,
			String replacingOrderReferenceSuffix,
			int price, int stopPrice, double size)
	{
		super(complexOrderReference, complexOrderLegId);
		this.replacingOrderReferenceSuffix = replacingOrderReferenceSuffix;
		this.price = price;
		this.stopPrice = stopPrice;
		this.size = size;
	}
	
	public int getPrice()
	{
		return price;
	}
	public void setPrice(int price)
	{
		this.price = price;
	}
	public int getStopPrice()
	{
		return stopPrice;
	}
	public void setStopPrice(int stopPrice)
	{
		this.stopPrice = stopPrice;
	}
	public double getSize()
	{
		return size;
	}
	public void setSize(double size)
	{
		this.size = size;
	}

	public String getOrderReference()
	{
		return orderReference;
	}

	public void setOrderReference(String orderReference)
	{
		this.orderReference = orderReference;
	}

	public String getReplacingOrderReferenceSuffix()
	{
		return replacingOrderReferenceSuffix;
	}

	public void setReplacingOrderReferenceSuffix(
			String replacingOrderReferenceSuffix)
	{
		this.replacingOrderReferenceSuffix = replacingOrderReferenceSuffix;
	}
}
