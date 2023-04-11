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

import com.quantfabric.algo.market.datamodel.ComplexMarketView;
import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.gateway.access.product.ContentType;


public class ComplexMarketViewProducer  extends AbstractProducer<ComplexMarketView, ComplexView>{
	
	private String productCode;
	
	public ComplexMarketViewProducer() {
		
		super();		
	}
	
	public ComplexMarketViewProducer(String productCode) {
		
		super();
		this.productCode = productCode;
	}
	
	@Override
	public ContentType getContentType() {

		return ContentType.COMPLEX_MARKET_VIEW;
	}

	@Override
	protected ComplexMarketView cast(Object sourceData) throws ProducerException {
		
		if (sourceData instanceof ComplexMarketView) 
			return (ComplexMarketView) sourceData;
		else
			throw new ProducerException("sourceData (" + sourceData.getClass() + ") can't be cast to ComplexMarketView.");	
	}

	@Override
	public ComplexView[] make(ComplexMarketView sourceData) throws ProducerException {
		
		MDDealableQuote topQuote = sourceData.getTopQuote();
		MDTrade trade = topQuote.getTrade();
		
		return new ComplexView[] { new ComplexView(
				sourceData.getSnapshotId(), 
				sourceData.getMidTopPrice(), 
				sourceData.getMidVWAPPrice(),
				sourceData.getMidOWAPPrice(),
				productCode,
				topQuote.getBidPrice(), 
				(int)topQuote.getBidSize(), 
				topQuote.getOfferPrice(), 
				(int)topQuote.getOfferSize(),
				topQuote.getSourceTimestamp(),
				sourceData.getBidVWAP().getPrice(),
				sourceData.getBidVWAP().getSize(),
				sourceData.getBidOWAP().getPrice(),
				sourceData.getBidOWAP().getAmountOrders(),
				sourceData.getOfferVWAP().getPrice(),
				sourceData.getOfferVWAP().getSize(),
				sourceData.getOfferOWAP().getPrice(),
				sourceData.getOfferOWAP().getAmountOrders(),
				sourceData.getBidVWAP().getDepth(), 
				sourceData.getFeedName(),
				sourceData.getInstrumentId(),
				trade.getTradeId(),
				trade.getPrice(),
				trade.getCurrency(),
				trade.getTradeSide().toString(),
				trade.getSourceTimestamp()
				) };
	}
}