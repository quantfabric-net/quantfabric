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
package com.quantfabric.algo.market.gate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import com.quantfabric.algo.market.dataprovider.PipelineServiceDefinition;
import com.quantfabric.algo.market.gateway.access.agent.Agent;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.util.NodeListIterator;


public class GatewayAgentsProviderDefinition implements PipelineServiceDefinition {
	
	private final String name;
	private final Map<String, Agent> agents;
		
	public GatewayAgentsProviderDefinition(String name, Map<String, Agent> agents) {
		
		this.name = name;
		this.agents = agents;
	}
	
	public Collection<Agent> getGatewayAgents() {
		
		return Collections.unmodifiableCollection(agents.values());		
	}
		
	public Agent getAgent(String name) {
		
		return agents.get(name);
	}

	@Override
	public String getName() {
		
		return name;
	}
	
	public static GatewayAgentsProviderDefinition fromXML(Node node) throws GatewayAgentException {
		
		String name = node.getAttributes().getNamedItem("name").getTextContent();
		
		NodeListIterator agentsIterator = new NodeListIterator(node.getChildNodes());
		Map<String, Agent> agents = new HashMap<String, Agent>();
		
		while (agentsIterator.hasNext()) {
			Node iteratorNode = agentsIterator.next();
			if (iteratorNode.getNodeName().equals("agent")) {				
				Agent agent = Agent.fromXML(iteratorNode);
				agents.put(agent.getName(), agent);
			}
		}
		
		return new GatewayAgentsProviderDefinition(name, agents);				
	}
}
