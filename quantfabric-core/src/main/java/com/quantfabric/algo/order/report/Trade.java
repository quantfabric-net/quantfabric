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

public abstract class Trade extends OrderExecutionReport
{
	private static final long serialVersionUID = -1117801127996560170L;
	
	private long price;
	private double quantity;
	private Integer averagePrice=null;
	private Double cumulativeQty=null;
	
	public Integer getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(Integer averagePrice) {
		this.averagePrice = averagePrice;
	}

	public Double getCumulativeQty() {
		return cumulativeQty;
	}

	public void setCumulativeQty(Double cumulativeQty) {
		this.cumulativeQty = cumulativeQty;
	}
	
	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
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

	public Trade()
	{
		super();
	}
	
	public Trade(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, 
			long price, double quantity)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setPrice(price);
		setQuantity(quantity);
	}

	public Trade(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID,
			long price, double quantity)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setPrice(price);
		setQuantity(quantity);
	}
	
	public Trade(long messageId, String sourceName,
                 Date sourceTimestamp, String institutionOrderReference,
                 String localOrderReference, String executionID,
                 long price, double quantity)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setPrice(price);
		setQuantity(quantity);
	}

	public Trade(long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID,
			long price, double quantity)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setPrice(price);
		setQuantity(quantity);
	}

	@Override
	public ExecutionReportType getExecutionReportType()
	{
		return ExecutionReportType.Fill;
	}

	@Override
	public String toString()
	{
        String result = super.toString() + ", Quantity=" +
                quantity +
                ", Price=" +
                price;
		
		return result;
	}
}
