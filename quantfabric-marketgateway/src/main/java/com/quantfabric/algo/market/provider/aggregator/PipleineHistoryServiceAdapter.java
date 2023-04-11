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

import com.quantfabric.algo.market.datamodel.BaseMDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.dataprovider.HistoricalDataViewRequest;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.dataprovider.PipelineHistoryService;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProvider;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDefinitionImpl;
import com.quantfabric.algo.market.history.TimeFrame;
import com.quantfabric.util.timeframes.TimeframeFactory;

public class PipleineHistoryServiceAdapter implements PipelineHistoryService
{
	private final MultiTimeFrameHistoryProvider historyProvider;
	private final MultiTimeFrameHistoryProviderDefinitionImpl multiTimeFrameHistoryProviderDefinition;
	private final MarketDataPipeline pipeline;
	
	public PipleineHistoryServiceAdapter(
			MarketDataPipeline pipeline,
			MultiTimeFrameHistoryProvider historyProvider, 
			MultiTimeFrameHistoryProviderDefinitionImpl multiTimeFrameHistoryProviderDefinition)
	{
		this.pipeline = pipeline;
		this.historyProvider = historyProvider;
		this.multiTimeFrameHistoryProviderDefinition = multiTimeFrameHistoryProviderDefinition;
	}
	
	@Override
	public MultiTimeFrameHistoryProviderDefinitionImpl getDefinition()
	{
		return multiTimeFrameHistoryProviderDefinition;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.algo.market.dataprovider.aggregator.PipelineHisoryService#getHistoricalBars(com.quantfabric.algo.market.dataprovider.HistoricalDataViewRequest)
	 */
	@Override
	public OHLCUpdate[] getHistoricalBars(HistoricalDataViewRequest dataViewRequest)
	{
		TimeFrame timeframe = TimeFrame.getTimeFrame(TimeframeFactory.getTimeframe(dataViewRequest.getTimeFrame().toLowerCase()).getLengthInSeconds());
		
		return makeOHLCUpdates(
				historyProvider.getTimeFrameHandler(timeframe).getBars(dataViewRequest.getDepth()),
				dataViewRequest);		
	}
	
	private OHLCUpdate[] makeOHLCUpdates(OHLCValue[] bars,
			HistoricalDataViewRequest historicalDataViewRequest)
	{		
		MarketDataFeed marketDataFeed = pipeline.getMarketDataFeed(historicalDataViewRequest.getFeedName());
		
		if (marketDataFeed == null)
			return null;
		
				
		OHLCUpdate[] ohlcUpdates = new OHLCUpdate[bars.length * 2];
		
		for (int i = 0, j = 0, snapshotId = bars.length * -1; i < bars.length; i++, j+=2, snapshotId++)
		{
			MDDealableQuote emptyQoute = new MDDealableQuote(new BaseMDFeedEvent(marketDataFeed));
			emptyQoute.setSnapshotId(snapshotId);
			
			OHLCValue open_ohlc = bars[i].clone();
			open_ohlc.setJustOpened(true);
			open_ohlc.setClosed(false);
			//Historical barId always must be less than regular barId
			open_ohlc.setBarId(open_ohlc.getBarId() - Long.MAX_VALUE); 
			
			OHLCValue closed_ohlc = bars[i].clone();
			closed_ohlc.setJustOpened(false);
			closed_ohlc.setClosed(true);
			//Historical barId always must be less than regular barId
			closed_ohlc.setBarId(closed_ohlc.getBarId() - Long.MAX_VALUE); 
			
			ohlcUpdates[j] = new OHLCUpdate(historicalDataViewRequest.getTimeFrame(), open_ohlc, emptyQoute);
			ohlcUpdates[j + 1] = new OHLCUpdate(historicalDataViewRequest.getTimeFrame(), closed_ohlc, emptyQoute);
		}
		
		return ohlcUpdates;
	}
}
