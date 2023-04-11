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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.market.dataprovider.FeedReference;
import com.quantfabric.algo.market.provider.MarketDataPipelineImp;
import com.quantfabric.algo.market.dataprovider.PipelineHistoryService;
import com.quantfabric.algo.market.dataprovider.PipelineService;
import com.quantfabric.algo.market.dataprovider.PipelineServiceDefinition;
import com.quantfabric.algo.market.provider.aggregator.util.SubscriberAdapter;
import com.quantfabric.algo.market.provider.aggregator.util.SynchronousOutput;
import com.quantfabric.algo.market.gate.GatewayAgentsProvider;
import com.quantfabric.algo.market.gate.GatewayAgentsProviderDefinition;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.market.gateway.OrderBookSnapshotsProvider;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gate.access.product.producer.ComplexMarketViewProducer;
import com.quantfabric.algo.market.gate.access.product.producer.ComplexOHLCProducer;
import com.quantfabric.algo.market.gate.access.product.producer.FullBookProducer;
import com.quantfabric.algo.market.gate.access.product.producer.OHLCProducer;
import com.quantfabric.algo.market.gate.access.product.producer.Producer;
import com.quantfabric.algo.market.gate.access.product.producer.ProducerFactory;
import com.quantfabric.algo.market.gate.access.product.producer.TopOfBookProducer;
import com.quantfabric.algo.market.gate.access.product.producer.TradeOHLCProducer;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import com.quantfabric.algo.market.gate.jmx.InternalObjectNameUtil;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProvider;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDefinitionImpl;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderDictionary;
import com.quantfabric.algo.market.history.MultiTimeFrameHistoryProviderFactory;
import com.quantfabric.algo.market.history.manager.HistoryManagerImpl;
import com.quantfabric.algo.market.history.manager.jmx.JmxHistoryManager;
import com.quantfabric.algo.market.history.manager.jmx.JmxHistoryManagerException;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.messaging.SubscriberBuffer;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.ListenersGateway;
import com.quantfabric.util.PropertiesViewer;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;

public class AggregatingMarketDataPipeline extends MarketDataPipelineImp
{
	protected final String DEFAULT_HISTORY_STORAGE_PATH = "/store/ohlc_hist";
	
	private final AggregatorManager aggregatorManager;
	
	private final Map<FeedReference, MultiTimeFrameHistoryProvider> multiTimeFrameHistoryProviders =
			new HashMap<FeedReference, MultiTimeFrameHistoryProvider>();
	
	private final Map<FeedReference, JmxHistoryManager> jmxHistoryManagers =
			new HashMap<FeedReference, JmxHistoryManager>();
	
	private final Map<String, GatewayAgentsProvider> gatewayAgentsProviders =
			new HashMap<String, GatewayAgentsProvider>();
	
	private final Map<MarketViewAggregator, MarketViewAggregatorListener> aggregatorListeners =
		new HashMap<MarketViewAggregator, MarketViewAggregatorListener>();
	
	private final Map<String, SynchronousOutput> synchronousOutputs = new HashMap<String, SynchronousOutput>();
	
	private final Map<FeedReference, Collection<AggregatorDefinition>> aggregatorDefinitions;
	
	private final Collection<PipelineServiceDefinition> pipelineServiceDefinitions;
	
	public AggregatingMarketDataPipeline(MarketGateway marketGateway,
			String pipelineName, Configuration cepConfig,
			Collection<PersistingUpdateListenerConfig> persisterConfigs,
			Map<FeedReference, Collection<AggregatorDefinition>> aggregatorDefinitions, 
			Collection<PipelineServiceDefinition> pipelineServiceDefinitions)
	{
		super(marketGateway, pipelineName, cepConfig, persisterConfigs);	
		this.aggregatorDefinitions = aggregatorDefinitions;
		this.pipelineServiceDefinitions = pipelineServiceDefinitions;
		
		MultiTimeFrameHistoryProviderDictionary  multiTimeFrameHistoryProviderDictionary = null;
		
		if (!pipelineServiceDefinitions.isEmpty())
			multiTimeFrameHistoryProviderDictionary = new MultiTimeFrameHistoryProviderDictionary() 
			{				
				@Override
				public MultiTimeFrameHistoryProvider getMultiTimeFrameHistoryProvider(
						FeedReference feedReference)
				{
					return multiTimeFrameHistoryProviders.get(feedReference);
				}
			};
		
		this.aggregatorManager = new AggregatorManager(multiTimeFrameHistoryProviderDictionary);
			
	}
	
