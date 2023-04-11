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


public class MDDealableQuote extends BaseMDFeedEvent implements Cloneable
{	
	private static final long serialVersionUID = 3479129480691902639L;

	public enum UpdateStatuses
	{
		NOTHING,
		BID_ONLY,
		OFFER_ONLY,
		BOTH
	}
	
	private UpdateStatuses updateStatus;
	
	private int bookLevel = 0;
	private long snapshotId = 0;
	
	private long bidMessageId = 0;
	private long bidTimestamp = 0;
	private long bidSourceTimestamp = 0;
	private long bidPrice = 0;
	private double bidSize = 0.;	
	private int bidCount = 0;
	
	private long offerMessageId = 0;
	private long offerTimestamp = 0L;
	private long offerSourceTimestamp = 0L;
	private long offerPrice = 0;
	private double offerSize = 0.;	
	private int offerCount = 0;
	
	private MDTrade trade = new MDTrade();
	
	private long sourceTimestamp = 0;

	public MDDealableQuote()
	{
		super();
	}	
		
	public MDDealableQuote(MDFeedEvent feedEvent)
	{
		super(feedEvent);
	}
	
	public void populateBidSide(MDPrice bid)
	{
		if (bid == null)
		{
			bidTimestamp = 0L;
			bidSourceTimestamp = 0L;
			bidPrice = 0;
			bidSize = 0L;
			bidCount = 0;
			bidMessageId = 0;
		}
		else
		{
			bidTimestamp = bid.getTimestamp();
			bidSourceTimestamp = bid.getSourceTimestamp();
			bidPrice = bid.getPrice();
			bidSize = bid.getSize();
			bidCount = bid.getAmountOrders();
			bidMessageId = bid.getMessageId();
		}
		
		if (getUpdateStatus() == UpdateStatuses.OFFER_ONLY)
			setUpdateStatus(UpdateStatuses.BOTH);
		else
			setUpdateStatus(UpdateStatuses.BID_ONLY);
	}
	
	public void populateOfferSide(MDPrice offer)
	{
		if (offer == null)
		{
			offerTimestamp = 0L;
			offerSourceTimestamp = 0L;
			offerPrice = 0;
			offerSize = 0L;
			offerCount = 0;
			offerMessageId = 0;
		}
		else
		{
			offerTimestamp = offer.getTimestamp();
			offerSourceTimestamp = offer.getSourceTimestamp();
			offerPrice = offer.getPrice();
			offerSize = offer.getSize();
			offerCount = offer.getAmountOrders();
			offerMessageId = offer.getMessageId();
		}
		
		if (getUpdateStatus() == UpdateStatuses.BID_ONLY)
			setUpdateStatus(UpdateStatuses.BOTH);
		else
			setUpdateStatus(UpdateStatuses.OFFER_ONLY);
	}
	
	public int getBookLevel()
	{
		return bookLevel;
	}
	public void setBookLevel(int bookLevel)
	{
		this.bookLevel = bookLevel;
	}	
	public long getSnapshotId()
	{
		return snapshotId;
	}
	public void setSnapshotId(long snapshotId)
	{
		this.snapshotId = snapshotId;
	}
	
	public long getBidMessageId()
	{
		return bidMessageId;
	}

	public void setBidMessageId(long bidMessageId)
	{
		this.bidMessageId = bidMessageId;
	}

	public long getOfferMessageId()
	{
		return offerMessageId;
	}

	public void setOfferMessageId(long offerMessageId)
	{
		this.offerMessageId = offerMessageId;
	}

