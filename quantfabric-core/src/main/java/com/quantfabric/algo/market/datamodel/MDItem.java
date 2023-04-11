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

public class MDItem extends MDMessageInfo implements MDFeedEvent
{
	private static final long serialVersionUID = -5691640592560930351L;

	public enum MDItemType
	{
		//TODO: add new types (KNOWM)
		UNKNOWN,
		BID,
		ASK,
		OFFER,
		PAID,
		GIVEN, 
		EMPTY,
		TRADE,

		TOP_OF_BOOK
	}
	
	public static final String DEFAULT_MDITEM_ID = ""; 
	
	private String mdItemId = DEFAULT_MDITEM_ID;
	private MDItemType mdItemType = MDItemType.UNKNOWN; 
	private String symbol;
	private int feedId;
	private String instrumentId;
	private int pointsInOne;
	private int feedGroupId;
	private String feedName;
	private int itemIndex = 0;
	
	public MDItem() {
		super();
	}
	
	public MDItem(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String symbol, int feedId)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long messageId, MDMessageType messageType, String sourceName, long sourceTimestamp, 
			int itemCount, int itemIndex, MDItemType mdItemType, String symbol, int feedId)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long messageId, MDMessageType messageType, String sourceName, Date sourceTimestamp, 
			int itemCount, int itemIndex, MDItemType mdItemType, String symbol, int feedId)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}	
	
	public MDItem(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String mdItemId, String symbol, int feedId)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setMdItemId(mdItemId);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long messageId, MDMessageType messageType, String sourceName, long sourceTimestamp, 
			int itemCount, int itemIndex, MDItemType mdItemType, String mdItemId, String symbol, int feedId)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setMdItemId(mdItemId);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String mdItemId, String symbol, int feedId)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setMdItemId(mdItemId);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}

	public MDItem(long messageId, MDMessageType messageType, String sourceName, Date sourceTimestamp, 
			int itemCount, int itemIndex, MDItemType mdItemType, String mdItemId, String symbol, int feedId)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
		setMdItemType(mdItemType);
		setMdItemId(mdItemId);
		setSymbol(symbol);
		setFeedId(feedId);
		setItemIndex(itemIndex);
	}
	
	
	public MDItemType getMdItemType()
	{
		return mdItemType;
	}

	public void setMdItemType(MDItemType mdItemType)
	{
		this.mdItemType = mdItemType;
	}
	
	public String getMdItemId()
	{
		return mdItemId;
	}

	public void setMdItemId(String mdItemId)
	{
		this.mdItemId = mdItemId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#getSymbol()
	 */
	@Override
	public String getSymbol()
	{
		return symbol;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#setSymbol(java.lang.String)
	 */
	@Override
	public void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#getFeedId()
	 */
	@Override
	public int getFeedId()
	{
		return feedId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#setFeedId(int)
	 */
	@Override
	public void setFeedId(int feedId)
	{
		this.feedId = feedId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#getFeedName()
	 */
	@Override
	public String getFeedName()
	{
		return feedName;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#setFeedName(java.lang.String)
	 */
	@Override
	public void setFeedName(String feedName)
	{
		this.feedName = feedName;
	}

	public int getItemIndex()
	{
		return itemIndex;
	}

	public void setItemIndex(int itemIndex)
	{
		this.itemIndex = itemIndex;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#getFeedGroupId()
	 */
	@Override
	public int getFeedGroupId()
	{
		return feedGroupId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#setFeedGroupId(int)
	 */
	@Override
	public void setFeedGroupId(int feedGroupId)
	{
		this.feedGroupId = feedGroupId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#getInstrumentId()
	 */
	@Override
	public String getInstrumentId()
	{
		return instrumentId;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.datamodel.MDFeedEvent#setInstrumentId(java.lang.String)
	 */
	@Override
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
}
