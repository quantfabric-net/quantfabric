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
package com.quantfabric.algo.market.provider.aggregator.util;

import com.quantfabric.algo.instrument.InstrumentImpl;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.net.stream.DataFilter;
import com.quantfabric.net.stream.DataStreamer;
import com.quantfabric.net.stream.StreamServer;
import com.quantfabric.net.stream.TypeRegistrator;

public class AggregatorUpdatesDataStreamer extends DataStreamer
{
	public AggregatorUpdatesDataStreamer(String dataSource, StreamServer streamServer)
	{
		super(dataSource, streamServer);
	}

	@Override
	protected DataFilter createDataFilter(DataStreamer dataStreamer)
	{
		return new DataFilter(this) {
			
			@Override
			protected boolean filter(Object data)
			{
                return data instanceof OHLCUpdate;
            }
		};
	}

	@Override
	protected void registerTypes(TypeRegistrator typeRegistrator)
	{
		registerUsingTypes(typeRegistrator);
	}
	
	public static void registerUsingTypes(TypeRegistrator typeRegistrator)
	{
		typeRegistrator.registerType("OHLCUpdate", OHLCUpdate.class);
		typeRegistrator.registerType("OHLCValue", OHLCValue.class);
		typeRegistrator.registerType("MDDealableQuote", MDDealableQuote.class);
		typeRegistrator.registerType("MDDealableQuote$UpdateStatuses", MDDealableQuote.UpdateStatuses.class);
		typeRegistrator.registerType("FeedNameImpl", FeedNameImpl.class);
		typeRegistrator.registerType("InstrumentImpl", InstrumentImpl.class);
	}
}
