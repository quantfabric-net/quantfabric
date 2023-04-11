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



public class ComplexOHLC {
	
	private final String productCode;
	private final long snapshotId;
	private final boolean isClosed;
	private final boolean isClosedByTimeout;
	private final long closeTimestamp;
	
	private final long genericOpen;
	private final long genericOpenSourceTimestamp;
	private final long genericHigh;
	private final long genericHighSourceTimestamp;
	private final long genericLow;
	private final long genericLowSourceTimestamp;
	private final long genericClose;
	private final long genericCloseSourceTimestamp;
	private final long genericTimeFrameInSeconds;
	private long genericBarId = 0L;
	
	private final long tradeOpen;
	private final long tradeOpenSourceTimestamp;
	private final long tradeHigh;
	private final long tradeHighSourceTimestamp;
	private final long tradeLow;
	private final long tradeLowSourceTimestamp;
	private final long tradeClose;
	private final long tradeCloseSourceTimestamp;
	private final int tradeTimeFrameInSeconds;
	private long tradeBarId = 0L;
	
	private int tradeCount = 0;
	private int buyCount = 0;
	private int sellCount = 0;
	private double buySellRatio = 0;
	private long avgBuy = 0;
	private long avgSell = 0;
	
	public ComplexOHLC() {
		this(null, 0, false, false, 0, 
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0);
	}
	
	public ComplexOHLC(
			String productCode,	long snapshotId, boolean isClosed, boolean isClosedByTimeout, long closeTimestamp,
			
			long genericOpen, long genericOpenSourceTimestamp, long genericHigh, long genericHighSourceTimestamp,
			long genericLow, long genericLowSourceTimestamp, long genericClose, long genericCloseSourceTimestamp,
			long genericTimeFrameInSeconds, long genericBarId,

			long tradeOpen, long tradeOpenSourceTimestamp, long tradeHigh, long tradeHighSourceTimestamp,
			long tradeLow, long tradeLowSourceTimestamp, long tradeClose, long tradeCloseSourceTimestamp,
			int tradeTimeFrameInSeconds, long tradeBarId,
			
			int tradeCount, int buyCount, int sellCount, double buySellRatio, long avgBuy, long avgSell) {
		super();
		
		this.productCode = productCode;		
		this.snapshotId = snapshotId;
		this.isClosed = isClosed;
		this.isClosedByTimeout = isClosedByTimeout;
		this.closeTimestamp = closeTimestamp;
		
		this.genericOpen = genericOpen;
		this.genericOpenSourceTimestamp = genericOpenSourceTimestamp;
		this.genericHigh = genericHigh;
		this.genericHighSourceTimestamp = genericHighSourceTimestamp;
		this.genericLow = genericLow;
		this.genericLowSourceTimestamp = genericLowSourceTimestamp;
		this.genericClose = genericClose;
		this.genericCloseSourceTimestamp = genericCloseSourceTimestamp;	
		this.genericTimeFrameInSeconds = genericTimeFrameInSeconds;				
		this.genericBarId = genericBarId;
		
		this.tradeOpen = tradeOpen;
		this.tradeOpenSourceTimestamp = tradeOpenSourceTimestamp;
		this.tradeHigh = tradeHigh;
		this.tradeHighSourceTimestamp = tradeHighSourceTimestamp;
		this.tradeLow = tradeLow;
		this.tradeLowSourceTimestamp = tradeLowSourceTimestamp;
		this.tradeClose = tradeClose;
		this.tradeCloseSourceTimestamp = tradeCloseSourceTimestamp;	
		this.tradeTimeFrameInSeconds = tradeTimeFrameInSeconds;				
		this.tradeBarId = tradeBarId;
		
		this.tradeCount = tradeCount;
		this.buyCount = buyCount;
		this.sellCount = sellCount;
		this.buySellRatio = buySellRatio;
		this.avgBuy = avgBuy;
		this.avgSell = avgSell;
	}
	
