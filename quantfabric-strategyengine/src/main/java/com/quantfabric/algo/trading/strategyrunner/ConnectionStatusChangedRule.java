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
package com.quantfabric.algo.trading.strategyrunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Node;

import com.quantfabric.algo.market.datamodel.StatusChanged.MarketConnectionStatuses;
import com.quantfabric.algo.market.gateway.MarketConnection.MarketConnectionMode;
import com.quantfabric.util.ConfigurationException;
import com.quantfabric.util.NodeListIterator;

public class ConnectionStatusChangedRule
{
	public static final String ANY = "ANY";
	
	public enum StrategyAction
	{
		NOTHING,
		START,
		STOP,
		DISABLE_EXECUTION,
		ENABLE_EXECUTION
	}
	
	private final String connectionName;
	private final MarketConnectionStatuses marketConnectionStatus;
	private final StrategyAction strategyAction;
	private final MarketConnectionMode marketConnectionMode;
	
	public ConnectionStatusChangedRule(String connectionName,
			MarketConnectionStatuses marketConnectionStatus,
			MarketConnectionMode marketConnectionMode,
			StrategyAction strategyAction)
	{
		super();
		this.connectionName = connectionName;
		this.marketConnectionStatus = marketConnectionStatus;
		this.marketConnectionMode = marketConnectionMode;
		this.strategyAction = strategyAction;
	}
	
	public String getConnectionName()
	{
		return connectionName;
	}
	public MarketConnectionStatuses getMarketConnectionStatus()
	{
		return marketConnectionStatus;
	}
	public MarketConnectionMode getMarketConnectionMode()
	{
		return marketConnectionMode;
	}
	public StrategyAction getStrategyAction()
	{
		return strategyAction;
	}
	
	@Override
	public String toString()
	{
		return "ConnectionStatusChangedRule [connectionName=" + connectionName
				+ ", marketConnectionStatus=" + marketConnectionStatus
				+ ", strategyAction=" + strategyAction
				+ ", marketConnectionMode=" + marketConnectionMode + "]";
	}

	public static Collection<ConnectionStatusChangedRule> fromXML(Node rulesNode) throws ConfigurationException
	{	
		List<ConnectionStatusChangedRule> connectionStatusChangedRules = new ArrayList<ConnectionStatusChangedRule>();
		
		NodeListIterator rulesIterator = new NodeListIterator(rulesNode.getChildNodes());
	
		while(rulesIterator.hasNext())
		{
			Node ruleElement = rulesIterator.next();
			
			if (!ruleElement.getNodeName().equals("rule"))
				continue;
				
			Node marketConnectionModeAttr = ruleElement.getAttributes().getNamedItem("connectionMode");
			String marketConnectionModeAttrValue = ConnectionStatusChangedRule.ANY;
			if (marketConnectionModeAttr != null)
				marketConnectionModeAttrValue = marketConnectionModeAttr.getTextContent().toUpperCase().trim();		
			
			Node connectionNamesAttr = ruleElement.getAttributes().getNamedItem("connectionNames");
			String connectionNamesAttrValue = ConnectionStatusChangedRule.ANY;
			if (connectionNamesAttr != null)
				connectionNamesAttrValue = connectionNamesAttr.getTextContent().trim();
			
			Node connectionStatusAttr = ruleElement.getAttributes().getNamedItem("connectionStatus");
			MarketConnectionStatuses marketConnectionStatus = null;
			if (connectionStatusAttr != null)
				marketConnectionStatus = MarketConnectionStatuses.valueOf(connectionStatusAttr.getTextContent().toUpperCase().trim());
			else
				throw new ConfigurationException("Attribute \"connectionStatus\" is required");
			
			Node strategyActionAttr = ruleElement.getAttributes().getNamedItem("action");
			StrategyAction strategyAction = null;
			if (strategyActionAttr != null)
				strategyAction = StrategyAction.valueOf(strategyActionAttr.getTextContent().toUpperCase().trim());
			else
				throw new ConfigurationException("Attribute \"action\" is required");
			
			if (connectionNamesAttrValue.equals(ConnectionStatusChangedRule.ANY))
			{
				makeRules(connectionStatusChangedRules, ConnectionStatusChangedRule.ANY, marketConnectionModeAttrValue, 
						marketConnectionStatus, strategyAction);
			}
			else
			{
				String[] connectionNames = connectionNamesAttr.getTextContent().split(",");
				for (String connectionName : connectionNames)
					makeRules(connectionStatusChangedRules, connectionName.trim(), marketConnectionModeAttrValue, 
							marketConnectionStatus, strategyAction);
			}
		}
		
		return connectionStatusChangedRules;
	}

	private static void makeRules(
			List<ConnectionStatusChangedRule> connectionStatusChangedRulesCollection,
			String connectionName,
			String marketConnectionModeAttrValue,
			MarketConnectionStatuses marketConnectionStatus,
			StrategyAction strategyAction)
	{
		if (marketConnectionModeAttrValue.equals(ConnectionStatusChangedRule.ANY))
			for (MarketConnectionMode marketConnectionMode :  MarketConnectionMode.values())
				makeRules(connectionStatusChangedRulesCollection, connectionName, 
						marketConnectionMode.name(), marketConnectionStatus, strategyAction);
		else
			connectionStatusChangedRulesCollection.add(new ConnectionStatusChangedRule(connectionName, marketConnectionStatus,
					MarketConnectionMode.valueOf(marketConnectionModeAttrValue), strategyAction));		
	}
}
