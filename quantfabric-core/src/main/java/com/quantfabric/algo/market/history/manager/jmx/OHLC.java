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
package com.quantfabric.algo.market.history.manager.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Date;

import com.quantfabric.algo.market.datamodel.OHLCValue;

public class OHLC implements Serializable
{
	private static final long serialVersionUID = -7367568311212782251L;
	
	Date openTime;
	long open, high, low, close;
	
	@ConstructorProperties({"openTime", "open", "high", "low", "close"})
	public OHLC(Date openTime, long open, long high, long low, long close)
	{
		this.openTime = openTime;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}

	public OHLC()
	{
		this.openTime = null;
		this.open = 0;
		this.high = 0;
		this.low = 0;
		this.close = 0;
	}
	
	public OHLC(OHLCValue ohlcValue)
	{
		this(new Date(ohlcValue.getOpenSourceTimestamp()), 
				ohlcValue.getOpen(), ohlcValue.getHigh(), 
			 ohlcValue.getLow(), ohlcValue.getClose());
	}

	public Date getOpenTime()
	{
		return openTime;
	}

	public void setOpenTime(Date openTime)
	{
		this.openTime = openTime;
	}

	public long getOpen()
	{
		return open;
	}

	public void setOpen(int open)
	{
		this.open = open;
	}

	public long getHigh()
	{
		return high;
	}

	public void setHigh(int high)
	{
		this.high = high;
	}

	public long getLow()
	{
		return low;
	}

	public void setLow(int low)
	{
		this.low = low;
	}

	public long getClose()
	{
		return close;
	}

	public void setClose(int close)
	{
		this.close = close;
	}

	@Override
	public String toString()
	{
		return "OHLC [openTime=" + openTime + ", open=" + open + ", high="
				+ high + ", low=" + low + ", close=" + close + "]";
	}
}
