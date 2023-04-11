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

public class MDPrice extends MDItem implements Cloneable
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6810398179897951917L;

	public enum PriceType 
	{ 
		UNKNOWN,
		DEALABLE,
		LOCAL,
		REGULAR,
		TRADE,
		INDICATOR_BEST,
		BID,
		OFFER
	}

	public static final int DEFAULT_AMOUNT_ORDERS = 1;
	
	private long price = 0;
	private double size = 0L;	
	private PriceType priceType = PriceType.UNKNOWN; 
	private boolean isAggregated;
	private int amountOrders = DEFAULT_AMOUNT_ORDERS;
	private int depthLevel = 0;
	
	public MDPrice() {
		super();
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String priceId, String symbol, int feedId,
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}
			
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType, 
			String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}
	
	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType,
			String priceId, String symbol, int feedId, long price, double size,
			PriceType priceType, boolean isAggregated)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId,
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName, 
			long sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType,
			String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated);
	}	
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, long price, double size,
			PriceType priceType, boolean isAggregated, int depthLevel, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
		setDepthLevel(depthLevel);
	}
	
	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int depthLevel, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
		setDepthLevel(depthLevel);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId, 
			long price, double size, PriceType priceType, boolean isAggregated, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}
			
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}
	
	public MDPrice(long timestamp, long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int depthLevel, int amountOrders)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
		setDepthLevel(depthLevel);
	}
	
	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, 
			long price, double size, PriceType priceType,
			boolean isAggregated, int depthLevel, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
		setDepthLevel(depthLevel);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId,
			long price, double size, PriceType priceType, boolean isAggregated, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}
	
	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType,
			String priceId, String symbol, int feedId,
			long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, 
			MDItemType mdItemType, String symbol, int feedId,
			long price, double size, PriceType priceType, boolean isAggregated, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}

	public MDPrice(long messageId, MDMessageType messageType, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType,
			String priceId, String symbol, int feedId,
			long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, priceId, symbol, feedId);
		init(price, size, priceType, isAggregated, amountOrders);
	}	
	
	public long getPrice()
	{
		return price;
	}
	
	public void setPrice(long price)
	{
		this.price = price;
	}
	
	public double getSize()
	{
		return size;
	}
	
	public void setSize(double size)
	{
		this.size = size;
	}
		
	public PriceType getPriceType()
	{
		return priceType;
	}

	public void setPriceType(PriceType priceType)
	{
		this.priceType = priceType;
	}
	
	public boolean isAggregated()
	{
		return isAggregated;
	}

	public void setAggregated(boolean isAggregated)
	{
		this.isAggregated = isAggregated;
	}

	public int getAmountOrders()
	{
		return amountOrders;
	}

	public void setAmountOrders(int amountOrders)
	{
		this.amountOrders = amountOrders;
	}
	
	public int getDepthLevel()
	{
		return depthLevel;
	}

	public void setDepthLevel(int depthLevel)
	{
		this.depthLevel = depthLevel;
	}

	private void init(long price, double size, PriceType priceType, boolean isAggregated)
	{
		setPrice(price);
		setSize(size);
		setPriceType(priceType);
		setAggregated(isAggregated);
	}	
	
	private void init(long price, double size, PriceType priceType,
			boolean isAggregated, int amountOrders)
	{
		setPrice(price);
		setSize(size);
		setPriceType(priceType);
		setAggregated(isAggregated);
		setAmountOrders(amountOrders);
	}

	@Override
	public MDPrice clone() 
	{
		MDPrice copy = new MDPrice(getMessageId(), getMessageId(), getMessageType(), getSourceName(), getSourceTimestamp(), 
				getItemCount(), getItemIndex(), getMdItemType(), getSymbol(), getFeedId(), 
				price, size, priceType, isAggregated, amountOrders);
		
		
		copy.setMdItemId(getMdItemId());
		copy.setInstrumentId(getInstrumentId());
		copy.setFeedName(getFeedName());
		copy.setFeedGroupId(getFeedGroupId());
		copy.setPointsInOne(getPointsInOne());		
		copy.setDepthLevel(depthLevel);		
		copy.setMessageLatency(getMessageLatency());
			
		return copy;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof MDPrice)
		{
			MDPrice priceObj = (MDPrice)obj;
			return this.price == priceObj.price &&
					this.size == priceObj.size &&
					this.amountOrders == priceObj.amountOrders &&
					this.isAggregated == priceObj.isAggregated &&
					this.depthLevel == priceObj.depthLevel &&
					this.priceType == priceObj.priceType &&
					super.equals(obj);
		}
		
		return false;
	}
	
	
}
