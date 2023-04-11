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

import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.gateway.access.product.ContentType;

public class TopOfBookProducer extends AbstractProducer<MDDealableQuote, TopOfBook>
{
	private String productCode;
	
	public TopOfBookProducer() {
		
		super();
	}
	
	public TopOfBookProducer(String productCode) {
		
		super();
		this.productCode = productCode;
	}
	
	@Override
	public ContentType getContentType()
	{
		return ContentType.TOP_OF_BOOK;
	}

	@Override
	protected MDDealableQuote cast(Object sourceData)
			throws ProducerException
	{
		if (sourceData instanceof MDDealableQuote) 
			return (MDDealableQuote) sourceData;
		else
			throw new ProducerException("sourceData can't be cast to MDDealableQuote");	
	}

	@Override
	public TopOfBook[] make(MDDealableQuote sourceData)
			throws ProducerException
	{
		return new TopOfBook[] { new TopOfBook(sourceData.getSnapshotId(), sourceData.getBidPrice(), (int) sourceData.getBidSize(), sourceData.getOfferPrice(),
				(int) sourceData.getOfferSize(), productCode, sourceData.getSourceTimestamp()) };
	}
}
