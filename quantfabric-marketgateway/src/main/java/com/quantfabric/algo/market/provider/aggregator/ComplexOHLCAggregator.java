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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.quantfabric.algo.cep.indicators.ohlc.ComplexOHLCCloser;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCUpdateListener;
import com.quantfabric.algo.market.datamodel.ComplexAccumulatedOHLC;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;

public class ComplexOHLCAggregator extends BaseMarketViewAggregator {

	private final List<MarketViewAggregatorSupport> aggregatorSupports =
			 new ArrayList<MarketViewAggregatorSupport>();
	
	private final ComplexOHLCCloser ohlcCloser;
	private boolean tradeObtained = false;
	private boolean genericObtained = false;	
	
	public ComplexOHLCAggregator(String name, Properties properties) {
		
		super(name, properties);
		
		OHLCAggregator ohlcAggregator = new OHLCAggregator(name + "OHLCAggregator", properties);
		TradeOHLCAggregator tradeOhlcAggregator = new TradeOHLCAggregator(name + "TradeOHLCAggregator", properties);
		
		aggregatorSupports.add(new MarketViewAggregatorSupport(ohlcAggregator));
		aggregatorSupports.add(new MarketViewAggregatorSupport(tradeOhlcAggregator));
		
		final String timeframe = properties.getProperty("timeFrame");
		
		String timeOffsetProperty = properties.getProperty("timeOffset");
		int timeOffset = 0;
		if (timeOffsetProperty != null)
			timeOffset = Integer.parseInt(timeOffsetProperty);
		
		ohlcCloser = new ComplexOHLCCloser(timeframe, timeOffset);
		
		ohlcCloser.setListener(new OHLCUpdateListener() {
			
			@Override
			public void update(ComplexAccumulatedOHLC value) {
				
				postComplexAccumulatedOHLC(value);
			}
			
			@Override
			public void update(OHLCValue value) {}
		});
	}

	private void createComplexOHLC(Object aggregation, long updateId) {
		
		try {
			ohlcCloser.update(new java.util.Date(getTimestamp((OHLCUpdate) aggregation)), (OHLCUpdate) aggregation);
		} catch (Exception e) {
			getLogger().error("Update ComplexOHLCCloser failed.", e);
		}
	}

	@Override
	public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot) throws OrderBookSnapshotListenerException {
		
		for (MarketViewAggregatorSupport aggregatorSupport : aggregatorSupports)
			aggregatorSupport.sendNewSnapshot(orderBookSnapshot);
	}

	@Override
	public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId, boolean isBookModified) {

		for (MarketViewAggregatorSupport aggregatorSupport : aggregatorSupports) {
			aggregatorSupport.sendEndUpdate(orderBookInfo, updateId, isBookModified);
			if (aggregatorSupport.isAggregationObtained()) {
				if (aggregatorSupport.getAggregation() != null)
					createComplexOHLC(aggregatorSupport.getAggregation(), updateId);
				if (aggregatorSupport.getAggregationBatch() != null)
					for (Object aggregation : aggregatorSupport.getAggregationBatch())
						createComplexOHLC(aggregation, updateId);
			}
		}
	}
	
	private void postComplexAccumulatedOHLC(ComplexAccumulatedOHLC complexOhlc) {

		if (complexOhlc != null) {
			if (complexOhlc.getGenericOHLCUpdate() != null)
				genericObtained = true;
			if (complexOhlc.getTradeOHLCUpdate() != null)
				tradeObtained = true;
		}
		
		if (genericObtained && tradeObtained) {

			genericObtained = false;
			tradeObtained = false;

			publish(complexOhlc);
		}
	}

	@Override
	public void processNoUpdate(long snapshotId) {}
	
	private long getTimestamp(OHLCUpdate update) {
		
		if (update.getOHLC().getTradeCount() > 0)
			return update.getTopQuote().getSourceTimestamp();
		else
			return update.getTopQuote().getSourceTimestamp();
	}
}