	private void loadGatewayAgentsProviders() throws GatewayAgentException, RemoteGatewayException {
		
		for (PipelineServiceDefinition pipelineServiceDefinition : pipelineServiceDefinitions) {
			if (pipelineServiceDefinition instanceof GatewayAgentsProviderDefinition) { 
				GatewayAgentsProvider agentsProvider = new GatewayAgentsProvider((GatewayAgentsProviderDefinition) pipelineServiceDefinition);
				String name = pipelineServiceDefinition.getName();
				gatewayAgentsProviders.put(name, agentsProvider);
				
				registerPipelineService(name, agentsProvider);
				agentsProvider.start();
			}
		}
	}

	private void loadHistoryProviders() throws InstantiationException, IllegalAccessException
	{
		MultiTimeFrameHistoryProviderDefinitionImpl multiTimeFrameHistoryProviderDefinition = 
				getMultiTimeFrameHistoryProviderDefinition();
		
		if (multiTimeFrameHistoryProviderDefinition != null)
		{
			MultiTimeFrameHistoryProviderFactory multiTimeFrameHistoryProviderFactory = 
					multiTimeFrameHistoryProviderDefinition.getFactoryClass().newInstance();
			
			for (FeedReference feedReference : getListeningFeeds())
				try
				{
					multiTimeFrameHistoryProviders.put(feedReference, 
							createTimeFrameHistoryProvider(
									multiTimeFrameHistoryProviderFactory, 
									multiTimeFrameHistoryProviderDefinition,
									feedReference));
					
					JmxHistoryManager historyManager = new JmxHistoryManager(feedReference.getFeedName().getName(), 
							new HistoryManagerImpl(multiTimeFrameHistoryProviders.get(feedReference)));
					
					historyManager.registerJmxBeans(
							InternalObjectNameUtil.toObjectNameMarketDataPipelineAsString("Xcellerate", this) + 
							",pipeline_group=History Management");
					
					jmxHistoryManagers.put(feedReference, historyManager);
					
					PipelineHistoryService pipelineHistoryService = 
							new PipleineHistoryServiceAdapter(this, multiTimeFrameHistoryProviders.get(feedReference), 
									multiTimeFrameHistoryProviderDefinition);
					
					registerPipelineService(multiTimeFrameHistoryProviderDefinition.getName() + 
								"-" + feedReference.getFeedName().getName(), 
								pipelineHistoryService);
				}
				catch (Exception e) 
				{
					getLogger().error("Can't create MultiTimeFrameHistoryProvider for feed - " + feedReference, e);
				}	
		}
	}
	
	private MultiTimeFrameHistoryProvider createTimeFrameHistoryProvider(
			MultiTimeFrameHistoryProviderFactory multiTimeFrameHistoryProviderFactory,
			MultiTimeFrameHistoryProviderDefinitionImpl multiTimeFrameHistoryProviderDefinition,
			FeedReference feedReference) throws Exception
	{
		return multiTimeFrameHistoryProviderFactory.create(multiTimeFrameHistoryProviderDefinition, 
				feedReference.getFeedName().getName());
	}

	private MultiTimeFrameHistoryProviderDefinitionImpl getMultiTimeFrameHistoryProviderDefinition()
	{
		for (PipelineServiceDefinition pipelineServiceDefinition : pipelineServiceDefinitions)
		{
			if (pipelineServiceDefinition instanceof MultiTimeFrameHistoryProviderDefinitionImpl)
				return (MultiTimeFrameHistoryProviderDefinitionImpl)pipelineServiceDefinition;
		}
		return null;
	}

	private void shutdownHistoryProviders()
	{
		for(PipelineService pipelineService : multiTimeFrameHistoryProviders.values())
			unregisterPipelineService(pipelineService.getDefinition().getName());
		
		for(JmxHistoryManager historyManager : jmxHistoryManagers.values())
			try
			{
				historyManager.unregisterJmxBean();
			}
			catch (JmxHistoryManagerException e)
			{
				getLogger().error("Can't unregister JmxHistoryManager", e);
			}
		
		multiTimeFrameHistoryProviders.clear();
		jmxHistoryManagers.clear();
	}
	
