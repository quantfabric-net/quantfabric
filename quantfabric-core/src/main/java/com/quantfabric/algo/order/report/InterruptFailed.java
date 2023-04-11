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

public class InterruptFailed extends OrderExecutionReport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8281680261376480407L;
	private int rejectReason = 0;
	private OrderStatus orderStatus = OrderStatus.Rejected;
	
	public InterruptFailed(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, DEFAULT_EXECUTION_ID);
	}

	public InterruptFailed(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, DEFAULT_EXECUTION_ID);
	}

	public InterruptFailed(long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, DEFAULT_EXECUTION_ID);
	}

	public InterruptFailed(long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, DEFAULT_EXECUTION_ID);
	}
	
	@Override
	public ExecutionReportType getExecutionReportType()
	{
		return ExecutionReportType.Rejected;
	}

	@Override
	public OrderStatus getOrderStatus()
	{		
		return orderStatus;
	}
	
	public void setOrderStatus(OrderStatus orderStatus)
	{
		this.orderStatus = orderStatus;
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
