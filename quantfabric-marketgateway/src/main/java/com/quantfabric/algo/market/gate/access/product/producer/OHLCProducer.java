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
package com.quantfabric.algo.market.gate.access.product.producer;

import com.quantfabric.algo.market.datamodel.OHLCUpdate;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.gateway.access.product.ContentType;


public class OHLCProducer extends AbstractProducer<OHLCUpdate, OHLC>{
	
	private String productCode;
	
	public OHLCProducer() {
		
		super();		
	}
	
	public OHLCProducer(String productCode) {
		
		super();
		this.productCode = productCode;
	}
	
	@Override
	public ContentType getContentType() {

		return ContentType.OHLC;
	}

	@Override
	protected OHLCUpdate cast(Object sourceData) throws ProducerException {
		
		if (sourceData instanceof OHLCUpdate) 
			return (OHLCUpdate) sourceData;
		else
			throw new ProducerException("sourceData (" + sourceData.getClass() + ") can't be cast to OHLCUpdate.");	
	}

	@Override
	public OHLC[] make(OHLCUpdate sourceData) throws ProducerException {
		
		OHLCValue ohlcValue = sourceData.getOHLC();
		
		return new OHLC[] { new OHLC(sourceData.getSnapshotId(), ohlcValue.getOpen(), ohlcValue.getOpenSourceTimestamp(), ohlcValue.getHigh(),
				ohlcValue.getHighSourceTimestamp(), ohlcValue.getLow(), ohlcValue.getLowSourceTimestamp(), ohlcValue.getClose(),
				ohlcValue.getCloseSourceTimestamp(), ohlcValue.isClosed(), ohlcValue.getCloseTimestamp(), ohlcValue.getTimeFrameInSeconds(), productCode,
				ohlcValue.getBarId()) };
	}
}