	private void shutdownGatewayAgentsProviders() {
		
		for(PipelineService pipelineService : gatewayAgentsProviders.values())
			unregisterPipelineService(pipelineService.getDefinition().getName());
		
		gatewayAgentsProviders.clear();
	}

	@Override
	public void start() 
	{				
		try
		{
			loadHistoryProviders();
		}
		catch (Exception e)
		{
			getLogger().error("can't load history provider", e);	
		}
		
		try {
			
			loadGatewayAgentsProviders();
		}
		catch (Exception e) {
			
			getLogger().error("can't load gateway agents provider", e);	
		}
		
		aggregatorListeners.clear();
		
		for (FeedReference feed : getListeningFeeds())
		{
			if (feed.isEnable())
			{			
				Collection<MarketViewAggregator> aggregators = null;
				
				try
				{
					aggregators = aggregatorManager.createAggregators(feed, getAggreagatorsConfig(feed));
				}
				catch (AggregatorManager.AggregatorManagerException e)
				{
					getLogger().error("can't create aggreagators for feed (" + feed + ")", e);					
				}
				
				if (aggregators != null)
					for (MarketViewAggregator aggregator : aggregators)
					{
						try
						{
							aggregatorListeners.put(aggregator, getAggregatorListener(aggregator, feed));
							aggregator.subscribe(aggregatorListeners.get(aggregator));
							
						}
						catch (Exception e)
						{
							getLogger().error(String.format(
									"Can't subscribe to aggregator (aggregatorName=%s, feedName=%s)", 
									aggregator.getName(), feed.getFeedName().getName()), e);
						}
					
						OrderBookSnapshotsProvider orderBookSnapshotsProvider =
							getOrderBookSnapshotsProvider(feed.getConnectionName());
						
						if (orderBookSnapshotsProvider != null)
						{	
							orderBookSnapshotsProvider.addOrderBookSnapshotListener(
									feed.getFeedName(), aggregator);
							
							feed.setConnected(true);
						}
						else
						{
							getLogger().error("Undefined market connection \"" +					
									feed.getConnectionName() + "\".");
						}
					}
			}
		}
		
		super.start();
	}

