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

public abstract class StrategyOrderExecutionReport implements ExecutionReport
{	
	private String complexOrderReference;
	private String orderReference;
	private int complexOrderLegId;
	private long roundTrip; 
	private long tradeOrderSignalSourceTimestamp;
	private long marketReportSourceTimestamp;
	private String venueText;
	
	public StrategyOrderExecutionReport()
	{
		this(null, null, 0);
	}
	
	public StrategyOrderExecutionReport(String orderReference, 
			String complexOrderReference, int complexOrderLegId)
	{
		this(orderReference, complexOrderReference, complexOrderLegId, 0, 0, 0);
	}
	
	public StrategyOrderExecutionReport(String orderReference, 
			String complexOrderReference, int complexOrderLegId, long roundTrip,
			long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp)
	{
		setOrderReference(orderReference);
		setComplexOrderReference(complexOrderReference);
		setComplexOrderLegId(complexOrderLegId);
		setRoundTrip(roundTrip);
		setTradeOrderSignalSourceTimestamp(tradeOrderSignalSourceTimestamp);
		setMarketReportSourceTimestamp(marketReportSourceTimestamp);
	}
	
	public String getOrderReference()
	{
		return orderReference;
	}

	public void setOrderReference(String orderReference)
	{
		this.orderReference = orderReference;
	}

	public String getComplexOrderReference()
	{
		return complexOrderReference;
	}

	public void setComplexOrderReference(String complexOrderReference)
	{
		this.complexOrderReference = complexOrderReference;
	}
	
	public int getComplexOrderLegId()
	{
		return complexOrderLegId;
	}

	public void setComplexOrderLegId(int complexOrderLegId)
	{
		this.complexOrderLegId = complexOrderLegId;
	}
	
	public long getRoundTrip()
	{
		return roundTrip;
	}

	public void setRoundTrip(long roundTrip)
	{
		this.roundTrip = roundTrip;
	}
	
	public long getTradeOrderSignalSourceTimestamp()
	{
		return tradeOrderSignalSourceTimestamp;
	}

	public void setTradeOrderSignalSourceTimestamp(
			long tradeOrderSignalSourceTimestamp)
	{
		this.tradeOrderSignalSourceTimestamp = tradeOrderSignalSourceTimestamp;
	}

	public long getMarketReportSourceTimestamp()
	{
		return marketReportSourceTimestamp;
	}

	public void setMarketReportSourceTimestamp(long marketReportSourceTimestamp)
	{
		this.marketReportSourceTimestamp = marketReportSourceTimestamp;
	}
	
	public String getVenueText()
	{
		return venueText;
	}

	public void setVenueText(String text)
	{
		this.venueText = text;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + 
			" : orderReference=" + orderReference +";" +
			" complexOrderReference=" + complexOrderReference +";" +
			" complexOrderLegId=" + complexOrderLegId +";" +
			" roundTrip=" + roundTrip + ";" +
			" veuneText=" + venueText + ";";
	}
}
