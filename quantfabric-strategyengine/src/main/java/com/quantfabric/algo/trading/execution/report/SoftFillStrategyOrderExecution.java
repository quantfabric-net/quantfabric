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

public class SoftFillStrategyOrderExecution extends CompletionStrategyOrderExecutionReport
{
	private long fillPrice;
	private double fillSize;
	private boolean partialFill;
	private String fillExecutionId;
	
	public SoftFillStrategyOrderExecution()
	{
		super();
	}

	public SoftFillStrategyOrderExecution(
			String orderReference, String complexOrderReference, int complexOrderLegId,
			String fillExecutionId, int fillPrice, double fillSize, boolean partialFill)
	{
		super(orderReference, complexOrderReference, complexOrderLegId);
		setFillPrice(fillPrice);
		setFillSize(fillSize);
		setPartialFill(partialFill);
		setFillExecutionId(fillExecutionId);
	}
	
	public SoftFillStrategyOrderExecution(String orderReference,
                                          String complexOrderReference, int complexOrderLegId, long roundTrip,
                                          long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp,
                                          long ackRoundTrip,
                                          String fillExecutionId, long fillPrice, double fillSize, boolean partialFill)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, roundTrip,
				tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp,
				ackRoundTrip);
		setFillPrice(fillPrice);
		setFillSize(fillSize);
		setPartialFill(partialFill);
		setFillExecutionId(fillExecutionId);
	}

	public long getFillPrice()
	{
		return fillPrice;
	}

	public void setFillPrice(long fillPrice)
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

	public boolean isPartialFill()
	{
		return partialFill;
	}

	public void setPartialFill(boolean partialFill)
	{
		this.partialFill = partialFill;
	}

	public String getFillExecutionId()
	{
		return fillExecutionId;
	}

	public void setFillExecutionId(String fillExecutionId)
	{
		this.fillExecutionId = fillExecutionId;
	}

	@Override
	public String toString()
	{
		return super.toString() + " fillPrice=" + fillPrice + "; fillSize=" + fillSize
				+ "; partialFill=" + partialFill + "; fillExecutionId="
				+ fillExecutionId + ";";
	}
	
	
}
