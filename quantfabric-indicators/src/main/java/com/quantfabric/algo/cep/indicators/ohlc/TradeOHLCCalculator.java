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
package com.quantfabric.algo.cep.indicators.ohlc;

import java.util.Date;

import com.quantfabric.algo.market.datamodel.MDTrade.MDTradeSide;


public class TradeOHLCCalculator extends OHLCCalculator {
	
	
	public TradeOHLCCalculator(String period) {
		
		super(period);
	}
	
	public TradeOHLCCalculator(String period, int timeOffSet)
	{
		super(period, timeOffSet);
	}	
	
	public synchronized void update(Date timestamp, long price, MDTradeSide tradeSide) throws Exception
	{	
		if (currentOhlcValue == null)
		{
			init(timestamp);
		}
					
		if (timestamp.compareTo(this.currentTimeframeInterval.getEnd()) >= 0)
		{
			currentOhlcValue.close(false);
			postCurrentState();
			init(timestamp);
			try {
				setupTimeout(calculateDelay(currentTimeframeInterval.getEnd(), timestamp));
			} catch (Exception e) {
				System.out.printf("ERROR (TF %s). Inconsistency in time intervals [%s, %s]. Exception message: %s%n",
                        timeframe, currentTimeframeInterval.getEnd(), timestamp, e.getMessage());
			}
		}
		
		currentOhlcValue.update(timestamp.getTime(), price, tradeSide);	
		
		postCurrentState();
	}
}