	public long getBidTimestamp()
	{
		return bidTimestamp;
	}
	public void setBidTimestamp(long bidTimestamp)
	{
		this.bidTimestamp = bidTimestamp;
	}
	public long getBidSourceTimestamp()
	{
		return bidSourceTimestamp;
	}
	public void setBidSourceTimestamp(long bidSourceTimestamp)
	{
		this.bidSourceTimestamp = bidSourceTimestamp;
	}
	public long getBidPrice()
	{
		return bidPrice;
	}
	public void setBidPrice(long bidPrice)
	{
		this.bidPrice = bidPrice;
	}
	public double getBidSize()
	{
		return bidSize;
	}
	public void setBidSize(double bidSize)
	{
		this.bidSize = bidSize;
	}
	public int getBidCount()
	{
		return bidCount;
	}
	public void setBidCount(int bidCount)
	{
		this.bidCount = bidCount;
	}
	public long getOfferTimestamp()
	{
		return offerTimestamp;
	}
	public void setOfferTimestamp(long offerTimestamp)
	{
		this.offerTimestamp = offerTimestamp;
	}
	public long getOfferSourceTimestamp()
	{
		return offerSourceTimestamp;
	}
	public void setOfferSourceTimestamp(long offerSourceTimestamp)
	{
		this.offerSourceTimestamp = offerSourceTimestamp;
	}
	public long getOfferPrice()
	{
		return offerPrice;
	}
	public void setOfferPrice(long offerPrice)
	{
		this.offerPrice = offerPrice;
	}
	public double getOfferSize()
	{
		return offerSize;
	}
	public void setOfferSize(double offerSize)
	{
		this.offerSize = offerSize;
	}
	public int getOfferCount()
	{
		return offerCount;
	}
	public void setOfferCount(int offerCount)
	{
		this.offerCount = offerCount;
	}

	public UpdateStatuses getUpdateStatus()
	{
		return updateStatus;
	}
	public void setUpdateStatus(UpdateStatuses updateStatus)
	{
		this.updateStatus = updateStatus;
	}
	public void resetUpdateStatus()
	{
		setUpdateStatus(UpdateStatuses.NOTHING);
	}
	
	public long getSourceTimestamp()
	{
		return sourceTimestamp;
	}

	public void setSourceTimestamp(long sourceTimestamp)
	{
		this.sourceTimestamp = sourceTimestamp;
	}
	
	public MDTrade getTrade() {
		
		return trade;
	}
	
	public void setTrade(MDTrade trade) {
		
		this.trade = trade;
	}


	@Override
	public MDDealableQuote clone() throws CloneNotSupportedException
	{
		MDDealableQuote quote =	new MDDealableQuote();
		quote.pupulate(this);
		quote.bidCount = this.bidCount;
		quote.bidMessageId = this.bidMessageId;
		quote.bidPrice = this.bidPrice;
		quote.bidSize = this.bidSize;
		quote.bidSourceTimestamp = this.bidSourceTimestamp;
		quote.bidMessageId = this.bidMessageId;
		quote.bidTimestamp = this.bidTimestamp;
		
		quote.bookLevel = this.bookLevel;
		quote.snapshotId = this.snapshotId;
		quote.updateStatus = this.updateStatus;
		
		quote.offerCount = this.offerCount;
		quote.offerMessageId = this.offerMessageId;
		quote.offerPrice = this.offerPrice;
		quote.offerSize = this.offerSize;
		quote.offerSourceTimestamp = this.offerSourceTimestamp;
		quote.offerMessageId = this.offerMessageId;
		quote.offerTimestamp = this.offerTimestamp;
		quote.sourceTimestamp = this.sourceTimestamp;
		
		quote.trade = this.trade;
		
		return quote;
	}

	@Override
	public String toString()
	{
		return "MDDealableQuote [updateStatus=" + updateStatus + ", bookLevel="
				+ bookLevel + ", snapshotId=" + snapshotId + ", bidMessageId="
				+ bidMessageId + ", bidTimestamp=" + bidTimestamp
				+ ", bidSourceTimestamp=" + bidSourceTimestamp + ", bidPrice="
				+ bidPrice + ", bidSize=" + bidSize + ", bidCount=" + bidCount
				+ ", offerMessageId=" + offerMessageId + ", offerTimestamp="
				+ offerTimestamp + ", offerSourceTimestamp="
				+ offerSourceTimestamp + ", offerPrice=" + offerPrice
				+ ", offerSize=" + offerSize + ", offerCount=" + offerCount
				+ ", sourceTimestamp=" + sourceTimestamp + "]";
	}
	
	
}
