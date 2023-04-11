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
package com.quantfabric.algo.market.gate.access.product;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Description;
import com.quantfabric.algo.market.gateway.access.product.Product;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherManagersProvider;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublishersManager;
import com.quantfabric.util.XMLConfigParser;

public class SimpleProduct implements Product
{
	private static final long serialVersionUID = 4226006658015250515L;
	
	private final String productCode;
	private final Description descrition;
	private final Set<ContentType> availableContentTypes;
	private final Map<ContentType, PublisherAddress> publishers;
	
	public SimpleProduct(String productCode, Description descrition, Map<ContentType, PublisherAddress> publishers)
	{
		this.productCode = productCode;
		this.descrition = descrition;
		this.availableContentTypes = new HashSet<ContentType>(publishers.keySet());
		this.publishers = publishers;		
	}

	@Override
	public String getProductCode()
	{
		return productCode;
	}

	@Override
	public Description getDescription()
	{
		return descrition;
	}

	@Override
	public Set<ContentType> getAvailableContentTypes()
	{
		return availableContentTypes;
	}

	@Override
	public PublisherAddress getPublisherAddress(ContentType contentType)
	{
		return publishers.get(contentType);
	}
	
	public static Product fromXml(Node rootNode, PublisherManagersProvider publisherManagersProvider)
	{
		Node productCodeNode = rootNode.getAttributes().getNamedItem("productCode");		
		String productCode = productCodeNode.getNodeValue();
		
		Node instrumentNode = rootNode.getAttributes().getNamedItem("instrument");
		String instrument = instrumentNode.getNodeValue();
		
		Map<ContentType, String> productSources = new HashMap<ContentType, String>();
		
		Element publishersNode = XMLConfigParser.findNode((Element)rootNode, "publishers");
		
		if (publishersNode != null)
		{
			DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(publishersNode.getChildNodes());
	        while (eventTypeNodeIterator.hasNext())
	        {
	            Element element = eventTypeNodeIterator.next();
	            String nodeName = element.getNodeName();
	            if (nodeName.equals("publisher"))
	            {
	            	ContentType contentType = null;
            		String contentTypeStr = element.getAttribute("contentType");            		
            		if (contentTypeStr != null)
            			contentType = ContentType.valueOf(contentTypeStr.trim());
            		
            		String managerName = element.getAttribute("managerName");
            		
            		productSources.put(contentType, managerName);
	            
	            }
	        }
		}
		
		Map<ContentType, PublisherAddress> publishers = new HashMap<ContentType, PublisherAddress>();		
		
		for (ContentType contentType : productSources.keySet())
		{
			PublishersManager publishersManager = 
					publisherManagersProvider.getPublishersManager(productSources.get(contentType));
			
			if (publishersManager != null)	
			{
				Publisher publisher = publishersManager.getPublisher(productCode, contentType);
			
				if (publisher != null)
					publishers.put(contentType, publisher.getAddress());
			}
		}
		
		return new SimpleProduct(productCode, new SimpleDescription(instrument), publishers);		
	}
}
