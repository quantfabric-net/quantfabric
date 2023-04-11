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
package com.quantfabric.algo.market.gate.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.gateway.MarketConnection;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

public class InternalObjectNameUtil
{

	public static ObjectName toObjectNameMarketConnection(String domain,
			MarketConnection connection) throws MalformedObjectNameException
	{
		String connectionName = connection.getName();
		return new ObjectName(
                domain +
                        ":type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=" +
                        connectionName);
	}

	public static ObjectName toObjectNameMarketDataPipeline(String domain,
			MarketDataPipeline pipeline) throws MalformedObjectNameException
	{
		return new ObjectName(toObjectNameMarketDataPipelineAsString(domain, pipeline));
	}
	
	public static String toObjectNameMarketDataPipelineAsString(String domain,
			MarketDataPipeline pipeline) 
	{
		String pipelineName = pipeline.getName();
		return domain +
                ":type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Pipelines,pipelineName=" +
                pipelineName;
	}
	
	public static String rootForMarketDataFeeds(String domain, String connectionName)
	{
		return rootForFeeds(domain, connectionName, "MarketData");
	}
	
	public static String rootForExecutionFeeds(String domain, String connectionName)
	{
		return rootForFeeds(domain, connectionName, "Execution");
	}
	
	private static String rootForFeeds(String domain, String connectionName, String feedsType)
	{
		return domain +
                ":type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=" +
                connectionName +
                ",connection_group=Feeds" +
                ",feed_type=" +
                feedsType +
                ",feedName=";
	}
	
	public static ObjectName toObjectNameFeed(String domain,
			Feed feed, MarketConnection connection) throws MalformedObjectNameException
	{
		String feedName = feed.getFeedName().getName();
		String connectionName = connection.getName();
				
		if(feed instanceof ExecutionFeed)
			return new ObjectName(rootForExecutionFeeds(domain, connectionName) + feedName);
		
		if(feed instanceof MarketDataFeed)
			return new ObjectName(rootForMarketDataFeeds(domain, connectionName) + feedName);
					
		return null;
	}
	
	public static ObjectName toObjectNameInstrument(String domain,
			Instrument instrument) throws MalformedObjectNameException
	{
		String instrumentName = instrument.getSymbol();
		return new ObjectName(
                domain +
                        ":type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Instruments,instrumentName=" +
                        instrumentName);
	}
}
