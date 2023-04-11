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
package com.quantfabric.algo.market.gate.jmx;

import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataFeedMBean;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

public class MarketDataFeedMgmt implements MarketDataFeedMBean
{
	private final MarketDataFeed feed;
	
	public MarketDataFeedMgmt(MarketDataFeed feed)
	{
		this.feed = feed;
	}
	
	@Override
	public String getName()
	{
		return feed.getFeedName().getName();
	}

	@Override
	public String getMarketDepth()
	{
		if (feed.getMarketDepth() == MarketDataFeed.FULL_MARKET_DEPTH)
			return "FULL_MARKET_DEPTH";
		else
			if (feed.getMarketDepth() == MarketDataFeed.TOP_MARKET_DEPTH)
				return "TOP_MARKET_DEPTH";
		
		return String.valueOf(feed.getMarketDepth());		
	}

	@Override
	public String getInstrumentId()
	{
		return feed.getInstrumentId();
	}

	@Override
	public String getChannel()
	{
		return feed.getChannel();
	}
	
	
}
