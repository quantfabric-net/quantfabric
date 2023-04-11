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
package com.quantfabric.algo.trading.execution.report;

public class ReplacedStrategyOrderExecution extends StrategyOrderExecutionReport
{
	private long price;
	private long stopPrice;
	private double size;	
	private String originalOrderReference;
	
	public ReplacedStrategyOrderExecution()
	{
		super();
	}
	
	public ReplacedStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId, 
			int price, int stopPrice,
			double size, String originalOrderReference)
	{
		super(orderReference, complexOrderReference, complexOrderLegId);
		this.price = price;
		this.stopPrice = stopPrice;
		this.size = size;
		this.originalOrderReference = originalOrderReference;
	}
	
	public ReplacedStrategyOrderExecution(String orderReference,
										  String complexOrderReference, int complexOrderLegId,
										  long roundTrip,
										  long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp,
										  long price, long stopPrice,
										  double size, String originalOrderReference)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, 
				roundTrip, tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp);
		this.price = price;
		this.stopPrice = stopPrice;
		this.size = size;
		this.originalOrderReference = originalOrderReference;
	}

	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
	{
		this.price = price;
	}

	public long getStopPrice()
	{
		return stopPrice;
	}

	public void setStopPrice(long stopPrice)
	{
		this.stopPrice = stopPrice;
	}

	public double getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public String getOriginalOrderReference()
	{
		return originalOrderReference;
	}

	public void setOriginalOrderReference(String originalOrderReference)
	{
		this.originalOrderReference = originalOrderReference;
	}

	@Override
	public String toString()
	{
		return super.toString() + " price=" + price
				+ "; stopPrice=" + stopPrice + "; size=" + size
				+ "; originalOrderReference=" + originalOrderReference
				+ ";";
	}
	
	
}
