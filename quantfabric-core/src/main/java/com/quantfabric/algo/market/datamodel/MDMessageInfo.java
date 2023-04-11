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
package com.quantfabric.algo.market.datamodel;

import java.util.Date;

public class MDMessageInfo extends MDEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1337766104693433837L;
	public enum MDMessageType
	{
		UNKNOWN,
		SNAPSHOT,
		INCREMENTAL_REFRESH,
		EXECUTION_REPORT
	}
	
	public static final long DEFAULT_MESSAGE_ID = -1L;
	
	private long messageId;
	
	private String sourceName;
	private long sourceTimestamp;
	private MDMessageType messageType = MDMessageType.UNKNOWN;
	private long messageLatency = 0;
	
	private int itemCount = 0;
	
	public MDMessageInfo() {
		super();
		this.messageId=DEFAULT_MESSAGE_ID;
		this.sourceName="";
	}
	public MDMessageInfo(long timestamp, long messageId, MDMessageType messageType, 
			String sourceName, long sourceTimestamp, int itemCount)
	{
		super(timestamp);
		init(messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
	
	public MDMessageInfo(long timestamp, long messageId, MDMessageType messageType, 
			String sourceName, Date sourceTimestamp, int itemCount)
	{
		this(timestamp, messageId, messageType, sourceName, sourceTimestamp.getTime(), itemCount);
	}
	
	public MDMessageInfo(long messageId, MDMessageType messageType, String sourceName, 
			long sourceTimestamp, int itemCount)
	{
		super();
		init(messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
	
	public MDMessageInfo(long messageId, MDMessageType messageType, String sourceName, 
			Date sourceTimestamp, int itemCount)
	{
		this(messageId, messageType, sourceName, sourceTimestamp.getTime(), itemCount);
	}
	
	private void init(long messageId, MDMessageType messageType, String sourceName, 
			long sourceTimestamp, int itemCount)
	{
		this.messageId=messageId;
		this.sourceName=sourceName;
		this.sourceTimestamp=sourceTimestamp;
		this.messageType=messageType;
		this.itemCount = itemCount;
	}
	
	public long getMessageId()
	{
		return messageId;
	}
	
	public void setMessageId(long messageId)
	{	
		this.messageId = messageId;
	}

	public String getSourceName()
	{
		return sourceName;
	}

	public void setSourceName(String sourceName)
	{
		this.sourceName = sourceName;
	}

	public long getSourceTimestamp()
	{
		return sourceTimestamp;
	}

	public void setSourceTimestamp(long sourceTimestamp)
	{
		this.sourceTimestamp = sourceTimestamp;
	}

	public MDMessageType getMessageType()
	{
		return messageType;
	}

	public void setMessageType(MDMessageType messageType)
	{
		this.messageType = messageType;
	}
	public int getItemCount()
	{
		return itemCount;
	}
	public void setItemCount(int itemCount)
	{
		this.itemCount = itemCount;
	}
	public long getMessageLatency()
	{
		return messageLatency;
	}
	public void setMessageLatency(long messageLatency)
	{
		this.messageLatency = messageLatency;
	}
}
