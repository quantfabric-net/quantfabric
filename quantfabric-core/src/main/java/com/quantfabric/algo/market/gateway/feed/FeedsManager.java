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

import com.quantfabric.algo.market.dataprovider.FeedName;

public class FeedsManager<T extends Feed, TC extends CashedByFeedIdFeedCollection<T>>
{
	protected TC feeds;
	
	public FeedsManager(TC feeds)
	{
		this.feeds = feeds;
	}
	
	public final TC getCollection()
	{
		return feeds;
	}
	
	public boolean add(T feed)
	{
		if (feeds.containsKey(feed.getFeedName()))
			return false;
		
		feeds.put(feed.getFeedName(), feed);
		return true;
	}
	
	public boolean remove(FeedName feedName)
	{
		if (feeds.containsKey(feedName))
		{
			feeds.remove(feedName);
			return true;
		}
		
		return false;
	}
	
	public boolean remove(T feed)
	{
		return remove(feed.getFeedName());
	}
}
