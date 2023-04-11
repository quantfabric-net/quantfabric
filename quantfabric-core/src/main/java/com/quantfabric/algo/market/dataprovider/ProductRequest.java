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
package com.quantfabric.algo.market.dataprovider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.net.stream.Event;
import com.quantfabric.util.Converter;
import com.quantfabric.util.PropertiesViewer;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;


public class ProductRequest extends AbstractDataViewRequest {	
	
	public static class ProductSubscriber implements Subscriber{

		private final com.quantfabric.messaging.Subscriber<Object> subscriber;
		
		public ProductSubscriber(com.quantfabric.messaging.Subscriber<Object> subscriber) {
			
			this.subscriber = subscriber;
		}
		
		@Override
		public void update(Event event) {
			
			subscriber.sendUpdate(event.getEventBean());
		}		
	}	
	
	private Set<String> dependences;
	private Map<String, String> parameters;
	
	private String serviceName;
	private String agentName;
	private String productCode;
	private Set<ContentType> contentTypes;
	
	public ProductRequest() {
		
		super();
	}

	public ProductRequest(String serviceName, String agentName, String productCode, Set<ContentType> contentTypes, Set<String> dependences, Map<String, String> parameters) {

		super(dependences, parameters);

		this.dependences = dependences;
		this.parameters = parameters;
		this.serviceName = serviceName;
		this.agentName = agentName;
		this.productCode = productCode;
		this.contentTypes = contentTypes;
	}
	
	
	public String getServiceName() {
		
		return serviceName;
	}

	
	public String getAgentName() {
		
		return agentName;
	}

	
	public String getProductCode() {
		
		return productCode;
	}

	
	public Set<ContentType> getContentTypes() {
		
		return Collections.unmodifiableSet(contentTypes);
	}

	@Override
	public String toString()
	{
		return "ProductRequest [dependences=" + dependences
				+ ", parameters=" + parameters + "]";
	}
	
	public static ProductRequest fromXML(Element element, Set<String> dependences, Map<String, String> parameters) throws NotSpecifiedProperty {
		
		Properties properties = Converter.mapToProperties(parameters);
		String serviceName = PropertiesViewer.getProperty(properties, "serviceName");
		String agentName = PropertiesViewer.getProperty(properties, "agentName");
		String productCode = PropertiesViewer.getProperty(properties, "productCode");
	
		Set<ContentType> contentTypes = new HashSet<ContentType>();
		
		for (String item : PropertiesViewer.getMultiProperty(properties, "contentTypes", ',')) {
			contentTypes.add(ContentType.valueOf(item));
		}
		
		return new ProductRequest(serviceName, agentName, productCode, contentTypes, dependences, parameters);
	}
	
}