	public String getProductCode() {
		return productCode;
	}

	public long getSnapshotId() {
		return snapshotId;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public boolean isClosedByTimeout() {
		return isClosedByTimeout;
	}

	public long getCloseTimestamp() {
		return closeTimestamp;
	}

	public long getGenericOpen() {
		return genericOpen;
	}

	public long getGenericOpenSourceTimestamp() {
		return genericOpenSourceTimestamp;
	}

	public long getGenericHigh() {
		return genericHigh;
	}

	public long getGenericHighSourceTimestamp() {
		return genericHighSourceTimestamp;
	}

	public long getGenericLow() {
		return genericLow;
	}

	public long getGenericLowSourceTimestamp() {
		return genericLowSourceTimestamp;
	}

	public long getGenericClose() {
		return genericClose;
	}

	public long getGenericCloseSourceTimestamp() {
		return genericCloseSourceTimestamp;
	}

	public long getGenericTimeFrameInSeconds() {
		return genericTimeFrameInSeconds;
	}

	public long getGenericBarId() {
		return genericBarId;
	}

	public long getTradeOpen() {
		return tradeOpen;
	}

	public long getTradeOpenSourceTimestamp() {
		return tradeOpenSourceTimestamp;
	}

	public long getTradeHigh() {
		return tradeHigh;
	}

	public long getTradeHighSourceTimestamp() {
		return tradeHighSourceTimestamp;
	}

	public long getTradeLow() {
		return tradeLow;
	}

	public long getTradeLowSourceTimestamp() {
		return tradeLowSourceTimestamp;
	}

	public long getTradeClose() {
		return tradeClose;
	}

	public long getTradeCloseSourceTimestamp() {
		return tradeCloseSourceTimestamp;
	}

	public int getTradeTimeFrameInSeconds() {
		return tradeTimeFrameInSeconds;
	}

	public long getTradeBarId() {
		return tradeBarId;
	}

	public int getTradeCount() {
		return tradeCount;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public int getSellCount() {
		return sellCount;
	}

	public double getBuySellRatio() {
		return buySellRatio;
	}

	public long getAvgBuy() {
		return avgBuy;
	}

	public long getAvgSell() {
		return avgSell;
	}

	@Override
	public String toString() {
		
		return "ComplexOHLC(" + productCode + "): snapshotId = " + snapshotId + ", isClosed = " + isClosed + ", isClosedByTimeout = " + isClosedByTimeout + ", generic OHLC = (open=[" + genericOpen + "]; openSourceTimestamp=[" + genericOpenSourceTimestamp + "]; high=["
				+ genericHigh + "]; highSourceTimestamp=[" + genericHighSourceTimestamp + "]; low=[" + genericLow + "]; lowSourceTimestamp=[" + genericLowSourceTimestamp + "]; close=[" + genericClose
				+ "]; closeSourceTimestamp=[" + genericCloseSourceTimestamp + "]; timeFrameInSeconds=[" + genericTimeFrameInSeconds + "])"
				+ ", trade OHLC = (open=[" + tradeOpen + "]; openSourceTimestamp=[" + tradeOpenSourceTimestamp + "]; high=["
				+ tradeHigh + "]; highSourceTimestamp=[" + tradeHighSourceTimestamp + "]; low=[" + tradeLow + "]; lowSourceTimestamp=[" + tradeLowSourceTimestamp + "]; close=[" + tradeClose
				+ "]; closeSourceTimestamp=[" + tradeCloseSourceTimestamp + "]; timeFrameInSeconds=[" + tradeTimeFrameInSeconds + "]; "
				+ "tradeCount=[" + tradeCount + "]; buyCount=[" + buyCount + "]; sellCount=[" + sellCount + "]; buySellRatio=[" + buySellRatio + "]; avgBuy=[" + avgBuy + "]; avgSell=[" + avgSell + "])"
				;
	}
}
