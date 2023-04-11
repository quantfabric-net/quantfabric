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
package com.quantfabric.algo.market.gateway.commands;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import org.apache.commons.lang.NotImplementedException;

public interface Subscribe
{
	default MarketDataFeed getFeed() {
		throw new NotImplementedException();
	}
	default void setFeed(MarketDataFeed feed) {
		throw new NotImplementedException();
	}
	default Instrument getInstrument() {
		throw new NotImplementedException();
	}
}
