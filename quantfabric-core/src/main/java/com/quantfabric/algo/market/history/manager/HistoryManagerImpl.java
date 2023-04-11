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

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProvider;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDefinition;
import com.quantfabric.algo.market.history.TimeFrame;
import com.quantfabric.algo.market.history.TimeFrameHistoryTable;


public class HistoryManagerImpl implements HistoryManager
{
	private static final Logger log = LoggerFactory.getLogger(HistoryManagerImpl.class);
	
	MultiTimeFrameHistoryProvider historyProvider;
	
	public HistoryManagerImpl(MultiTimeFrameHistoryProvider historyProvider)
	{
		this.historyProvider = historyProvider;
	}
	
	@Override
	public Collection<OHLCValue> exportHistory(TimeFrame timeFrame)
	{
		Properties properties = historyProvider.getDefinition().getProperties();			
		int size = Integer.parseInt(
				properties.getProperty("historyCapacity", "1000"));

		return exportHistory(timeFrame, size);
	}
	
	@Override
	public Collection<OHLCValue> exportHistory(TimeFrame timeFrame, int depth)
	{
		TimeFrameHistoryTable historyTable =
				historyProvider.getTimeFrameHandler(timeFrame);
		
		if (historyTable != null)
		{
			OHLCValue[] historyArray = historyTable.getBars(depth);
			
			return Arrays.asList(historyArray);
		}
		
		return null;
	}

	@Override
	public void importHistory(TimeFrame timeFrame, Collection<OHLCValue> bars)
	{
		TimeFrameHistoryTable historyTable =
				historyProvider.getTimeFrameHandler(timeFrame);
		
		if (historyTable != null)
		{
			for (OHLCValue bar : bars)
				historyTable.addBar(bar);
		}		
	}
	
	@Override
	public Collection<Long> getGapIndexes(TimeFrame timeFrame)
	{
		TimeFrameHistoryTable historyTable =
				historyProvider.getTimeFrameHandler(timeFrame);
		
		if (historyTable != null)
		{
			return historyTable.getGapIndexes();
		}
		
		return null;
	}

	@Override
	public void replaceBar(TimeFrame timeFrame, OHLCValue ohlcValue)
	{
		TimeFrameHistoryTable historyTable =
				historyProvider.getTimeFrameHandler(timeFrame);
		
		if (historyTable != null)
		{
			historyTable.replaceBar(ohlcValue);
		}
	}

	@Override
	public void addBar(TimeFrame timeFrame, OHLCValue ohlcValue)
	{
		TimeFrameHistoryTable historyTable =
				historyProvider.getTimeFrameHandler(timeFrame);
		
		if (historyTable != null)
		{
			historyTable.addBar(ohlcValue);
		}		
	}

	public static MultiTimeFrameHistoryProvider createMockHistoryProvider()
	{	
		MultiTimeFrameHistoryProviderDefinition def = EasyMock.createNiceMock(MultiTimeFrameHistoryProviderDefinition.class);
		EasyMock.expect(def.getProperties()).andReturn(new Properties()).anyTimes();
		EasyMock.replay(def);
		
		TimeFrameHistoryTable table = EasyMock.createNiceMock(TimeFrameHistoryTable.class);
		EasyMock.expect(table.getBars(EasyMock.anyInt())).andReturn(new OHLCValue[]{null, null, null}).anyTimes();
		EasyMock.replay(table);
				
		MultiTimeFrameHistoryProvider historyProvider =  EasyMock.createNiceMock(MultiTimeFrameHistoryProvider.class);
		EasyMock.expect(historyProvider.getTimeFrameHandler(EasyMock.anyObject(TimeFrame.class))).andReturn(table);
		EasyMock.expect(historyProvider.getDefinition()).andReturn(def).anyTimes();
				
		EasyMock.replay(historyProvider);
		
		historyProvider.getDefinition().getProperties().setProperty("historyCapacity", "1000");
		
		return historyProvider;
	}
}
