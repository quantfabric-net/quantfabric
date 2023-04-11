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
package com.quantfabric.algo.order;

import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.OrderType;

public class IFDSettings
{
	private OrderType IFDIfType;
	private OrderType IFDThenType;
	private OrderSide IFDThenSide;
	private int IFDIfStopRate;
	private int IFDThenPrStopRate;
	private int IFDThenPrLimitRate;
	private OrderSide IFDThenPrStopSide;
	
	public IFDSettings(){}

	public OrderType getIFDIfType()
	{
		return IFDIfType;
	}

	public void setIFDIfType(OrderType iFDIfType)
	{
		IFDIfType = iFDIfType;
	}

	public OrderType getIFDThenType()
	{
		return IFDThenType;
	}

	public void setIFDThenType(OrderType iFDThenType)
	{
		IFDThenType = iFDThenType;
	}

	public OrderSide getIFDThenSide()
	{
		return IFDThenSide;
	}

	public void setIFDThenSide(OrderSide iFDThenSide)
	{
		IFDThenSide = iFDThenSide;
	}

	public int getIFDIfStopRate()
	{
		return IFDIfStopRate;
	}

	public void setIFDIfStopRate(int iFDIfStopRate)
	{
		IFDIfStopRate = iFDIfStopRate;
	}

	public int getIFDThenPrStopRate()
	{
		return IFDThenPrStopRate;
	}

	public void setIFDThenPrStopRate(int iFDThenPrStopRate)
	{
		IFDThenPrStopRate = iFDThenPrStopRate;
	}

	public int getIFDThenPrLimitRate()
	{
		return IFDThenPrLimitRate;
	}

	public void setIFDThenPrLimitRate(int iFDThenPrLimitRate)
	{
		IFDThenPrLimitRate = iFDThenPrLimitRate;
	}

	public OrderSide getIFDThenPrStopSide()
	{
		return IFDThenPrStopSide;
	}

	public void setIFDThenPrStopSide(OrderSide iFDThenPrStopSide)
	{
		IFDThenPrStopSide = iFDThenPrStopSide;
	}
	
}
