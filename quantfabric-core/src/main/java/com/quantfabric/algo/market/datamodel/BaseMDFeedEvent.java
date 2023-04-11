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

import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

public class BaseMDFeedEvent extends MDEvent implements MDFeedEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6934373882707300176L;
	private String symbol;
	private int feedId;
	private String instrumentId;
	private int pointsInOne;
	private int feedGroupId;
	private String feedName;
	
	public BaseMDFeedEvent()
	{
		super();
	}
	
	public BaseMDFeedEvent(MarketDataFeed marketDataFeed)
	{
		super();
		pupulate(marketDataFeed);
	}
	
	public BaseMDFeedEvent(MDFeedEvent event)
	{
		super();
		pupulate(event);
	}
	
	public void pupulate(MarketDataFeed marektDataFeed)
	{
		setSymbol(marektDataFeed.getInstrument().getSymbol());
		setFeedId(marektDataFeed.getFeedId());
		setFeedGroupId(marektDataFeed.getFeedGroupId());
		setFeedName(marektDataFeed.getFeedName().getName());
		setInstrumentId(marektDataFeed.getInstrumentId());
		setPointsInOne(marektDataFeed.getInstrument().getPointsInOne());
	}
	
	public void pupulate(MDFeedEvent event)
	{
		setSymbol(event.getSymbol());
		setFeedId(event.getFeedId());
		setFeedGroupId(event.getFeedGroupId());
		setFeedName(event.getFeedName());
		setInstrumentId(event.getInstrumentId());
		setPointsInOne(event.getPointsInOne());
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
	public String getInstrumentId()
	{
		return instrumentId;
	}
	public void setInstrumentId(String instrumentId)
	{
		this.instrumentId = instrumentId;
	}
	
	@Override
	public int getPointsInOne()
	{
		return pointsInOne;
	}

	@Override
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
	public String getFeedName()
	{
		return feedName;
	}
	public void setFeedName(String feedName)
	{
		this.feedName = feedName;
	}
}
