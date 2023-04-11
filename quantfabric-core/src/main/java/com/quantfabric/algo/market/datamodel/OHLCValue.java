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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.quantfabric.algo.market.datamodel.MDTrade.MDTradeSide;

public class OHLCValue implements Cloneable, Serializable
{
	private static final long serialVersionUID = 6488936978641124760L;

	private static final AtomicLong ohlcValueInstancesCounter = new AtomicLong(0);
	private static long getNewBarId()
	{
		return ohlcValueInstancesCounter.incrementAndGet();
	}
		
	private static final int NOT_INITIALIZED = -1;
	
	private long barId = 0L;
	private int timeFrameInSeconds;	
	private long open;
	private long openSourceTimestamp;
	private long high;
	private long highSourceTimestamp;
	private long low;
	private long lowSourceTimestamp;
	private long close;
	private long closeSourceTimestamp;	
	private long typical;
	private long barSize;	
	
	private boolean closed;
	private long closeTimestamp;
	private boolean closeByTimeout;
	
	private boolean justOpened;
	
	private int tradeCount = 0;
	private int buyCount = 0;
	private int sellCount = 0;
	private double buySellRatio = 0;
	private int cumulativeBuyPrice = 0;
	private int cumulativeSellPrice = 0;
	private long avgBuy = 0;
	private long avgSell = 0;
		
	public OHLCValue()
	{
		this(NOT_INITIALIZED);
	}
		
	public OHLCValue(int timeFrameInSeconds)
	{
		this(timeFrameInSeconds != NOT_INITIALIZED ? getNewBarId() : 0L, 
				timeFrameInSeconds, 0, 0L, 0, 0L, 0, 0L, 
				0, 0L, 0, 0, false, 0L, false, false);
	}	
	
	public OHLCValue(int timeFrameInSeconds,
					 long open,
					 long openSourceTimestamp,
					 long high,
					 long highSourceTimestamp,
					 long low,
					 long lowSourceTimestamp,
					 long close,
					 long closeSourceTimestamp,
					 boolean closed,
					 boolean justOpened)
	{
		this(getNewBarId(), timeFrameInSeconds, open, openSourceTimestamp, high, highSourceTimestamp, low, lowSourceTimestamp, 
				close, closeSourceTimestamp, (high + low + close) / 3, close - open, closed, 0L, false, justOpened);	
	}	
			
	protected OHLCValue(long barId, int timeFrameInSeconds, long open,
			long openSourceTimestamp, long high, long highSourceTimestamp,
			long low, long lowSourceTimestamp, long close,
			long closeSourceTimestamp, long typical, long barSize,
			boolean closed, long closeTimestamp, boolean closeByTimeout,
			boolean justOpened)
	{
		super();
		this.barId = barId;
		this.timeFrameInSeconds = timeFrameInSeconds;
		this.open = open;
		this.openSourceTimestamp = openSourceTimestamp;
		this.high = high;
		this.highSourceTimestamp = highSourceTimestamp;
		this.low = low;
		this.lowSourceTimestamp = lowSourceTimestamp;
		this.close = close;
		this.closeSourceTimestamp = closeSourceTimestamp;
		this.typical = typical;
		this.barSize = barSize;
		this.closed = closed;
		this.closeTimestamp = closeTimestamp;
		this.closeByTimeout = closeByTimeout;
		this.justOpened = justOpened;
		
		clearTradeData();
	}
	
	protected OHLCValue(long barId, int timeFrameInSeconds, long open,
			long openSourceTimestamp, long high, long highSourceTimestamp,
			long low, long lowSourceTimestamp, long close,
			long closeSourceTimestamp, long typical, long barSize,
			boolean closed, long closeTimestamp, boolean closeByTimeout,
			boolean justOpened, int tradeCount, int buyCount, int sellCount, double buySellRatio, long avgBuy, long avgSell)
	{
		super();
		this.barId = barId;
		this.timeFrameInSeconds = timeFrameInSeconds;
		this.open = open;
		this.openSourceTimestamp = openSourceTimestamp;
		this.high = high;
		this.highSourceTimestamp = highSourceTimestamp;
		this.low = low;
		this.lowSourceTimestamp = lowSourceTimestamp;
		this.close = close;
		this.closeSourceTimestamp = closeSourceTimestamp;
		this.typical = typical;
		this.barSize = barSize;
		this.closed = closed;
		this.closeTimestamp = closeTimestamp;
		this.closeByTimeout = closeByTimeout;
		this.justOpened = justOpened;
		
		this.tradeCount = tradeCount;
		this.buyCount = buyCount;
		this.sellCount = sellCount;
		this.buySellRatio = buySellRatio;
		this.avgBuy = avgBuy;
		this.avgSell = avgSell;
	}

	public long getBarId()
	{
		return barId;
	}
	public void setBarId(long id)
	{
		this.barId = id;
	}
	public int getTimeFrameInSeconds()
	{
		return timeFrameInSeconds;
	}
	public void setTimeFrameInSeconds(int timeFrameInSeconds)
	{
		this.timeFrameInSeconds = timeFrameInSeconds;
	}
	public long getOpen()
	{
		return open;
	}
	public void setOpen(long open)
	{
		this.open = open;
	}

	public void setOpenSourceTimestamp(long openSourceTimestamp)
	{
		this.openSourceTimestamp = openSourceTimestamp;
	}

	public void setHigh(long high)
	{
		this.high = high;
	}

	public void setHighSourceTimestamp(long highSourceTimestamp)
	{
		this.highSourceTimestamp = highSourceTimestamp;
	}

