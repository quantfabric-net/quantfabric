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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.report.Trade;

public class ComplexOrderExecution implements ExecutionReport
{
	private final List<String> tradeOrderReferences = new ArrayList<String>();
	
	private String complexOrderReference;
	private String instrumentId;
	private double expectedSize;
	private double lastFillSize;
	private int lastFillLegId;
	private double leavesSize;
	
	private final Object sigmaMutex = new Object();
	private BigDecimal sigmaFilledVWPrice = BigDecimal.ZERO;
	private BigDecimal sigmaFilledQty = BigDecimal.ZERO;
	
	boolean instrumentIdInitialized = false;
	
	public ComplexOrderExecution(String complexOrderReference)
	{
		super();
		this.complexOrderReference = complexOrderReference;
	}

	public String getComplexOrderReference()
	{
		return complexOrderReference;
	}

	public void setComplexOrderReference(String complexOrderReference)
	{
		this.complexOrderReference = complexOrderReference;
	}

	public double getExpectedSize()
	{
		return expectedSize;
	}

	public void setExpectedSize(double expectedSize)
	{
		this.expectedSize = expectedSize;
	}

	public double getLastFillSize()
	{
		return lastFillSize;
	}

	public void setLastFillSize(double lastFillSize)
	{
		this.lastFillSize = lastFillSize;
	}

	public double getLeavesSize()
	{
		return leavesSize;
	}

	public void setLeavesSize(double leavesSize)
	{
		this.leavesSize = leavesSize;
	}	
		
	public String getInstrumentId()
	{
		return instrumentId;
	}

	public void setInstrumentId(String instrumentId)
	{
		this.instrumentId = instrumentId;
	}

	public int getLastFillLegId()
	{
		return lastFillLegId;
	}

	public void setLastFillLegId(int lastFillLegId)
	{
		this.lastFillLegId = lastFillLegId;
	}

	public void update(TradeOrder order, Trade trade)
	{
		if (!instrumentIdInitialized)
		{
			this.instrumentId = order.getInstrumentId();
			instrumentIdInitialized = true;
		}
		
		if (!tradeOrderReferences.contains(order.getOrderReference()))
		{
			tradeOrderReferences.add(order.getOrderReference());			
			
			if (order.getOrderSide() == OrderSide.BUY)
			{
				this.expectedSize += order.getSize();
			}
			else if (order.getOrderSide() == OrderSide.SELL)
			{
				this.expectedSize -= order.getSize();
			}
		}
		
		this.lastFillLegId = order.getComplexOrderLegId();
		
		switch (order.getOrderSide())
		{
			case BUY:
					this.lastFillSize = trade.getQuantity();
					this.leavesSize += trade.getQuantity();
				break;
			case SELL:
					this.lastFillSize = - trade.getQuantity();
					this.leavesSize -= trade.getQuantity();
				break;
		}
		
		synchronized (sigmaMutex) {
			BigDecimal quantity = BigDecimal.valueOf(trade.getQuantity());
			BigDecimal price = BigDecimal.valueOf(trade.getPrice());
			this.sigmaFilledVWPrice = this.sigmaFilledVWPrice.add(quantity.multiply(price));
			this.sigmaFilledQty = this.sigmaFilledQty.add(quantity);
		}
	}
	
	public int getAvaragePrice() {
		synchronized (sigmaMutex) {
			return sigmaFilledVWPrice.divide(sigmaFilledQty).intValue();
		}
	}
	
	public double getCumulativeQty() {
		synchronized (sigmaMutex) {
			return sigmaFilledQty.doubleValue();
		}
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " complexOrderReference=" + complexOrderReference
				+ "; expectedSize=" + expectedSize + ";"
				+ "; lastFillSize=" + lastFillSize + ";"
				+ "; leavesSize=" + leavesSize + ";"
				+ "; tradeOrderReferences=" + tradeOrderReferences + ";"
				+ "; sigmaFilledVWPrice=" + sigmaFilledVWPrice + ";"
				+ "; sigmaFIlledQty=" + sigmaFilledQty + ";";
	}
}
