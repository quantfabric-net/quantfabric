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

public class NewSnapshot extends MDMessageInfo implements MDFeedEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5836075707370770357L;
	private String symbol;
	private int feedId;
	private String feedName;
	private String instrumentId;
	private int pointsInOne;
	private int feedGroupId;
	
	public NewSnapshot()
	{
		super();
	}
	
	public NewSnapshot(long timestamp, long messageId, String sourceName, long sourceTimestamp,
			int itemCount, String symbol, int feedId , String feedName)
	{
		super(timestamp, messageId, MDMessageType.SNAPSHOT, sourceName, sourceTimestamp, itemCount);
		setSymbol(symbol);
		setFeedId(feedId);
		setFeedName(feedName);
	}
	
	public NewSnapshot(long messageId, String sourceName, long sourceTimestamp, 
			int itemCount, String symbol, int feedId , String feedName)
	{
		super(messageId, MDMessageType.SNAPSHOT, sourceName, sourceTimestamp, itemCount);
		setSymbol(symbol);
		setFeedId(feedId);
		setFeedName(feedName);
	}
		
	public NewSnapshot(long messageId, String sourceName, Date sourceTimestamp, 
			int itemCount, String symbol, int feedId , String feedName)
	{
		super(messageId, MDMessageType.SNAPSHOT, sourceName, sourceTimestamp, itemCount);
		setSymbol(symbol);
		setFeedId(feedId);
		setFeedName(feedName);
	}

	public String getSymbol()
	{
		return symbol;
	}
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}
	public int getFeedId()
	{
		return feedId;
	}
	public void setFeedId(int feedId)
	{
		this.feedId = feedId;
	}
	public String getFeedName()
	{
		return feedName;
	}
	public void setFeedName(String feedName)
	{
		this.feedName = feedName;
	}
	public String getInstrumentId()
	{
		return instrumentId;
	}
	public void setInstrumentId(String instrumentId)
	{
		this.instrumentId = instrumentId;
	}
	public int getPointsInOne()
	{
		return pointsInOne;
	}
	public void setPointsInOne(int pointsInOne)
	{
		this.pointsInOne = pointsInOne;
	}
	public int getFeedGroupId()
	{
		return feedGroupId;
	}
	public void setFeedGroupId(int feedGroupId)
	{
		this.feedGroupId = feedGroupId;
	}
}
