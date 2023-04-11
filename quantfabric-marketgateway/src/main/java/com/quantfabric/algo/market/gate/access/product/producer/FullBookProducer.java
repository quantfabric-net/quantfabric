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

import java.util.List;

import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.gateway.access.product.ContentType;

public class FullBookProducer extends AbstractProducer<MDOrderBook, FullBook>
{
	private String productCode;
	
	public FullBookProducer() {
		
		super();
	}
	
	public FullBookProducer(String productCode) {
		
		super();
		this.productCode = productCode;
	}
	
	@Override
	public ContentType getContentType()
	{
		return ContentType.FULL_BOOK;
	}

	@Override
	protected MDOrderBook cast(Object sourceData) throws ProducerException
	{
		if (sourceData instanceof MDOrderBook) 
			return (MDOrderBook) sourceData;
		else
			throw new ProducerException("sourceData can't be cast to MDOrderBook");
	}

	@Override
	public FullBook[] make(MDOrderBook sourceData) throws ProducerException
	{
		List<MDPrice> bids = sourceData.getBids().getAllLevels();
		
		long[] bidPrices = new long[bids.size()];
		long[] bidVolumes = new long[bids.size()];
		
		int i = 0;
		for (MDPrice price : bids)
		{
			bidPrices[i] = price.getPrice();
			bidVolumes[i] = (int)price.getSize();
			i++;
		}
			
		List<MDPrice> asks = sourceData.getOffers().getAllLevels();

		long[] askPrices = new long[asks.size()];
		long[] askVolumes = new long[asks.size()];
		
		i = 0;
		for (MDPrice price : asks)
		{
			askPrices[i] = price.getPrice();
			askVolumes[i] = (int)price.getSize();
			i++;
		}
			
		return new FullBook[] { new FullBook(sourceData.getSnapshotId(), bidPrices, bidVolumes, askPrices, askVolumes,
				productCode, sourceData.getSourceTimestamp(), sourceData.getInstrumentId(), sourceData.getSymbol(),
				sourceData.getFeedName()) };
	}
}
