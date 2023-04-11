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

import com.quantfabric.algo.market.datamodel.ComplexAccumulatedOHLC;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.algo.market.gateway.access.product.ContentType;


public class ComplexOHLCProducer extends AbstractProducer<ComplexAccumulatedOHLC, ComplexOHLC>{
	
	private String productCode;
	
	public ComplexOHLCProducer() {
		
		super();		
	}
	
	public ComplexOHLCProducer(String productCode) {
		
		super();
		this.productCode = productCode;
	}
	
	@Override
	public ContentType getContentType() {

		return ContentType.COMPLEX_OHLC;
	}

	@Override
	protected ComplexAccumulatedOHLC cast(Object sourceData) throws ProducerException {
		
		if (sourceData instanceof ComplexAccumulatedOHLC) 
			return (ComplexAccumulatedOHLC) sourceData;
		else
			throw new ProducerException("sourceData (" + sourceData.getClass() + ") can't be cast to OHLCUpdate.");	
	}

	@Override
	public ComplexOHLC[] make(ComplexAccumulatedOHLC sourceData) throws ProducerException {
		
		OHLCValue genericOhlcValue = sourceData.getGenericOHLCUpdate().getOHLC();	
		OHLCValue tradeOhlcValue = sourceData.getTradeOHLCUpdate().getOHLC();
		
		return new ComplexOHLC[] { new ComplexOHLC(productCode, sourceData.getSnapshotId(), sourceData.isClosed(), sourceData.isClosedByTimeout(), sourceData.getClosedTimestamp(),
				
				genericOhlcValue.getOpen(), genericOhlcValue.getOpenSourceTimestamp(), genericOhlcValue.getHigh(), genericOhlcValue.getHighSourceTimestamp(), 
				genericOhlcValue.getLow(), genericOhlcValue.getLowSourceTimestamp(), genericOhlcValue.getClose(), 
				genericOhlcValue.getCloseSourceTimestamp(),  
				genericOhlcValue.getTimeFrameInSeconds(), genericOhlcValue.getBarId(), 
				
				tradeOhlcValue.getOpen(), tradeOhlcValue.getOpenSourceTimestamp(), tradeOhlcValue.getHigh(), tradeOhlcValue.getHighSourceTimestamp(), 
				tradeOhlcValue.getLow(), tradeOhlcValue.getLowSourceTimestamp(), tradeOhlcValue.getClose(), 
				tradeOhlcValue.getCloseSourceTimestamp(), 
				tradeOhlcValue.getTimeFrameInSeconds(), tradeOhlcValue.getBarId(),
				
				tradeOhlcValue.getTradeCount(), tradeOhlcValue.getBuyCount(), tradeOhlcValue.getSellCount(),
				tradeOhlcValue.getBuySellRatio(), tradeOhlcValue.getAvgBuy(), tradeOhlcValue.getAvgSell()
				) };
	}
}
