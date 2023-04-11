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
package com.quantfabric.algo.order.report;

import java.util.Date;

public class Replaced extends OrderExecutionReport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2135179756119278683L;
	private int price;
	private int stopPrice;
	private double quantity;
	
	public Replaced()
	{
		super();
	}
	
	public Replaced(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
	}

	public Replaced(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
	}
	
	public Replaced(long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
	}

	public Replaced(long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
	}

	public int getPrice()
	{
		return price;
	}

	public void setPrice(int price)
	{
		this.price = price;
	}

	public double getQuantity()
	{
		return quantity;
	}

	public void setQuantity(double quantity)
	{
		this.quantity = quantity;
	}
	
	public int getStopPrice()
	{
		return stopPrice;
	}

	public void setStopPrice(int stopPrice)
	{
		this.stopPrice = stopPrice;
	}

	@Override
	public ExecutionReportType getExecutionReportType()
	{
		return ExecutionReportType.Replace;
	}

	@Override
	public OrderStatus getOrderStatus()
	{
		return OrderStatus.Replaced;
	}

}
