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
package com.quantfabric.algo.market.gateway.access.agent;

import java.util.Properties;
import java.util.Set;

import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.Product;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.algo.market.gateway.access.subscription.SubscriptionOptions;
import com.quantfabric.util.XMLConfigParser;

public abstract class Agent {
	
	public abstract String getName();

	public abstract Set<Product> getProducts();

	public abstract boolean isStarted();

	public abstract void start() throws GatewayAgentException, RemoteGatewayException;

	public abstract void stop() throws RemoteGatewayException;

	public abstract String subscribe(String productCode, ContentType contentType, SubscriptionOptions subscriptionOption, Subscriber subscriber) throws RemoteGatewayException,
			GatewayAgentException;

	public abstract void unsubscribe(String subscriptionId) throws GatewayAgentException, RemoteGatewayException;
	
	public static Agent fromXML(Node node) throws GatewayAgentException {

		String name = node.getAttributes().getNamedItem("name").getTextContent();

		String agentFactoryName = node.getAttributes().getNamedItem("factory-class").getTextContent();

		Properties gatewayAgentProperties = XMLConfigParser.findAndParseSettingsNode((Element) node, "settings");

		try {
			AgentFactory agentFactory = (AgentFactory) Class.forName(agentFactoryName).newInstance();
			return agentFactory.createAgent(name, gatewayAgentProperties);
		}
		catch (Exception e) {
			throw new GatewayAgentException("Can't create agent.", e);
		}
	}
}
