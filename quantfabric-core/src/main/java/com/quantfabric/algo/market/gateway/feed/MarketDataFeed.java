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

public interface MarketDataFeed extends Feed
{
	enum MarketDataType {
		DEALABLE_PRICE,
		TRADES,
		ORDERBOOK,
		TOP_OF_BOOK,
// 		TICKER
//		BALANCES,
//		ORDERS,
		PRICES_AND_TRADES
	}

	int FULL_MARKET_DEPTH = 0;
	int TOP_MARKET_DEPTH = 1;
	int DEFAULT_MARKET_DEPTH = FULL_MARKET_DEPTH;
	String DEFAULT_CHANNEL = "DEFAULT";
	MarketDataType DEFAULT_MARKET_DATA_TYPE = MarketDataType.DEALABLE_PRICE;


	String getChannel();
	void setChannel(String channel);
	int getMarketDepth();
	void setMarketDepth(int depth);
	MarketDataType getMarketDataType();
	void setMarketDataType(MarketDataType type);
	long nextSeqId();

}

