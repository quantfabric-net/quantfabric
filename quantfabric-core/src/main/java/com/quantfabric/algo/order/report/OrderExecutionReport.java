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

import com.quantfabric.algo.market.datamodel.MDMessageInfo;

public abstract class OrderExecutionReport extends MDMessageInfo
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4914706724130840434L;

	public enum ExecutionReportType
	{
		New,
		Fill,
		Canceled,
		PendingCancel,
		Replace,
		Rejected,
		Suspended,
		Expired
	}
	
	public enum OrderStatus
	{
		New,
		PartialFilled,
		Filled,
		Canceled,
		PendingCancel,
		Replaced,
		Rejected,
		Expired,
		ConfirmPartialFillPending,
		ConfirmFillPending
	}
	
	public static final String DEFAULT_EXECUTION_ID = null;
	
	private String institutionOrderReference;
	private String localOrderReference;
	private String originalLocalOrderReference;
	private String executionID;
	private String text;
	private Date doneTransationTime;
	
	public abstract ExecutionReportType getExecutionReportType();
	
	public abstract OrderStatus getOrderStatus();
	
		
	public OrderExecutionReport()
	{
		super();
	}
	
	public OrderExecutionReport(long timestamp, long messageId,
			String sourceName, Date sourceTimestamp, 
			String institutionOrderReference, String localOrderReference, String executionID)
	{
		super(timestamp, messageId, MDMessageType.EXECUTION_REPORT, sourceName, sourceTimestamp, 0);
		this.institutionOrderReference = institutionOrderReference;
		this.localOrderReference = localOrderReference;
		this.executionID = executionID;
	}
	
	public OrderExecutionReport(long timestamp, long messageId,
			String sourceName, long sourceTimestamp, 
			String institutionOrderReference, String localOrderReference, String executionID)
	{
		super(timestamp, messageId, MDMessageType.EXECUTION_REPORT, sourceName, sourceTimestamp, 0);
		this.institutionOrderReference = institutionOrderReference;
		this.localOrderReference = localOrderReference;
		this.executionID = executionID;
	}
	
	public OrderExecutionReport(long messageId,
			String sourceName, Date sourceTimestamp, 
			String institutionOrderReference, String localOrderReference, String executionID)
	{
		super(messageId, MDMessageType.EXECUTION_REPORT, sourceName, sourceTimestamp, 0);
		this.institutionOrderReference = institutionOrderReference;
		this.localOrderReference = localOrderReference;
		this.executionID = executionID;
	}
	
	public OrderExecutionReport(long messageId,
			String sourceName, long sourceTimestamp, 
			String institutionOrderReference, String localOrderReference, String executionID)
	{
		super(messageId, MDMessageType.EXECUTION_REPORT, sourceName, sourceTimestamp, 0);
		this.institutionOrderReference = institutionOrderReference;
		this.localOrderReference = localOrderReference;
		this.executionID = executionID;
	}
	
	public void setText(String text)
	{
		this.text = text;
	}
	public String getText()
	{
		return text;
	}
	
	public String getInstitutionOrderReference()
	{
		return institutionOrderReference;
	}
	public void setInstitutionOrderReference(String institutionOrderReference)
	{
		this.institutionOrderReference = institutionOrderReference;
	}
	public String getLocalOrderReference()
	{
		return localOrderReference;
	}
	public void setLocalOrderReference(String localOrderReference)
	{
		this.localOrderReference = localOrderReference;
	}
	public String getExecutionID()
	{
		return executionID;
	}
	public void setExecutionID(String executionID)
	{
		this.executionID = executionID;
	}
	
	public String getOriginalLocalOrderReference()
	{
		return originalLocalOrderReference;
	}

	public void setOriginalLocalOrderReference(String originalLocalOrderReference)
	{
		this.originalLocalOrderReference = originalLocalOrderReference;
	}
	
	public Date getDoneTransationTime()
	{
		return doneTransationTime;
	}

	public void setDoneTransationTime(Date doneTransationTime)
	{
		this.doneTransationTime = doneTransationTime;
	}

	@Override
	public String toString()
	{
        String result = this.getClass().getSimpleName() + " : LocalTime=" +
                getTimestamp() +
                ", InstitutionOrderReference=" +
                institutionOrderReference +
                ", LocalOrderReference=" +
                localOrderReference +
                ", OriginalLocalOrderReference=" +
                originalLocalOrderReference +
                ", ExecutionID=" +
                executionID +
                ", ExecutionReportType=" +
                getExecutionReportType() +
                ", OrderStatus=" +
                getOrderStatus() +
                ", Text=" +
                text;
		
		return result;
	}	
}
