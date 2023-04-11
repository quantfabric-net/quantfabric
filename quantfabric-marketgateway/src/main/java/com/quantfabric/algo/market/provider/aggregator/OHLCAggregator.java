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

import com.quantfabric.algo.cep.indicators.ohlc.OHLCCalculator;
import com.quantfabric.algo.cep.indicators.ohlc.OHLCUpdateListener;
import com.quantfabric.algo.market.datamodel.ComplexAccumulatedOHLC;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProvider;
import com.quantfabric.algo.market.history.TimeFrame;
import com.quantfabric.algo.market.history.TimeFrameHistoryRecorder;
import com.quantfabric.algo.market.history.TimeFrameHistoryTable;
import com.quantfabric.algo.market.history.TimeFrameHistoryView;
import com.quantfabric.util.timeframes.TimeframeFactory;

public class OHLCAggregator extends TopMDQuoteAggregator
{
	private OHLCCalculator ohlcCalculator;
	protected TimeFrameHistoryRecorder historyRecorder;
	protected TimeFrameHistoryView historyView;
	
	public OHLCAggregator(String name, Properties properties)
	{
		super(name, properties);
			
		final String timeframe = properties.getProperty("timeFrame");
		
		if (timeframe != null)
		{
			MultiTimeFrameHistoryProvider multiTimeFrameHistoryProvider = 
				(MultiTimeFrameHistoryProvider) properties.get("multiTimeFrameHistoryProvider");
			
			if (multiTimeFrameHistoryProvider != null)
			{
				TimeFrameHistoryTable historyTable = multiTimeFrameHistoryProvider.getTimeFrameHandler(
						TimeFrame.getTimeFrame(TimeframeFactory.getTimeframe(timeframe).getLengthInSeconds()));
				
				if (properties.containsKey("isHistoryRecorder") && 
						Boolean.parseBoolean(properties.getProperty("isHistoryRecorder", "false")))
					historyRecorder = historyTable;
				
				historyView = historyTable;
			}			
			
			String timeOffsetProperty = properties.getProperty("timeOffset");
			int timeOffset = 0;
			if (timeOffsetProperty != null)
				timeOffset = Integer.parseInt(timeOffsetProperty);
			
			ohlcCalculator = new OHLCCalculator(timeframe, timeOffset);
			ohlcCalculator.setListener(
				new OHLCUpdateListener() 
				{	
					@Override
					public void update(OHLCValue value)
					{
						OHLCAggregator.this.callSuperPublish(new OHLCUpdate(timeframe, value, getTopQuote()));
						
						if (historyRecorder != null)							
							if (value.isJustOpened()) 
							{								
								historyRecorder.addBar(value);
							}
							else
							{
								historyRecorder.replaceBar(value);
							}
					}

					@Override
					public void update(ComplexAccumulatedOHLC value) {}
				});
		}		
	}

	protected void callSuperPublish(OHLCUpdate update)
	{
		super.publish(update, update.getOHLC().isClosed());		
	}

	@Override
	public void publish(Object event, boolean forceProcessing)
	{
		if (event instanceof MDDealableQuote)
		{
			MDDealableQuote topQuote = (MDDealableQuote)event;
			
			try
			{
				if (!ohlcCalculator.isInitialized() && historyView != null)
				{
					OHLCValue openBar = historyView.getOpenBar();
					if (openBar != null)
					{
						if (ohlcCalculator.getTimeframe().interval(new Date(openBar.getCloseSourceTimestamp())).equals(
							ohlcCalculator.getTimeframe().interval(new Date(topQuote.getSourceTimestamp()))))
						{
							getLogger().info(
									"OHLC calculator ("+ topQuote.getFeedName() + " " + ohlcCalculator.getTimeframe().getLengthInSeconds()
									+ ") synchronized (accuracy: " + (topQuote.getSourceTimestamp() - openBar.getCloseSourceTimestamp()) 
									+ "ms) by " + openBar);
							ohlcCalculator.init(openBar);	
							
							if (openBar.getCloseSourceTimestamp() < topQuote.getSourceTimestamp())
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
			catch (Exception e)
			{
				getLogger().error("Update OHLCCalculator failed.", e);
			}			
		}
	}
	
	protected void updateCalculator(MDDealableQuote topQuote) throws Exception
	{
		if (topQuote.getBidPrice() != 0)
			ohlcCalculator.update(topQuote.getSourceTimestamp(), topQuote.getBidPrice());
	}
}
