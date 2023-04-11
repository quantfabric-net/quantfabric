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
package com.quantfabric.algo.market.gate.access.product.publisher;

import java.util.HashMap;
import java.util.Map;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublishersManager;

public class PublishersContainer implements PublishersManager
{
	private final Map<String, Map<ContentType, Publisher>> publishers =
			new HashMap<String, Map<ContentType,Publisher>>();
	
	private final String name;
	
	public PublishersContainer(String name)
	{
		this.name = name;
	}
	
	@Override
	public Publisher getPublisher(String productCode, ContentType contentType)
	{
		return publishers.containsKey(productCode) ? 
			publishers.get(productCode).get(contentType) : null;
	}
	
	public Publisher[] getPublishers(String productCode)
	{
		Publisher[] publishersArray = new Publisher[]{};
		
		return publishers.containsKey(productCode) ? 
				publishers.get(productCode).values().toArray(publishersArray) : publishersArray;
	}
	
	public void addPublisher(String productCode, ContentType contentType, Publisher publisher)
	{
		if (!publishers.containsKey(productCode))
			publishers.put(productCode, new HashMap<ContentType, Publisher>());
		
		publishers.get(productCode).put(contentType, publisher);
	}

	@Override
	public String getName()
	{
		return name;
	}
}
