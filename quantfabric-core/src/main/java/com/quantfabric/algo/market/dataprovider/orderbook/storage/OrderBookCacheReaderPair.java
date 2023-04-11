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
package com.quantfabric.algo.market.dataprovider.orderbook.storage;

public class OrderBookCacheReaderPair
{
	private final OrderBookCacheReader bidOrderBookCacheReader;
	private final OrderBookCacheReader offerOrderBookCacheReader;
	
	public OrderBookCacheReaderPair(
			OrderBookCacheReader bidOrderBookCacheReader,
			OrderBookCacheReader offerOrderBookCacheReader)
	{
		this.bidOrderBookCacheReader = bidOrderBookCacheReader;
		this.offerOrderBookCacheReader = offerOrderBookCacheReader;
	}
	
	public OrderBookCacheReader getBidOrderBookCacheReader()
	{
		return bidOrderBookCacheReader;
	}
	public OrderBookCacheReader getOfferOrderBookCacheReader()
	{
		return offerOrderBookCacheReader;
	}
}
