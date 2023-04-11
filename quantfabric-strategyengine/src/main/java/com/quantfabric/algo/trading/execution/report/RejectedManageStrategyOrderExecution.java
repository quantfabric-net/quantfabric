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

public class RejectedManageStrategyOrderExecution extends StrategyOrderExecutionReport
{
	private int rejectReason;
	
	public RejectedManageStrategyOrderExecution()
	{
		super();
	}

	public RejectedManageStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId,
			long roundTrip, long tradeOrderSignalSourceTimestamp,
			long marketReportSourceTimestamp)
	{
		super(orderReference, complexOrderReference, complexOrderLegId, roundTrip,
				tradeOrderSignalSourceTimestamp, marketReportSourceTimestamp);
	}

	public RejectedManageStrategyOrderExecution(String orderReference,
			String complexOrderReference, int complexOrderLegId)
	{
		super(orderReference, complexOrderReference, complexOrderLegId);
	}

	public int getRejectReason()
	{
		return rejectReason;
	}

	public void setRejectReason(int rejectReason)
	{
		this.rejectReason = rejectReason;
	}
}
