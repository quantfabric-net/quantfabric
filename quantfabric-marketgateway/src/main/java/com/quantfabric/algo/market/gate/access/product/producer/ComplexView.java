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
package com.quantfabric.algo.market.gate.access.product.producer;

import com.quantfabric.algo.market.datamodel.BaseLightweightMDFeedEvent;


public class ComplexView implements BaseLightweightMDFeedEvent {	
	
	private final long snapshotId;
	private final long midTopPrice;
	private final long midVWAPPrice;
	private final long midOWAPPrice;
	private final String productCode;
	
	private final long bidPrice;
	private final int bidValue;
	private final long offerPrice;
	private final int offerValue;
	private final long sourceTimestamp;
	
	private long bidVWAPPrice = 0;
	private double bidVWAPSize = 0.;
	
	private long bidOWAPPrice = 0;
	private int bidOWAPAmountOrders = 0;
	
	private long offerVWAPPrice = 0;
	private double offerVWAPSize = 0.;
	
	private long offerOWAPPrice = 0;
	private int offerOWAPAmountOrders = 0;
	
	private int depth = 0;
	private final String feedName;
	private final String instrumentId;
	
	private final long tradeId;
	private long tradePrice;
	private final String tradeCurrency;
	private final String tradeSide;
	private final long tradeSourceTimestamp;
	
	public ComplexView() {
		this(0, 0, 0, 0, null, 0, 0, 0, 0, 0, 0, 0., 0, 0, 0, 0., 0, 0, 0, null, null, 0, 0, null, null, 0);
	}	
	
	public ComplexView(long snapshotId, long midTopPrice, long midVWAPPrice, long midOWAPPrice, String productCode, long bidPrice, int bidValue, long offerPrice,
					   int offerValue, long sourceTimestamp, long bidVWAPPrice, double bidVWAPSize, long bidOWAPPrice, int bidOWAPAmountOrders, long offerVWAPPrice,
					   double offerVWAPSize, int offerOWAPPrice, int offerOWAPAmountOrders, int depth, String feedName, String instrumentId,
					   long tradeId, long tradePrice, String tradeCurrency, String tradeSide, long tradeSourceTimestamp) {
		super();
		this.snapshotId = snapshotId;
		this.midTopPrice = midTopPrice;
		this.midVWAPPrice = midVWAPPrice;
		this.midOWAPPrice = midOWAPPrice;
		this.productCode = productCode;
		this.bidPrice = bidPrice;
		this.bidValue = bidValue;
		this.offerPrice = offerPrice;
		this.offerValue = offerValue;
		this.sourceTimestamp = sourceTimestamp;
		this.bidVWAPPrice = bidVWAPPrice;
		this.bidVWAPSize = bidVWAPSize;
		this.bidOWAPPrice = bidOWAPPrice;
		this.bidOWAPAmountOrders = bidOWAPAmountOrders;
		this.offerVWAPPrice = offerVWAPPrice;
		this.offerVWAPSize = offerVWAPSize;
		this.offerOWAPPrice = offerOWAPPrice;
		this.offerOWAPAmountOrders = offerOWAPAmountOrders;
		this.depth = depth;
		this.feedName = feedName;
		this.instrumentId = instrumentId;
		
		this.tradeId = tradeId;
		this.tradePrice = tradePrice;
		this.tradeCurrency = tradeCurrency;
		this.tradeSide = tradeSide;
		this.tradeSourceTimestamp = tradeSourceTimestamp;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public long getSnapshotId() {
		
		return snapshotId;
	}

	public long getMidTopPrice() {
		
		return midTopPrice;
	}

	
	public long getMidVWAPPrice() {
		return midVWAPPrice;
	}

	
	public long getMidOWAPPrice() {
		return midOWAPPrice;
	}

	public String getProductCode() {

		return productCode;
	}
	
	public long getBidPrice() {
		return bidPrice;
	}
	
	public int getBidValue() {
		return bidValue;
	}
	
	public long getOfferPrice() {
		return offerPrice;
	}
	
	public int getOfferValue() {
		return offerValue;
	}
	
	public long getSourceTimestamp() {
		return sourceTimestamp;
	}
	
	public long getBidVWAPPrice() {
		return bidVWAPPrice;
	}
	
	public double getBidVWAPSize() {
		return bidVWAPSize;
	}
	
	public long getBidOWAPPrice() {
		return bidOWAPPrice;
	}
	
	public int getBidOWAPAmountOrders() {
		return bidOWAPAmountOrders;
	}
	
	public long getOfferVWAPPrice() {
		return offerVWAPPrice;
	}
	
	public double getOfferVWAPSize() {
		return offerVWAPSize;
	}
	
	public long getOfferOWAPPrice() {
		return offerOWAPPrice;
	}
	
	public int getOfferOWAPAmountOrders() {
		return offerOWAPAmountOrders;
	}

	public String getFeedName() {
		return feedName;
	}

	public String getInstrumentId() {
		return instrumentId;
	}

	public long getTradeId() {
		return tradeId;
	}

	public long getTradePrice() {
		return tradePrice;
	}

	public void setTradePrice(long tradePrice) {
		this.tradePrice = tradePrice;
	}

	public String getTradeCurrency() {
		return tradeCurrency;
	}

	public String getTradeSide() {
		return tradeSide;
	}

	
	public long getTradeSourceTimestamp() {
		return tradeSourceTimestamp;
	}

	@Override
	public String toString() {
		
		return "ComplexView id = " + snapshotId + " (midTopPrice=[" + midTopPrice + "])";
	}
}
