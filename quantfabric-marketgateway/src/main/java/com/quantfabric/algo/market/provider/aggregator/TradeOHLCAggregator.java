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
package com.quantfabric.algo.market.provider.aggregator;

import java.util.Date;
import java.util.Properties;

import com.quantfabric.algo.cep.indicators.ohlc.OHLCUpdateListener;
import com.quantfabric.algo.cep.indicators.ohlc.TradeOHLCCalculator;
import com.quantfabric.algo.market.datamodel.ComplexAccumulatedOHLC;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;

public class TradeOHLCAggregator extends OHLCAggregator {

	private TradeOHLCCalculator ohlcCalculator;
	private static final long NOT_INITIALIZED = -1;
	private long lastTradeId;
	private boolean isNewTrade = false;

	public TradeOHLCAggregator(String name, Properties properties) {
		super(name, properties);

		lastTradeId = NOT_INITIALIZED;
		final String timeframe = properties.getProperty("timeFrame");

		if (timeframe != null) {

			String timeOffsetProperty = properties.getProperty("timeOffset");
			int timeOffset = 0;
			if (timeOffsetProperty != null)
				timeOffset = Integer.parseInt(timeOffsetProperty);

			ohlcCalculator = new TradeOHLCCalculator(timeframe, timeOffset);
			ohlcCalculator.setListener(new OHLCUpdateListener() {

				@Override
				public void update(OHLCValue value) {
					TradeOHLCAggregator.this.callSuperPublish(new OHLCUpdate(timeframe, value, getTopQuote()));
					
					if (historyRecorder != null) {
						if (value.isJustOpened()) {
							historyRecorder.addBar(value);
						}
						else {
							historyRecorder.replaceBar(value);
						}
					}
				}

				@Override
				public void update(ComplexAccumulatedOHLC value) {}
			});
		}
	}

	@Override
	public void publish(Object event, boolean forceProcessing) {
		if (event instanceof MDDealableQuote) {
			MDDealableQuote topQuote = (MDDealableQuote) event;

			if (!topQuote.getTrade().getCurrency().equals(MDTrade.NOT_SET)) {
				try {					
					isNewTrade = tryUpdateLastTradeId(topQuote.getTrade().getTradeId());
					if (!ohlcCalculator.isInitialized() && historyView != null) {
						OHLCValue openBar = historyView.getOpenBar();
						if (openBar != null) {
							if (ohlcCalculator.getTimeframe().interval(new Date(openBar.getCloseSourceTimestamp()))
									.equals(ohlcCalculator.getTimeframe().interval(new Date(topQuote.getTrade().getSourceTimestamp())))) {
								getLogger().info(
										"Trade OHLC calculator (" + topQuote.getTrade().getFeedName() + " " + ohlcCalculator.getTimeframe().getLengthInSeconds()
												+ ") synchronized (accuracy: " + (topQuote.getTrade().getSourceTimestamp() - openBar.getCloseSourceTimestamp())
												+ "ms) by " + openBar);
								ohlcCalculator.init(openBar);

								if (openBar.getCloseSourceTimestamp() < topQuote.getTrade().getSourceTimestamp())
									updateCalculator(topQuote);
							}
							else
								updateCalculator(topQuote);
						}
						else
							updateCalculator(topQuote);
					}
					else
						updateCalculator(topQuote);
				}
				catch (Exception e) {
					getLogger().error("Update OHLCCalculator failed.", e);
				}
			}
		}
	}

	@Override
	protected void updateCalculator(MDDealableQuote topQuote) throws Exception {
		if (topQuote.getTrade().getPrice() != 0 && isNewTrade)
			ohlcCalculator.update(new Date(topQuote.getSourceTimestamp()), topQuote.getTrade().getPrice(), topQuote.getTrade().getTradeSide());
	}

	private boolean tryUpdateLastTradeId(long tradeId) {
		
		if (lastTradeId == NOT_INITIALIZED || lastTradeId != tradeId) {
			lastTradeId = tradeId;
			return true;
		}
		
		return false;
	}
}
