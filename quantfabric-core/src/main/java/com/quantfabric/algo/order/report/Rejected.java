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

public class Rejected extends Interrupted
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7410399257025072478L;

	public static final String DEFAULT_REASON = null;
	
	private String reason;
	
	public Rejected()
	{
		super();
	}
	
	public Rejected(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, String reason)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setReason(reason);
	}

	public Rejected(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, String reason)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setReason(reason);
	}
	
	public Rejected(long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, String reason)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setReason(reason);
	}

	public Rejected(long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, String reason)
	{
		super(messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID);
		setReason(reason);
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	@Override
	public ExecutionReportType getExecutionReportType()
	{
		return ExecutionReportType.Rejected;
	}

	@Override
	public OrderStatus getOrderStatus()
	{
		return OrderStatus.Rejected;
	}

	public String toString()
	{

        String result = super.toString() + ", Reason=" +
                getReason();
		
		return result;
	}
}
