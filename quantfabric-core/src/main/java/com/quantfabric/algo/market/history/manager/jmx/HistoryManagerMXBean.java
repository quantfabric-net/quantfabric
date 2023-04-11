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
package com.quantfabric.algo.market.history.manager.jmx;

import java.text.ParseException;
import java.util.List;

public interface HistoryManagerMXBean 
{
	List<OHLC> exportHistory(String timeFrame) throws ParseException;
	List<OHLC> exportHistory(String timeFrame, int depth) throws ParseException;
	
	void replaceBar(String timeFrame, OHLC ohlc) throws ParseException;
	void replaceBar(String timeFrame, String openTime, int open, int high, int low, int close) throws ParseException;
	
	void addBar(String timeFrame, OHLC ohlc) throws ParseException;
	void addBar(String timeFrame, String openTime, int open, int high, int low, int close) throws ParseException;
}