	public void setLow(long low)
	{
		this.low = low;
	}

	public void setLowSourceTimestamp(long lowSourceTimestamp)
	{
		this.lowSourceTimestamp = lowSourceTimestamp;
	}

	public void setClose(long close)
	{
		this.close = close;
	}

	public void setCloseSourceTimestamp(long closeSourceTimestamp)
	{
		this.closeSourceTimestamp = closeSourceTimestamp;
	}

	public void setCloseTimestamp(long closeTimestamp)
	{
		this.closeTimestamp = closeTimestamp;
	}

	public void setCloseByTimeout(boolean closeByTimeout)
	{
		this.closeByTimeout = closeByTimeout;
	}

	public long getOpenSourceTimestamp()
	{
		return openSourceTimestamp;
	}
	public long getHigh()
	{
		return high;
	}
	public long getHighSourceTimestamp()
	{
		return highSourceTimestamp;
	}
	public long getLow()
	{
		return low;
	}
	public long getLowSourceTimestamp()
	{
		return lowSourceTimestamp;
	}
	public long getClose()
	{
		return close;
	}
	public long getCloseSourceTimestamp()
	{
		return closeSourceTimestamp;
	}
	public boolean isClosed()
	{
		return closed;
	}
	public void setClosed(boolean closed)
	{
		this.closed =  closed;
	}
	public long getCloseTimestamp()
	{
		return closeTimestamp;
	}
	public boolean isCloseByTimeout()
	{
		return closeByTimeout;
	}	
	public long getTypical()
	{
		return typical;
	}
	public void setTypical(long typical)
	{
		this.typical = typical;
	}	
	public long getBarSize()
	{
		return barSize;
	}

	public void setBarSize(long barSize)
	{
		this.barSize = barSize;
	}

	public void update (long timestamp, long price) throws Exception
	{
		if (timeFrameInSeconds == NOT_INITIALIZED)
			throw new Exception("TimeFrameInSeconds must be initialized before");
		
		if (isClosed())
			throw new Exception("Can't be update because is closed.");
		
		if (!isClosed())
		{
			close = price;
			closeSourceTimestamp = timestamp;
		}
			
		if (open == 0 && openSourceTimestamp == 0L)
		{
			justOpened = true;
			open = price;
			openSourceTimestamp = timestamp;
		}
		else
			justOpened = false;

		if (low == 0 || low > price)
		{
			low = price;
			lowSourceTimestamp = timestamp;
		}
		
		if (high == 0 || high < price)
		{
			high = price;
			highSourceTimestamp = timestamp;
		}
		
		typical = (high + low + close) / 3;
		
		barSize = close - open;
	}
	
	public void update(long timestamp, long price, MDTradeSide tradeSide) throws Exception {

		update(timestamp, price);

		switch (tradeSide) {
			case BUY:
				buyCount++;
				cumulativeBuyPrice += price;
				break;
			case SELL:
				sellCount++;
				cumulativeSellPrice += price;
				break;
			case NA:
				break;
			default:
				break;
		}

		tradeCount = buyCount + sellCount;

		if (tradeCount != 0)
			buySellRatio = (buyCount - sellCount) * 100 / tradeCount;
		else
			buySellRatio = 0;

		if (cumulativeBuyPrice != 0)
			avgBuy = cumulativeBuyPrice / buyCount;
		else
			avgBuy = 0;

		if (cumulativeSellPrice != 0)
			avgSell = cumulativeSellPrice / sellCount;
		else
			avgSell = 0;

	}
	
	public void close (boolean timeoutExpired)
	{
		closed = true;
		closeByTimeout = timeoutExpired;
		closeTimestamp = System.currentTimeMillis();
	}
	
	private void clearTradeData() {
		
		tradeCount = 0;
		buyCount = 0;
		sellCount = 0;
		buySellRatio = 0;
		cumulativeBuyPrice = 0;
		cumulativeSellPrice = 0;
		avgBuy = 0;
		avgSell = 0;
	}
	
	public boolean isJustOpened()
	{
		return justOpened;
	}

	public void setJustOpened(boolean justOpened)
	{
		this.justOpened = justOpened;
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
	public String toString()
	{
		return " barId=" + barId + "; timeFrameInSeconds=" + timeFrameInSeconds
				+ "; open=" + open + "; openSourceTimestamp="
				+ openSourceTimestamp + "; high=" + high
				+ "; highSourceTimestamp=" + highSourceTimestamp + "; low="
				+ low + "; lowSourceTimestamp=" + lowSourceTimestamp
				+ "; close=" + close + "; closeSourceTimestamp="
				+ closeSourceTimestamp + "; typical=" + typical + "; barSize="
				+ barSize + "; closed=" + closed + "; closeTimestamp="
				+ closeTimestamp + "; closeByTimeout=" + closeByTimeout
				+ "; justOpened=" + justOpened + "; tradeCount=" 
				+ tradeCount + "; buyCount=" + buyCount + "; sellCount=" 
				+ sellCount + "; buySellRatio=" + buySellRatio + "; avgBuy="
				+ avgBuy + "; avgSell=" + avgSell
				;
	}

	@Override
	public OHLCValue clone() 
	{		
		return new OHLCValue(barId, timeFrameInSeconds, open, openSourceTimestamp, high, 
				highSourceTimestamp, low, lowSourceTimestamp, close, closeSourceTimestamp, typical, barSize, 
				closed, closeTimestamp, closeByTimeout, justOpened, tradeCount, buyCount, sellCount, buySellRatio, avgBuy, avgSell);
	}
}
