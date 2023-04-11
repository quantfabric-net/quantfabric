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

import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherAddress;
import org.w3c.dom.Node;

import com.quantfabric.algo.market.datamodel.IncrementalUpdate;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gate.access.product.producer.FullBook;
import com.quantfabric.algo.market.gate.access.product.producer.TopOfBook;
import com.quantfabric.net.Transmitter;
import com.quantfabric.net.stream.Event;
import com.quantfabric.net.stream.StreamServer;
import com.quantfabric.net.stream.TypeRegistrator;
import com.quantfabric.net.stream.ZMQTransmitter;
import com.quantfabric.net.stream.kryo.KryoStreamServer;

public class ZMQPublisher implements Publisher
{
	private final String pubId;
	private final StreamServer streamServer;
	private final Transmitter transmitter;
	
	private final PublisherAddress address;
	
	public ZMQPublisher(String pubId, String endpointAddress, int port, ContentType contentType)
	{
		this.pubId = pubId;
		this.transmitter = new ZMQTransmitter(port);
		this.streamServer = new KryoStreamServer(transmitter);
		
		this.address = new PublisherAddress(endpointAddress, port, contentType);
		
		registerTypes(streamServer, contentType);
	}
	
	@Override
	public PublisherAddress getAddress()
	{
		return address;
	}
	
	public static void registerTypes(TypeRegistrator typeRegistrator,
			ContentType contentType)
	{
		typeRegistrator.registerType("Event", Event.class);
		typeRegistrator.registerType("TopOfBook", TopOfBook.class);	
		typeRegistrator.registerType("TopOfBook[]", TopOfBook[].class);
		typeRegistrator.registerType("FullBook", FullBook.class);	
		typeRegistrator.registerType("FullBook[]", FullBook[].class);
		typeRegistrator.registerType("IncrementalUpdate", IncrementalUpdate.class);
		typeRegistrator.registerType("IncrementalUpdate[]", IncrementalUpdate[].class);
	}
	
	@Override
	public void publish(Object dataBean) throws Exception
	{
		streamServer.send(new Event(pubId, dataBean));		
	}

	public static ZMQPublisher fromXml(Node rootNode, String endpointAddress)
	{
		String productCode = rootNode.getAttributes().getNamedItem("productCode").getNodeValue();
   		int port = Integer.parseInt(rootNode.getAttributes().getNamedItem("port").getNodeValue());
   		
		ContentType contentType = null;
		Node contentTypeNode = rootNode.getAttributes().getNamedItem("contentType");            		
		if (contentTypeNode != null)
			contentType = ContentType.valueOf(contentTypeNode.getNodeValue().trim());
		
		return new ZMQPublisher(productCode, endpointAddress, port, contentType);		
	}
}