	private MarketViewAggregatorListener getAggregatorListener(
			MarketViewAggregator aggregator, FeedReference feed)
	{		
		Map<String, String> varDictionary = new HashMap<String, String>();
		varDictionary.put("%FEED_NAME%", feed.getFeedName().getName());
		varDictionary.put("%FEED_CONNECTION_NAME%", feed.getConnectionName());
		
		ListenersGateway<MarketViewAggregatorListener> gateway = new ListenersGateway<MarketViewAggregatorListener>();
				
		String synchronousOutput = aggregator.getProperties().getProperty("synchronousOutput"); 
		if (synchronousOutput == null)
			gateway.attachListener(new SubscriberAdapter(new SubscriberBuffer<Object>(aggregator.getName(), this)));
		else
		{			
			synchronousOutput = 
					QuantfabricRuntime.sabstituteVariableValues(aggregator.getProperties().getProperty("synchronousOutput"), varDictionary);
			
			if (!synchronousOutputs.containsKey(synchronousOutput))
				synchronousOutputs.put(synchronousOutput, 
						new SynchronousOutput(new SubscriberBuffer<Object>(synchronousOutput, this)));
			
			synchronousOutputs.get(synchronousOutput).assignSource(aggregator);
			
			getLogger().info("Synchronous output assign : " + synchronousOutput + " <-- " + aggregator.getName());
			
			gateway.attachListener(synchronousOutputs.get(synchronousOutput));
		}
		
		final boolean isProductProducer = aggregator.getProperties().containsKey("isProductProducer") && Boolean.parseBoolean(aggregator.getProperties().getProperty("isProductProducer").toLowerCase());

				
		if(isProductProducer)
		{
			String pubManagerName = null;
			String productCode = null;
			Set<ContentType> contentTypes = new HashSet<ContentType>();
			
			try
			{
				pubManagerName = QuantfabricRuntime.sabstituteVariableValues(
						PropertiesViewer.getProperty(aggregator.getProperties(), "publishersManager"), varDictionary);					
				productCode = QuantfabricRuntime.sabstituteVariableValues(
						PropertiesViewer.getProperty(aggregator.getProperties(), "productCode"), varDictionary);	
				String[] strContentTypes = PropertiesViewer.getMultiProperty(aggregator.getProperties(), "contentTypes", ',');
				
				for (String strContentType : strContentTypes)
				{
					ContentType contentType;
					
					try
					{
						contentType = ContentType.valueOf(strContentType.toUpperCase());
					}
					catch(IllegalArgumentException e)
					{
						getLogger().error("Wrong content type value in product producer configuration." , e);
						break;
					}					
					
					contentTypes.add(contentType);	
				}
			}
			catch(NotSpecifiedProperty e)
			{
				getLogger().error("Wrong product producer configuration." , e);
			}			
			
			ProducerFactory producerFactory = new ProducerFactory() {
				
				@Override
				public Producer createProducer(String productCode, ContentType contentType,
						Class<? extends MarketViewAggregator> aggregatorType)
				{
					switch (contentType)
					{
						case TOP_OF_BOOK:
							if (aggregatorType == TopMDQuoteAggregator.class)								
								return new TopOfBookProducer(productCode);						
						case COMPLEX_MARKET_VIEW:
							if (aggregatorType == ComplexMarketViewAggregator.class)
								return new ComplexMarketViewProducer(productCode);							
						case FULL_BOOK:
							if (aggregatorType == OrderBookAggregator.class)
								return new FullBookProducer(productCode);
						case OHLC:
							if (aggregatorType == OHLCAggregator.class)
								return new OHLCProducer(productCode);
						case TRADE_OHLC:
							if (aggregatorType == TradeOHLCAggregator.class)
								return new TradeOHLCProducer(productCode);
						case COMPLEX_OHLC:
							if (aggregatorType == ComplexOHLCAggregator.class)
								return new ComplexOHLCProducer(productCode);
						default:
							break;							
					}
					
					throw new IllegalArgumentException(String.format(
							"Can't determine producer for these product content type (%s) and aggregator type (%s)",
							contentType, aggregatorType));
				}
			};
			
			AggregationProductMaker productMaker = new AggregationProductMaker(
					getPublishersManager(pubManagerName), 
					productCode, 
					contentTypes.toArray(new ContentType[]{}), 
					aggregator.getClass(), 
					producerFactory);
			
			getLogger().info(String.format("Created ProductMaker (productCode=%s, contentTypes=%s)",
					productCode, Arrays.toString(contentTypes.toArray())));
			
			gateway.attachListener(productMaker);
		}
		
		return ListenersGateway.getGatewayProxy(gateway, MarketViewAggregatorListener.class);
	}

	private Collection<AggregatorDefinition> getAggreagatorsConfig(FeedReference feedReference)
	{		
		return aggregatorDefinitions.get(feedReference);
	}

	@Override
	public void stop()
	{
		for (FeedReference feed : getListeningFeeds())
		{
			if (feed.isEnable())
			{
				Collection<MarketViewAggregator> aggregators = aggregatorManager.getAggregators(feed);
				
				for (MarketViewAggregator aggregator : aggregators)
				{
					MarketViewAggregatorListener subscriberBuffer = aggregatorListeners.remove(aggregator);
					
					aggregator.unSubscribe(subscriberBuffer);
									
					//subscriberBuffer.dispose();
					
					OrderBookSnapshotsProvider orderBookSnapshotsProvider =
						getOrderBookSnapshotsProvider(feed.getConnectionName());
					
					if (orderBookSnapshotsProvider != null)	
					{
						orderBookSnapshotsProvider.
							removeOrderBookSnapshotListener(feed.getFeedName(), aggregator.getName());
					}
					//set disconnected anyway
					feed.setConnected(false);
				}
			}
		}
		
		for (SynchronousOutput syncOutput : synchronousOutputs.values())
			syncOutput.unassignAllSources();
		
		synchronousOutputs.clear();
		
		aggregatorManager.deleteAggregators();
		
		shutdownHistoryProviders();
		
		shutdownGatewayAgentsProviders();
		
		super.stop();
	}
}
