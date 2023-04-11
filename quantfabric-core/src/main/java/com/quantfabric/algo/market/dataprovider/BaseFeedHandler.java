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
package com.quantfabric.algo.market.dataprovider;

import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.messaging.Subscriber;

public abstract class BaseFeedHandler 
	implements Subscriber<Object>, FeedHandler
{
	private final MarketFeeder marketFeeder;
	private final FeedName feedName;
	private boolean started = false; 
	
	public BaseFeedHandler(MarketFeeder marketFeeder, FeedName feedName)
	{
		this.marketFeeder = marketFeeder;
		this.feedName = feedName;
	}

	public MarketFeeder getMarketFeeder()
	{
		return marketFeeder;
	}

	public FeedName getMonitoredFeedName()
	{
		return feedName;
	}

	public void start() throws Exception
	{
		if (feedName != null && marketFeeder != null)
		{
			marketFeeder.subscribe(this, feedName);
			started = true;
		}		
	}

	public void stop() throws Exception
	{
		if (feedName != null && marketFeeder != null)
		{
			marketFeeder.unSubscribe(this, feedName);
			started = false;
		}
	}

	public boolean isStarted()
	{
		return started;
	}
}
