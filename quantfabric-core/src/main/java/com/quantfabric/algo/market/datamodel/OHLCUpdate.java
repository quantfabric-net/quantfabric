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

public class OHLCUpdate extends BaseMDFeedEvent implements Cloneable
{
	private static final long serialVersionUID = 6948246297752264723L;
	
	private OHLCValue ohlc = null;
	private MDDealableQuote topQuote = null;
	private String timeframe = null;
	
	public OHLCUpdate()
	{
		super();
	}
	
	public OHLCUpdate(String timeframe, OHLCValue ohlc, MDDealableQuote topQuote)
	{
		super(topQuote);
		this.timeframe = timeframe;
		this.ohlc = ohlc;
		this.topQuote = topQuote;
	}

	public OHLCValue getOHLC()
	{
		return ohlc;
	}
	
	public void setOHLC(OHLCValue ohlcValue)
	{
		this.ohlc = ohlcValue;
	}
	
	public MDDealableQuote getTopQuote()
	{
		return topQuote;
	}
	
	public void setTopQuote(MDDealableQuote topQuote)
	{
		pupulate(topQuote);
		this.topQuote = topQuote;
	}
	
	public long getSnapshotId()
	{
		return getTopQuote().getSnapshotId();
	}
	
	public void setSnapshotId(long snapshotId)
	{
		getTopQuote().setSnapshotId(snapshotId);
	}
	
	public String getTimeframe()
	{
		return timeframe;
	}
	
	public void setTimeframe(String timeframe)
	{
		this.timeframe = timeframe;
	}
	
	@Override
	public OHLCUpdate clone() throws CloneNotSupportedException
	{
		return new OHLCUpdate(this.getTimeframe(), this.getOHLC().clone(), this.topQuote.clone());
	}

	@Override
	public String toString()
	{
		return "OHLCUpdate [timeframe=" + timeframe + ", " + super.toString() + ", ohlc=" + ohlc + ", topQuote=" + topQuote + "]";
	}
}
