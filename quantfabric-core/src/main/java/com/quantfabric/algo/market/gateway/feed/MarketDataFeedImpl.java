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
package com.quantfabric.algo.market.gateway.feed;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.dataprovider.FeedName;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.atomic.AtomicLong;

public class MarketDataFeedImpl extends AbstractFeed implements MarketDataFeed
{
	public static final int FULL_MARKET_DEPTH = 0;
	public static final int TOP_MARKET_DEPTH = 1;
	public static final int DEFAULT_MARKET_DEPTH = FULL_MARKET_DEPTH;
	public static final String DEFAULT_CHANNEL = "DEFAULT";


	private int marketDepth = DEFAULT_MARKET_DEPTH;
	private String channel = DEFAULT_CHANNEL;
	private MarketDataType marketDataType = DEFAULT_MARKET_DATA_TYPE;
	private static final AtomicLong  seqId = new AtomicLong(0);
	private Disposable disposable;

	public MarketDataFeedImpl(FeedName feedName, Instrument instrument)
	{
		this(feedName, null, instrument.getId(), true);
		setInstrument(instrument);
	}

	public MarketDataFeedImpl(
			FeedName feedName,
			InstrumentProvider instrumentProvider,
			String instrumentId,
			boolean cachingInstrument)
	{
		super(feedName, instrumentProvider, instrumentId, cachingInstrument);
	}


	@Override
	public int getFeedId()
	{
		return hashCode();
	}

	@Override
	public String getChannel()
	{
		return channel;
	}
	@Override
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	@Override
	public int getMarketDepth()
	{
		return marketDepth;
	}
	@Override
	public void setMarketDepth(int marketDepth)
	{
		this.marketDepth = marketDepth;
	}

	@Override
	public MarketDataType getMarketDataType() {

		return marketDataType;
	}
	@Override
	public void setMarketDataType(MarketDataType marketDataType) {
		this.marketDataType = marketDataType;
	}

	@Override
	public long nextSeqId(){
		return seqId.incrementAndGet();
	}

	@Override
	public Disposable getDisposable() {
		return disposable;
	}

	@Override
	public void setDisposable(Disposable disposable) {
		this.disposable = disposable;
	}

	@Override
	public String toString() {
		return "MarketDataFeedImpl [instrumentId=" + getInstrumentId()
				+ ", instrument=" + getInstrument() + ", instrumentProvider="
				+ getInstrumentProvider() + ", cachingInstrument="
				+ isCachingInstrument() + ", marketDepth=" + marketDepth
				+ ", channel=" + channel + ", marketDataType=" + marketDataType
				+ ", toString()=" + super.toString() + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof MarketDataFeed)
		{
			MarketDataFeed feed = (MarketDataFeed) obj;

			return this.getFeedName().equals(feed.getFeedName())
					&& getInstrument() == null ? getInstrumentId().equals(feed.getInstrumentId()) : getInstrument().equals(feed.getInstrumentId());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + (getInstrument() == null ? getInstrumentId().hashCode() : getInstrument().hashCode());
	}

}


