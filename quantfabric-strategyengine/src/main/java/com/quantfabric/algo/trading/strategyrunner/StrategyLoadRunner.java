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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.backtesting.eventbus.BackTestingEventAdapter;
import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.dataprovider.GatewayAgentsService;
import com.quantfabric.algo.market.dataprovider.HistoricalDataViewRequest;
import com.quantfabric.algo.market.dataprovider.PipelineHistoryService;
import com.quantfabric.algo.market.dataprovider.PipelineService;
import com.quantfabric.algo.market.dataprovider.ProductRequest;
import com.quantfabric.algo.market.dataprovider.QueryDataViewRequest;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.agent.Agent;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.GatewayAgentException;
import com.quantfabric.algo.market.gateway.access.agent.exceptions.RemoteGatewayException;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.runtime.QuantfabricRuntimeService;
import com.quantfabric.algo.trading.execution.ExecutionProvider;
import com.quantfabric.algo.trading.execution.ExecutionProviderImpl;
import com.quantfabric.algo.trading.execution.StrategyEventsStreamer;
import com.quantfabric.algo.trading.strategy.DataSink;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategyrunner.jmx.TradingJMXProvider;
import com.quantfabric.cep.CEPProvider;
import com.quantfabric.cep.ICEPProvider;
import com.quantfabric.messaging.NamedMapSubscriber;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.messaging.SubscriberBuffer;
import com.quantfabric.net.Transmitter;
import com.quantfabric.net.stream.ZMQTransmitter;
import com.quantfabric.net.stream.kryo.KryoStreamServer;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.Configurable;


import static com.quantfabric.algo.configuration.QuantfabricConstants.CONFIG_URL;

