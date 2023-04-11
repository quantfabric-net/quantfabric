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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.quantfabric.algo.market.dataprovider.*;
import com.quantfabric.algo.market.provider.DefaultDataViews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.algo.market.gate.GatewayAgentsProviderDefinition;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDefinitionImpl;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.XMLConfigParser;

public class AggregatingMarketDataPipelineBuilder implements MarketDataPipelineBuider
{
	private static final Logger log = LoggerFactory.getLogger(AggregatingMarketDataPipelineBuilder.class);
	public static Logger getLogger()
	{
		return log;
	}

	@Override
	public MarketDataPipeline buildPipeline(
			Node piplelineConfigRoot,
			MarketGateway marketGateway, 
			Configuration cepConfig,
			Collection<PersistingUpdateListenerConfig> persisterConfigs) throws Exception
	{
		String factoryClass = null;
		String storage = null;
		String name = null;
		
		Map<FeedReference, Collection<AggregatorDefinition>> aggregatorDefinitions = 
				new HashMap<FeedReference, Collection<AggregatorDefinition>>();
		
		List<PipelineServiceDefinition> pipelineServiceDefinition = new ArrayList<PipelineServiceDefinition>();
		List<DataView> dataViewDescriptions = new ArrayList<>();
		
		String pipelineName = piplelineConfigRoot.getAttributes().getNamedItem("name").getTextContent();
		String pipelineThreadModel = piplelineConfigRoot.getAttributes().getNamedItem("threadModel").getTextContent();
		boolean singleThread = pipelineThreadModel.equalsIgnoreCase("single");
		
		DOMElementIterator nodeIterator = new DOMElementIterator(piplelineConfigRoot.getChildNodes());
			
		while (nodeIterator.hasNext())
		{
			Element subElement = nodeIterator.next();
			String nodeNeme = subElement.getNodeName();
			if (nodeNeme.equals("services")){
				DOMElementIterator servicesNodeIterator = new DOMElementIterator(subElement.getChildNodes());
				while(servicesNodeIterator.hasNext())
				{
					Element servicesSubElement = servicesNodeIterator.next();
					String servicesSubName = servicesSubElement.getNodeName();
					
					if (servicesSubName.equals("multiTimeFrameHistoryProvider"))
					{
						factoryClass = servicesSubElement.getAttributes().getNamedItem("factory-class").getTextContent();
						storage = servicesSubElement.getAttributes().getNamedItem("storage").getTextContent();
						name = servicesSubElement.getAttributes().getNamedItem("name").getTextContent();
						
						Properties properties = findSettings(new DOMElementIterator(servicesSubElement.getChildNodes()));
						
						try
						{
							pipelineServiceDefinition.add(
									new MultiTimeFrameHistoryProviderDefinitionImpl(name, factoryClass, 
											QuantfabricRuntime.sabstituteVariableValues(storage), properties));
						}
						catch (Exception e)
						{
							throw new MarketDataPipelineBuiderException(
									"Building MultiTimeFrameHistoryProviderDefinition failed", e);
						}
					}
					
					if (servicesSubName.equals("gatewayAgentsProvider")) {

						if (servicesSubElement != null) {
							
							try {
								pipelineServiceDefinition.add(GatewayAgentsProviderDefinition.fromXML(servicesSubElement));
							}
							catch (Exception e) {
								throw new MarketDataPipelineBuiderException("Building GatewayAgentsProviderDefinition failed", e);
							}
						}
					}
				}
			}	
			
			else if (nodeNeme.equals("marketDataFeeds")){
				List<AggregatorDefinition> commonAggregators = new ArrayList<AggregatorDefinition>(); 
				
				DOMElementIterator marketDataFeedsNodeIterator = new DOMElementIterator(subElement.getChildNodes());
				
				while(marketDataFeedsNodeIterator.hasNext())
				{
					Element marketSubElement = marketDataFeedsNodeIterator.next();
					String marketSubName = marketSubElement.getNodeName();
					
					if (marketSubName.equals("common-marketViewAggregators"))
					{
						DOMElementIterator commonMarketViewNodeIterator = new DOMElementIterator(marketSubElement.getChildNodes());
						while(commonMarketViewNodeIterator.hasNext())
						{
							Element marketViewSubElement = commonMarketViewNodeIterator.next();
							if (marketViewSubElement.getNodeName().equals("aggregator"))
							{								
								commonAggregators.add(AggregatorDefinitionImpl.load(marketViewSubElement));
							}
						}
					}			
					
					if (marketSubName.equals("feed"))
					{
						String feedName = marketSubElement.getAttribute("name");
                        String connection = marketSubElement.getAttribute("connection");

						FeedReference feed = 
    							new FeedReferenceImpl(new FeedNameImpl(feedName), connection);
						
						aggregatorDefinitions.put(feed, new ArrayList<AggregatorDefinition>());
						
						DOMElementIterator marketViewAggregatorsNodeIterator = new DOMElementIterator(marketSubElement.getChildNodes());
						while(marketViewAggregatorsNodeIterator.hasNext())
						{
							Element marketViewAggregatorsSubElement = marketViewAggregatorsNodeIterator.next();
							if (marketViewAggregatorsSubElement.getNodeName().equals("marketViewAggregators"))
							{
								DOMElementIterator marketAggregatorsNodeIterator = new DOMElementIterator(marketViewAggregatorsSubElement.getChildNodes());
								while(marketAggregatorsNodeIterator.hasNext())
								{
									Element marketAggregatorsSubElement = marketAggregatorsNodeIterator.next();
									if (marketAggregatorsSubElement.getNodeName().equals("aggregator"))
									{										
										AggregatorDefinition aggregatorDefinition = AggregatorDefinitionImpl.load(marketAggregatorsSubElement);
										if (aggregatorDefinition != null)
											aggregatorDefinitions.get(feed).add(aggregatorDefinition);
									}
								}								
							}
						}
					}
				}
				
				for (FeedReference feed : aggregatorDefinitions.keySet())
					aggregatorDefinitions.get(feed).addAll(commonAggregators);					
			}

			else if (nodeNeme.equals("dataViews")){
				DOMElementIterator dataViewNodeIterator = new DOMElementIterator(subElement.getChildNodes());
				while(dataViewNodeIterator.hasNext()) {
					Element servicesSubElement = dataViewNodeIterator.next();
					String servicesSubName = servicesSubElement.getNodeName();

					if (servicesSubName.equals("dataView")) {
						String provideBy = servicesSubElement.getAttributes().getNamedItem("provideBy").getTextContent();
						String viewName = servicesSubElement.getAttributes().getNamedItem("view").getTextContent();
						Boolean debug = Boolean.valueOf(servicesSubElement.getAttributes().getNamedItem("debug").getTextContent());
						if (provideBy.equals("DefaultDataViews")){
							DataView view = DefaultDataViews.getDataView(viewName);
							if (view == null){
								log.error(String.format("unexpected definition of view '{}", viewName));
							}
							else{
								dataViewDescriptions.add(view);
							}
						}
					}
				}
			}
		}



		MarketDataPipeline pipeline = 
				new AggregatingMarketDataPipeline(marketGateway, pipelineName, 
						cepConfig, persisterConfigs, aggregatorDefinitions,
						pipelineServiceDefinition);

		pipeline.setSingleThreadModel(singleThread);

		pipeline.addDataViews(dataViewDescriptions);
		
		for (FeedReference feed : aggregatorDefinitions.keySet())
		{
			pipeline.addFeedListener(feed);
		}
		
		return pipeline;
		
	}



	private Properties findSettings(DOMElementIterator domElementIterator)
	{
		while(domElementIterator.hasNext())
		{
			Element element = domElementIterator.next();
			String elementName = element.getNodeName();
			
			if (elementName.equals("settings"))
			{
				return XMLConfigParser.parseSettingsNode(element);
			}
		}
		return null;
	}



}
