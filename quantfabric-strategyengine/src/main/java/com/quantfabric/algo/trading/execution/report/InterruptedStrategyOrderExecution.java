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

import com.quantfabric.algo.trading.execution.tradeMonitor.OrderExecutionState.TradingStatus;

public class InterruptedStrategyOrderExecution extends CompletionStrategyOrderExecutionReport
{
	private TradingStatus tradingStatus;
	
	public InterruptedStrategyOrderExecution()
	{
		super();
		setTradingStatus(TradingStatus.Empty);
		setVenueText(null);
	}

	public InterruptedStrategyOrderExecution(String orderReference, 
			String complexOrderReference, int complexOrderLegId,
			TradingStatus tradingStatus, String venueText)
	{
		super(orderReference, complexOrderReference, complexOrderLegId);
		setTradingStatus(tradingStatus);
		setVenueText(venueText);
	}
	
	public InterruptedStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId, long roundTrip,
			long tradeOrderSignalSourceTimestamp, long marketReportSourceTimestamp, 
			long ackRoundTrip,
			TradingStatus tradingStatus, String venueText)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, roundTrip,
				tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp,
				ackRoundTrip );
		setTradingStatus(tradingStatus);
		setVenueText(venueText);
	}

	public TradingStatus getTradingStatus()
	{
		return tradingStatus;
	}

	public void setTradingStatus(TradingStatus tradingStatus)
	{
		this.tradingStatus = tradingStatus;
	}

	@Override
	public String toString()
	{
		return super.toString() + " tradingStatus=" + tradingStatus +";";
	}
}
