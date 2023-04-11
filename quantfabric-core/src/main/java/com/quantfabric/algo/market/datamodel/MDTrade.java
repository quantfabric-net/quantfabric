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


public class MDTrade extends MDItem {

	/**
	 * 
	 */
	
	public enum MDTradeSide {
		
		BUY,
		SELL,
		NA
	}
	
	private static final long serialVersionUID = -3959982843830048682L;
	public static final String NOT_SET = "NA";

	private boolean buyerMarketMaker;
	private long buyerOrderId;
	private  long sellerOrderId;
	private long price;
	private String currency = "NA";
	private MDTradeSide tradeSide = MDTradeSide.NA;
	
	public MDTrade() {
		super();
	}

	public MDTrade(long messageId, MDMessageType incrementalRefresh, String sourceName, Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, long price, String currency, int feedId, MDTradeSide tradeSide) {
		super(messageId, incrementalRefresh, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
		this.price = price;
		this.currency = currency;
		this.tradeSide = tradeSide;
	}


	public MDTrade(long messageId, MDMessageType incrementalRefresh, String sourceName, Date sourceTimestamp, int itemCount, int itemIndex,
				   MDItemType mdItemType, String symbol, long price, String currency, int feedId, MDTradeSide tradeSide, boolean buyerMarketMaker,
				   long buyerOrderId, long sellerOrderId) {
		//MDTrade(messageId, incrementalRefresh, sourceName, sourceTimestamp,itemCount,itemIndex,mdItemType,symbol, price, currency, feedId, tradeSide)
		super(messageId, incrementalRefresh, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
		this.price = price;
		this.currency = currency;
		this.tradeSide = tradeSide;
		this.buyerMarketMaker = buyerMarketMaker;
		this.buyerOrderId = buyerOrderId;
		this.sellerOrderId = sellerOrderId;
	}
	public long getTradeId() {
		return getMessageId();
	}
	
	public long getPrice() {
		return price;
	}

	
	public String getCurrency() {
		return currency;
	}

	
	public void setPrice(long price) {
		this.price = price;
	}

	
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public MDTradeSide getTradeSide() {
		return tradeSide;
	}

	public void setTradeSide(MDTradeSide tradeSide) {
		this.tradeSide = tradeSide;
	}

	public boolean isBuyerMarketMaker() {
		return buyerMarketMaker;
	}

	public void setBuyerMarketMaker(boolean buyerMarketMaker) {
		this.buyerMarketMaker = buyerMarketMaker;
	}

	public long getBuyerOrderId() {
		return buyerOrderId;
	}

	public void setBuyerOrderId(long buyerOrderId) {
		this.buyerOrderId = buyerOrderId;
	}

	public long getSellerOrderId() {
		return sellerOrderId;
	}

	public void setSellerOrderId(long sellerOrderId) {
		this.sellerOrderId = sellerOrderId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MDTrade) {
			MDTrade tradeObj = (MDTrade) obj;
			return this.currency == tradeObj.currency && this.price == tradeObj.price && this.tradeSide == tradeObj.tradeSide && super.equals(obj);
		}

		return false;
	}

	@Override
	public String toString() {
		
		return "MDTrade id:[" + getTradeId() + "], currency:[" + currency + "], price:[" + price + "], timestamp:[" + getSourceTimestamp() + "]";
	}
}