public class StrategyLoadRunner extends QuantfabricRuntimeService implements
		StrategyRunner, Configurable
{
	private static final Logger log = LoggerFactory.getLogger(StrategyLoadRunner.class);
	
	private final Map<String, TradingStrategy> strategies = 
		new ConcurrentHashMap<String, TradingStrategy>();
	private final Map<String, Boolean> autoRunForStrategies =
		new ConcurrentHashMap<String, Boolean>();
	private final Map<String, Collection<ConnectionStatusChangedRule>> connectionStatusChangedRulesForStrategies =
		new ConcurrentHashMap<String, Collection<ConnectionStatusChangedRule>>();
	
	private final Map<String, Integer> portsForStrategiesData =
		new ConcurrentHashMap<String, Integer>();

	private Map<String, String> globalSettings;
	private final Map<String, PersistingUpdateListenerConfig> cepPersisterConfigs =
		new ConcurrentHashMap<String, PersistingUpdateListenerConfig>();
	private final Map<String, Set<String>> persistersForStrategy =
		new HashMap<String, Set<String>>();
	private final Map<DataSink, Set<String>> sinkContext =
		new HashMap<DataSink, Set<String>>();
	
	private final Map<String, StrategyLoadContext> strategyLoadContexts =
			new HashMap<String, StrategyLoadContext>();
	
	private TradingJMXProvider jmxProvider;
	
	private MarketGateway marketGateway;
	
	private String lastConfigFile = null;
	
	private final Map<String, Transmitter> createdTransmitters = new HashMap<String, Transmitter>();
	
	public StrategyLoadRunner()
	{
		super();
		
		QuantfabricRuntime.getGlobalBackTestingEventBus().attachListener(new BackTestingEventAdapter() {
			
			@Override
			public void reloadExecution(BackTestingEvent event)
			{
				reload();
			}
		});
	}

	public void setStrategyLoadContext(String strategyName, StrategyLoadContext context)
	{
		strategyLoadContexts.put(strategyName, context);
	}
	
	public StrategyLoadContext getStrategyLoadContext(String strategyName)
	{
		return strategyLoadContexts.get(strategyName);
	}
	
	public void addPersisterNameForStrategy(String strategyName, String persisterName)
	{
		if (!persistersForStrategy.containsKey(strategyName))
			persistersForStrategy.put(strategyName, new HashSet<String>());
		
		persistersForStrategy.get(strategyName).add(persisterName);
	}
	
	public Map<String, TradingStrategy> getStrategies()
	{
		return Collections.unmodifiableMap(strategies);
	}
	
	public TradingStrategy getStrategy(String strategyName)
	{
		return strategies.get(strategyName);
	}
	
	public Map<String, String> getGlobalSettings()
	{
		return globalSettings;
	}

	public void setGlobalSettings(Map<String, String> globalSettings)
	{
		this.globalSettings = globalSettings;
	}

	@Override
	public void configure(Properties properties) throws Exception
	{
		String configFile = properties.getProperty(CONFIG_URL);
		configFile = QuantfabricRuntime.getAbsolutePath(configFile);

		configureFromFile(configFile);
	}
	
	private void configureFromFile(String configFile)
	{
		if (log.isDebugEnabled())
		{
			log.debug("configuring from file: " + configFile); 
		}
		try
		{
			ConfigProvider.doConfigure(this, new FileInputStream(configFile),
					configFile);
			lastConfigFile = configFile;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void loadStrategy(TradingStrategy strategy, boolean enable)
	{
		strategy.setEnabled(enable);
		strategies.put(strategy.getName(), strategy);
	}
	
	public void unloadStrategy(TradingStrategy strategy) throws Exception {
		
		String strategyName = strategy.getName();
		
		if (strategies.get(strategyName) == null)
			throw new Exception("Strategy doesn't exist (" + strategy.getName() + ")");
		else {
			if (strategy.isRunning()) 
				strategy.stop();
			
			for (DataSink sink : strategy.getDataSinks()) {
				if (sink.isActive())
					deActivateSink(strategy, sink);
			}
			
			strategy.destroyCep();
			portsForStrategiesData.remove(strategyName);
			autoRunForStrategies.remove(strategyName);
			persistersForStrategy.remove(strategyName);
			createdTransmitters.get(strategyName).endOutput();
			createdTransmitters.remove(strategyName);
			globalSettings.remove(strategyName);
			strategies.remove(strategyName);
		}
	}

	public void setAutoRunForStrategy(String strategyName, boolean isAutoRun)
	{
		autoRunForStrategies.put(strategyName, isAutoRun);
	}
	
	public void setPortForStrategyData(String strategyName, int port)
	{
		portsForStrategiesData.put(strategyName, port);
	}

	@Override
	public void setRuntime(QuantfabricRuntime runtime)
	{
		super.setRuntime(runtime);
		jmxProvider = new TradingJMXProvider(this);
		jmxProvider.start();
	}

	public boolean strategyIsAutoRun(TradingStrategy strategy)
	{
		if (autoRunForStrategies.containsKey(strategy.getName()))
			return autoRunForStrategies.get(strategy.getName());
		else
			return DEFAULT_AUTO_RUN_STRATEGY;
	}

	@Override
	public synchronized void start()
	{

		for (TradingStrategy strategy : strategies.values())
		{
			try
			{			
				if (strategyIsAutoRun(strategy))
				{
					strategy.start();
				}
			}
			catch (Exception e)
			{
				log.error("can't auto run strategy", e);
			}
		}
		super.start();

	}

	@Override
	public synchronized void stop()
	{
		for (TradingStrategy strategy : strategies.values())
		{
			try
			{
				if (strategy.isRunning()) 
				{
					strategy.stop();
				}
			}
			catch (Exception e)
			{
				log.error("can't stop strategy", e);
			}
		}

		super.stop();
	}

	public void reload()
	{
		try
		{
			jmxProvider.stop();
			jmxProvider = null;
			
			stop();
			
			for (TradingStrategy strategy : strategies.values()) {
				this.unloadStrategy(strategy);
			}
			
			globalSettings.clear();
			cepPersisterConfigs.clear();
			
			configureFromFile(lastConfigFile);	
			
			Thread.sleep(1000);
			
			jmxProvider = new TradingJMXProvider(this);
			jmxProvider.start();
			
			start();
		}
		catch (Exception e) 
		{
			log.error("StrategyLoadRunner reload failed.", e);
		}
		
	}
	
	@Override
	public void activateSink(TradingStrategy strategy, DataSink sink)
			throws Exception
	{
		MarketGateway gateway = getMarketGateway();
		
		if (sink.getObservation() instanceof QueryDataViewRequest)
		{		
			Publisher<NamedMapSubscriber<Object>, QueryDataViewRequest, Object> pipeline =
					gateway.getMDPipelinePublisher(sink.getPipeline());
	
			sink.setSubscriberBuffer(new SubscriberBuffer<Object>(strategy
					.getName() + "-" + sink.getName(), strategy));
	
			pipeline.subscribe(sink.getSubscriberBuffer(), (QueryDataViewRequest)sink.getObservation());
			log.info(sink.getName() + " activated");
	
			sink.setActive(true);
		}
		else if (sink.getObservation() instanceof HistoricalDataViewRequest)
		{
			HistoricalDataViewRequest historicalDataViewRequest = (HistoricalDataViewRequest) sink.getObservation();
			
			PipelineService pipelineService =
					gateway.getPipelineService(sink.getPipeline(), 
							historicalDataViewRequest.getServiceReference() + "-" + historicalDataViewRequest.getFeedName());
			
			if (pipelineService == null)
				log.error(String.format("PipelineService (%s, %s) is absent on the Pipeline (%s)", 
						historicalDataViewRequest.getServiceReference(), 
						 historicalDataViewRequest.getFeedName(), sink.getPipeline()));
			else
			{			
				if (pipelineService instanceof PipelineHistoryService)
				{
					PipelineHistoryService historyProvider = 
							(PipelineHistoryService)pipelineService;
										
					strategy.sendUpdate(historyProvider.getHistoricalBars(historicalDataViewRequest));
				
					log.info(sink.getName() + " activated");				
					sink.setActive(true);
				}
			}
		}
		else 
			if (sink.getObservation() instanceof ProductRequest) {
			
			ProductRequest productRequest = (ProductRequest)sink.getObservation();
						
			GatewayAgentsService pipelineService = (GatewayAgentsService) gateway.getPipelineService(sink.getPipeline(),
					productRequest.getServiceName());

			Agent agent = pipelineService.getAgent(productRequest.getAgentName());
			
			sink.setSubscriberBuffer(new SubscriberBuffer<Object>(strategy.getName() + "-" + sink.getName(), strategy));
			
			ProductRequest.ProductSubscriber productSubscriber = new ProductRequest.ProductSubscriber(sink.getSubscriberBuffer());
			
			for (ContentType contentType : productRequest.getContentTypes()) {
				
				String subscriptionId = agent.subscribe(productRequest.getProductCode(), contentType, null,
						productSubscriber);
				if (!sinkContext.containsKey(sink)) 
					sinkContext.put(sink, new HashSet<String>());
				
				sinkContext.get(sink).add(subscriptionId);					
			}
			
			log.info(sink.getName() + " activated");	
			sink.setActive(true);
		}
	}

	@Override
	public void deActivateSink(TradingStrategy strategy, DataSink sink)
	{
		MarketGateway gateway = getMarketGateway();
		
		if (sink.getObservation() instanceof QueryDataViewRequest)
		{
			Publisher<NamedMapSubscriber<Object>, QueryDataViewRequest, Object> pipeline =
					gateway
					.getMDPipelinePublisher(sink.getPipeline());
	
			pipeline.unSubscribe(sink.getSubscriberBuffer(), (QueryDataViewRequest)sink.getObservation());
			sink.getSubscriberBuffer().dispose();
	
			log.info(sink.getName() + " deactivated");
	
			sink.setActive(false);
		}
		else if (sink.getObservation() instanceof HistoricalDataViewRequest)
		{
			log.info(sink.getName() + " deactivated");			
			sink.setActive(false);
		}
		else if (sink.getObservation() instanceof ProductRequest) {
			
			if (sinkContext.containsKey(sink)) {
				
				ProductRequest productRequest = (ProductRequest) sink.getObservation();
				GatewayAgentsService pipelineService = (GatewayAgentsService) gateway.getPipelineService(sink.getPipeline(), productRequest.getServiceName());
				Agent agent = pipelineService.getAgent(productRequest.getAgentName());	
				
				List<String> subscriptionIds = new LinkedList<String>(sinkContext.get(sink));
				for (String subscriptionId : subscriptionIds) {
					try {
						agent.unsubscribe(subscriptionId);
						sinkContext.get(sink).remove(subscriptionId);
					}
					catch (GatewayAgentException e) {
						log.error("Can't unsubscribe agent(" + agent.getName() + ")", e);
					}
					catch (RemoteGatewayException e) {
						log.error("Can't unsubscribe agent(" + agent.getName() + ")", e);
					}
				}
			}
			
			log.info(sink.getName() + " deactivated");
			sink.setActive(false);
		}
	}

	private void determineMarketGateway()
	{
		this.marketGateway = (MarketGateway) getRuntime().getService(
			"com.quantfabric.algo.market.gateway.MarketGatewayService");
	}	
	
	@Override
	public MarketGateway getMarketGateway()
	{
		if (marketGateway == null)
			determineMarketGateway();
		
		return marketGateway;
	}

	@Override
	public ExecutionProvider getExecutionProvider(TradingStrategy strategy)
	{	
		if (portsForStrategiesData.containsKey(strategy.getName()))
		{
			int port = portsForStrategiesData.get(strategy.getName());			
			
			strategy.setStrategyDataStreamPort(port);
			
			Transmitter transmitter = new ZMQTransmitter(port);
			
			createdTransmitters.put(strategy.getName(), transmitter);
			
			return new ExecutionProviderImpl(strategy, this, 
					new StrategyEventsStreamer(strategy.getName(), new KryoStreamServer(transmitter)));
		}
		else
			return new ExecutionProviderImpl(strategy, this);
	}

	@Override
	public ICEPProvider getCEPProvider(String strategyName, String uri, Configuration config)
	{
		return getCEPProvider(strategyName, uri, config, getDefaultCepPersisterConfigs(strategyName));
	}
	
	@Override
	public ICEPProvider getCEPProvider(String strategyName, String uri, Configuration config,
			Collection<PersistingUpdateListenerConfig> persisterConfigs)
	{
		return CEPProvider.getCEPProvider(uri, config, persisterConfigs);
	}

	public void addDefaultCepPersisterConfig(
			PersistingUpdateListenerConfig config)
	{
		cepPersisterConfigs.put(config.getName(), config);
	}

	public Collection<PersistingUpdateListenerConfig> getDefaultCepPersisterConfigs()
	{
		return getDefaultCepPersisterConfigs("unknown");
	}
	
	@SuppressWarnings("unchecked")
	public Collection<PersistingUpdateListenerConfig> getDefaultCepPersisterConfigs(String strategyName)
	{
		Collection<PersistingUpdateListenerConfig> allCepPersisterConfigs = null;
		
		if (cepPersisterConfigs.isEmpty())
		{
			Object propertValue = getRuntime().getProperty(
					"esperEventPersisterConfigs");
			if (propertValue != null && propertValue instanceof Collection<?>)
				allCepPersisterConfigs = (Collection<PersistingUpdateListenerConfig>) propertValue;
		}
		
		if (allCepPersisterConfigs == null)
			allCepPersisterConfigs = cepPersisterConfigs.values();
		
		List<PersistingUpdateListenerConfig> cepPersisterConfigsForStrat = new ArrayList<PersistingUpdateListenerConfig>();
		
		if (!persistersForStrategy.containsKey(strategyName))
			return allCepPersisterConfigs;
		else
		{
			Collection<String> persisterNamesForStrat = persistersForStrategy.get(strategyName);
			for (PersistingUpdateListenerConfig persisterConfig : allCepPersisterConfigs)
			{
				if (persisterNamesForStrat.contains(persisterConfig.getName()))
					cepPersisterConfigsForStrat.add(persisterConfig);
			}
		}
		
		return cepPersisterConfigsForStrat;
	}

	public void setConnectionStatusChangedRules(String strategyName, Collection<ConnectionStatusChangedRule> rules)
	{
		connectionStatusChangedRulesForStrategies.put(strategyName, rules);
	}
	
	@Override
	public void connectionStatusChanged(final TradingStrategy recepient,
			StatusChanged event)
	{		
		log.info(recepient.getName() + " : Connection status changed (" + event.getConnectionName()
				+ " - " + event.getMarketConnectionStatus() + ")");

		Collection<ConnectionStatusChangedRule> connectionStatusChangedRules = 
				connectionStatusChangedRulesForStrategies.get(recepient.getName());
		
		if (connectionStatusChangedRules != null)
		{			
			for (ConnectionStatusChangedRule rule : connectionStatusChangedRules)
			{		
				if ((rule.getConnectionName().equals(ConnectionStatusChangedRule.ANY) || 
					rule.getConnectionName().equals(event.getConnectionName())) && 
					
					(rule.getMarketConnectionMode() == event.getConnectionMode() &&
					 rule.getMarketConnectionStatus() == event.getMarketConnectionStatus()))
				{
					log.info(recepient.getName() + " : " + "Active rule - " + rule);
					
					switch (rule.getStrategyAction())
					{
						case STOP:
							if (recepient.isRunning())
								try
								{
									recepient.stop();
								}
								catch (Exception e)
								{
									log.error("Stop strategy (" + recepient.getName() + ") failed");
								}
							else
								log.info(recepient.getName() + " : " + "Is stopped already");
							break;
							
						case START:
							if (recepient.isEnabled() && !recepient.isRunning())
								try
								{
									recepient.start();
								}
								catch (Exception e)
								{
									log.error("Start strategy (" + recepient.getName() + ") failed");
								}
							else
								log.info(recepient.getName() + " : " + "Is running already");
							break;
							
						case DISABLE_EXECUTION:
						case ENABLE_EXECUTION:
							log.error("Unsupported strategy action" + rule.getStrategyAction());
							break;
							
						case NOTHING:
							log.info("Strategy action is NOTHING");
							break;
							
						default:
							log.error("Unsupported strategy action" + rule.getStrategyAction());
					}
					break;
				}
			}
		}
		else
			log.info(recepient.getName() + " : " + "No rules");
	}
}
