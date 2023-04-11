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

import java.util.Map;

import com.quantfabric.algo.market.dataprovider.FeedName;

public abstract class DoubleCachedFeedCollection<T extends Feed, CK> 
	extends CashedByFeedIdFeedCollection<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5622610578918008380L;

	protected abstract CK getSecondCashedKey(T value);
	
	private final CachableFeedCollection<T, CK> feeds = new CachableFeedCollection<T, CK>()
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8834278145715124483L;

		@Override
		protected CK getCachedKey(T value)
		{
			return getSecondCashedKey(value);
		}
		
	};

	public T getBySecondCashKey(CK key)
	{
		return feeds.getByCashKey(key);
	}
	
	@Override
	public T put(FeedName key, T value)
	{
		feeds.put(key, value);
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends FeedName, ? extends T> m)
	{
		feeds.putAll(m);
		super.putAll(m);
	}

	@Override
	public T remove(Object key)
	{
		feeds.remove(key);
		return super.remove(key);
	}

	@Override
	public void clear()
	{
		feeds.clear();
		super.clear();
	}
	
	

}
