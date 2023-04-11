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
package com.quantfabric.algo.market.history.manager;

import java.util.Collection;

import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.history.TimeFrame;

public interface HistoryManager
{
	Collection<OHLCValue> exportHistory(TimeFrame timeFrame);
	Collection<OHLCValue> exportHistory(TimeFrame timeFrame, int depth);
	void importHistory(TimeFrame timeFrame, Collection<OHLCValue> bars);
	Collection<Long> getGapIndexes(TimeFrame timeFrame);
	void addBar(TimeFrame timeFrame, OHLCValue ohlcValue);
	void replaceBar(TimeFrame timeFrame, OHLCValue ohlcValue);
}
