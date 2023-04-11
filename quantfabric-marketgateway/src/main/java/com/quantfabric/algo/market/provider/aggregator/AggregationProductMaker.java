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

import java.util.HashMap;
import java.util.Map;

import com.quantfabric.algo.market.provider.aggregator.event.NoUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gate.access.product.producer.Producer;
import com.quantfabric.algo.market.gate.access.product.producer.ProducerException;
import com.quantfabric.algo.market.gate.access.product.producer.ProducerFactory;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublishersManager;

public class AggregationProductMaker implements MarketViewAggregatorListener
{
	private static final Logger logger = LoggerFactory.getLogger(AggregationProductMaker.class);
	private static class ComponentsBunch
	{
		ContentType contentType;
		Producer producer;
		Publisher publisher;
	}
	
	private final String productCode;
	private final Map<ContentType, ComponentsBunch> components = new HashMap<ContentType, ComponentsBunch>();
	
	public AggregationProductMaker(
			PublishersManager publishersManager, String productCode, ContentType[] contentTypes,
			Class<? extends MarketViewAggregator> aggregatorType, 
			ProducerFactory producerFactory) 
	{		
		if (publishersManager == null)
			throw new IllegalArgumentException("Publishers manager isn't set");	
		
		this.productCode = productCode;
		
		for (ContentType contentType : contentTypes)
		{
			ComponentsBunch bunch = new ComponentsBunch();
			bunch.contentType = contentType;
			bunch.producer = producerFactory.createProducer(productCode, contentType, aggregatorType);
			bunch.publisher = publishersManager.getPublisher(productCode, contentType);
			
			if (bunch.publisher == null)
				throw new IllegalArgumentException(String.format(
						"Publisher doesn't exists (productCode=%s, contentType=%s)", 
						productCode, contentType));		
			
			components.put(contentType, bunch);
		}
	}
	
	@Override
	public void update(MarketViewAggregator source, Object event,
			boolean forceProcessing)
	{
		if (!(event instanceof NoUpdates))
		{
			for (ComponentsBunch bunch : components.values())
			{
				try
				{
					for (Object dataBean : bunch.producer.produce(event))
						try
						{
							bunch.publisher.publish(dataBean);
						}
						catch (Exception e)
						{
							logger.error(String.format("Error during product publishing (productCode=%s, contentType=%s).",
										productCode, bunch.contentType), e);
							break;
						}
				}
				catch (ProducerException e)
				{
					logger.error(String.format("Product producing failed (productCode=%s, contentType=%s).",
								productCode, bunch.contentType), e);
					break;
				}
			}
		}
	}

	@Override
	public void update(MarketViewAggregator source, Object[] events,
			boolean forceProcessing)
	{
		for (Object e : events)
			update(source, e, forceProcessing);
	}
}
