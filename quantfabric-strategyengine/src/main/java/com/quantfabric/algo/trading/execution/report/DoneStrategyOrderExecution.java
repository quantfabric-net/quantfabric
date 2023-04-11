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

import com.quantfabric.algo.order.TradeOrder.OrderSide;

public class DoneStrategyOrderExecution extends CompletionStrategyOrderExecutionReport
{
	private int fillPrice;
	private double fillSize;
	private OrderSide tradeSide;
	
	public DoneStrategyOrderExecution()
	{
		super();
	}

	public DoneStrategyOrderExecution(
			String orderReference, String complexOrderReference, int complexOrderLegId,
			int fillPrice, double fillSize, OrderSide tradeSide)
	{
		super(orderReference, complexOrderReference, complexOrderLegId);
		setFillPrice(fillPrice);
		setFillSize(fillSize);
		setTradeSide(tradeSide);
	}
	
	public DoneStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId, long roundTrip,
			long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp,
			long ackRoundTrip,
			int fillPrice, double fillSize, OrderSide tradeSide)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, roundTrip,
				tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp,
				ackRoundTrip);
		setFillPrice(fillPrice);
		setFillSize(fillSize);
		setTradeSide(tradeSide);
	}
	
	public DoneStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId, long roundTrip,
			long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp,
			long ackRoundTrip,
			int fillPrice, double fillSize, OrderSide tradeSide, String venueText)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, roundTrip,
				tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp,
				ackRoundTrip);
		setFillPrice(fillPrice);
		setFillSize(fillSize);
		setTradeSide(tradeSide);
		setVenueText(venueText);
	}

	public int getFillPrice()
	{
		return fillPrice;
	}

	public void setFillPrice(int fillPrice)
	{
		this.fillPrice = fillPrice;
	}

	public double getFillSize()
	{
		return fillSize;
	}

	public void setFillSize(double fillSize)
	{
		this.fillSize = fillSize;
	}

	public OrderSide getTradeSide()
	{
		return tradeSide;
	}

	public void setTradeSide(OrderSide tradeSide)
	{
		this.tradeSide = tradeSide;
	}

	@Override
	public String toString()
	{
		return super.toString() + " fillPrice=" + fillPrice + "; fillSize=" + fillSize
				+ "; tradeSide=" + tradeSide + ";";
	}
	
	
}
