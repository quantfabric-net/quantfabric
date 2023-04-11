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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.quantfabric.algo.market.gate.access.MarketDataServiceHost;
import com.quantfabric.algo.market.gate.jmx.MGatewayJMXProvider;
import com.quantfabric.algo.market.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.dataprovider.FeedHandler;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.dataprovider.PipelineService;
import com.quantfabric.algo.market.dataprovider.QueryDataViewRequest;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublishersManager;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeedProvider;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeedProvider;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.runtime.QuantfabricRuntimeService;
import com.quantfabric.messaging.NamedMapSubscriber;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.Configurable;

import static com.quantfabric.algo.configuration.QuantfabricConstants.CONFIG_URL;


public class MarketGatewayService extends QuantfabricRuntimeService implements MarketGateway,
		MarketGatewayManager, MarketConnectionFactory, MarketDataPipelineFactory, Configurable
{
	private MGatewayJMXProvider jmxProvider;
	
	@Override
	public void setRuntime(QuantfabricRuntime runtime)
	{
		super.setRuntime(runtime);
		jmxProvider = new MGatewayJMXProvider(this);
		jmxProvider.start();
	}

	@Override
	public synchronized void start()
	{
		super.start();			
		startAllPiplines();
		startAllMDServiceHost();
		startAllFeedHandlers();
	}

	@Override
	public synchronized void stop()
	{
		super.stop();
		disconnectAll();
		stopAllPiplines();
		stopAllFeedHandlers();
	}
	
	private static final String DEFAULT_PIPPELINE = "default";
	private static final String DEFAULT_PIPPELINE_CLASS = "com.quantfabric.algo.market.dataprovider.MarketDataPipelineImp";
	
	private static final Logger log = LoggerFactory.getLogger(MarketGatewayService.class);
	private final Map<String, Instrument> instruments = new HashMap<String, Instrument>();
	private final Map<String, MarketConnection> connections = new HashMap<String, MarketConnection>();
	private final Map<String, MarketConnectionSettings> connectionsSettings = new HashMap<String, MarketConnectionSettings>();
	private final Map<String, MarketDataPipeline> pipelines = new HashMap<String, MarketDataPipeline>();
	
	private final Map<String, MarketDataServiceHost> marketDataServiceHosts = new HashMap<String, MarketDataServiceHost>();
	private final Map<String, PublishersManager> publisherManagers = new HashMap<String, PublishersManager>();
	
    private Configuration cepConfig = null;
	private final Map<String, PersistingUpdateListenerConfig> cepPersisterConfigs =
		new ConcurrentHashMap<String, PersistingUpdateListenerConfig>();
	    	
	private final List<FeedHandler> feedHandlers = new ArrayList<FeedHandler>();
	
	public void addFeedHandler(FeedHandler feedHandler) {
		feedHandlers.add(feedHandler);
	}
	
	public void removeFeedHandler(FeedHandler feedHandler) {
		feedHandlers.remove(feedHandler);
	}
	
	public void addMarketDataServiceHost(MarketDataServiceHost serviceHost)
	{
		marketDataServiceHosts.put(serviceHost.getServiceHostName(), serviceHost);
	}	
	
	private void startAllFeedHandlers() {
		
		for (FeedHandler feedHandler : feedHandlers) {
			try {
				feedHandler.start();
			}
			catch (Exception e) {
				log.error("start MarketDataServiceHost(" + feedHandler.getName() + ") failed.", e);
				
				e.printStackTrace();
			}
		}
	}
	
	private void startAllMDServiceHost()
	{
		for (MarketDataServiceHost host : marketDataServiceHosts.values())
			try
			{				
				host.start();
			}
			catch (Exception e)
			{
				log.error("Starting of MarketDataServiceHost({}) failed. {}",host.getServiceHostName(), e);
				e.printStackTrace();
			}
		
	}
	
	public void addPublishersManager(String name, PublishersManager pubManager)
	{
		publisherManagers.put(name, pubManager);
	}
	
	@Override
	public PublishersManager getPublishersManager(String pubManagerName)
	{
		return publisherManagers.get(pubManagerName);
	}
	
	public Configuration getCepConfig() {
		return cepConfig;
	}

	void setCepConfig(Configuration cepConfig) {
		this.cepConfig = cepConfig;
	}

	public void addDefaultCepPersisterConfig(PersistingUpdateListenerConfig config)
	{
		cepPersisterConfigs.put(config.getName(), config);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<PersistingUpdateListenerConfig> getDefaultCepPersisterConfigs()
	{
		if (cepPersisterConfigs.isEmpty())
		{
			if (getRuntime() != null)
			{
				Object propertValue = getRuntime().getProperty("esperEventPersisterConfigs");		
				if (propertValue != null && propertValue instanceof Collection<?>)
					return (Collection<PersistingUpdateListenerConfig>)propertValue;
			}
			else
				return null;
		}
		return cepPersisterConfigs.values();		
	}
	
	public MarketGatewayService() 
	{
		super();
	}
	
	@Override
	public OrderBookSnapshotsProvider getOrderBookSnapshotsProvider(String name)
	{
		return getConnection(name);
	}
	
	@Override
	public MarketFeeder getMarketFeeder(String name)
	{
		return getConnection(name);
	}
	
	@Override
	public CommandExecutor getMarketCommandExecutor(String name)
	{
		return getConnection(name);
	}	
	
	@Override
	public LoanCancelProvider getLoanCancelProvider(String name)
	{
		return getConnection(name);
	}

	
	@Override
	public MarketDataFeedProvider getMarketDataFeedProvider(String name)
	{
		return getConnection(name);
	}

	@Override
	public ExecutionFeedProvider getExecutionFeedProvider(String name)
	{
		return getConnection(name);
	}

	@Override
	public MarketConnection getConnection(String name)
	{
		synchronized (connections)
		{
			if (connections.containsKey(name)) { return connections.get(name); }
		}
		return null;
	}
	
	@Override
	public void connectToAll(boolean onlyAutoConnect)
	{
		synchronized (connections)
		{
			for (MarketConnection connection : connections.values())
			{
				try
				{
					if (!onlyAutoConnect || isConnectionAutoStart(connection.getName()))
						if (!connection.isConnected())
						{
							log.info("connect to MarketConnection : {}", connection.getName());
							connection.connect();
						}
						else
						{
							log.info("already connected to MarketConnection : {}", connection.getName());
						}
				}
				catch (MarketConnectionException e)
				{
					log.error("Connect to \"{}\" was failed, {}", connection.getIdentifier() , e);
				}
			}
		}
	}

	@Override
	public void disconnectAll()
	{
		synchronized (connections)
		{
			for (MarketConnection connection : connections.values())
			{
				try
				{
					connection.disconnect();
				}
				catch (MarketConnectionException e)
				{
					log.error("Disconnect from \"{}\" was failed",connection.getIdentifier(), e);
				}
			}
		}
	}
	
	@Override
	public Publisher<NamedMapSubscriber<Object>, QueryDataViewRequest, Object> getMDPipelinePublisher(
			String name)
	{
		return getMDPipeline(name);
	}
	
	@Override
	public PipelineService getPipelineService(String pipelineName,
			String serviceName)
	{
		return getMDPipeline(pipelineName).getPipelineService(serviceName);
	}

	@Override
    public MarketDataPipeline getMDPipeline(String name) {
		synchronized (pipelines)
		{
			if (pipelines.containsKey(name))
				return pipelines.get(name);
			return null;
		}
	}
	
	@Override
	public MarketDataPipeline createMDPipeline(String name,
			String pipelineClassName,boolean singleThread)
	{
		MarketDataPipeline pipeline = null;

		synchronized (pipelines)
		{
			if (pipelines.containsKey(name)) {
				pipeline=pipelines.get(name);
				pipeline.setSingleThreadModel(singleThread);
				return pipeline;
			}
			
			try
			{
				Class<?> pipelineClass = Class.forName(pipelineClassName);
			
				pipeline = (MarketDataPipeline)pipelineClass.
					getConstructor(
							MarketGateway.class, 
							String.class, 
							Configuration.class, 
							Collection.class).
						newInstance(this, name, getCepConfig(), getDefaultCepPersisterConfigs());
				
				pipeline.setSingleThreadModel(singleThread);
				pipelines.put(name, pipeline);
			}
			catch (Exception e)
			{
				log.error(String.format("Can't create pipeline (class:%s)", pipelineClassName), e);
				return null;
			}
		}
		return pipeline;
	}

	@Override
	public MarketDataPipeline createMDPipeline(String name,boolean singleThread)
	{
		return createMDPipeline(name, DEFAULT_PIPPELINE_CLASS, singleThread);
	}
	
	@Override
	public void addMDPipeline(MarketDataPipeline pipeline)
	{
		synchronized (pipelines)
		{
			pipelines.put(pipeline.getName(), pipeline);
		}
	}

	@Override
	public void removeMDPipeline(String name)
	{
		synchronized (pipelines)
		{
			pipelines.remove(name);
		}
	}

	public void startAllPiplines()
	{
		for (MarketDataPipeline pipeline : pipelines.values())
			try {
				pipeline.start();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
	}
	
	public void stopAllPiplines()
	{
		for (MarketDataPipeline pipeline : pipelines.values())
			try {
				pipeline.stop();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
	}
	
	public void stopAllFeedHandlers() {
		for (FeedHandler feedHandler : feedHandlers) {
			try {
				feedHandler.stop();
			}
			catch (Exception e) {
				log.error("Stopping of MarketDataServiceHost({}) failed. {}",feedHandler.getName() ,e);
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void configure(Properties properties) throws Exception
	{
		String configFile = properties.getProperty(CONFIG_URL);
		
		configFile = QuantfabricRuntime.getAbsolutePath(configFile);
		
		if (log.isDebugEnabled())
		{
			log.debug("configuring from file: {}", configFile);
		}
		try
		{
			ConfigProvider.doConfigure(this, new FileInputStream(configFile), configFile);
			
			createMDPipeline(DEFAULT_PIPPELINE, DEFAULT_PIPPELINE_CLASS,false);

		}
		catch (Exception e)
		{
			log.error(e.getMessage());
		}
	}

	@Override
	public void addInstrument(Instrument instrument)
	{
		instruments.put(instrument.getId(), instrument);
	}

	@Override
	public MarketConnection createConnection(String name,
			MarketConnectionSettings settings) throws MarketGatewayException
	{
		MarketConnectionImp connection = (MarketConnectionImp) createConnection(name, settings.getProvider(),
				settings.getSettings(), settings.getCredentials(), settings.isAutoConnect());
		
		connectionsSettings.put(name, settings);
		
		if (settings.getMarketDataStorageProviderClassName() != null)
		{
			try
			{
				MarketDataCacheProvider storageProvider =
					(MarketDataCacheProvider)Class.forName(settings.getMarketDataStorageProviderClassName()).
						getConstructor(MarketConnectionImp.class, Properties.class).
							newInstance(connection, settings.getMarketDataStorageProviderSettings());
			
				connection.setMarketDataStorageProvider(storageProvider);
			}
			catch (Exception e)
			{
				throw new MarketGatewayException(e);
			}			
		}

		
		try
		{
			connection.setMode(settings.getMode());
		}
		catch (MarketConnectionException e)
		{
			throw new MarketGatewayException(e);
		}
		
		return connection;
	}

	@Override
	public MarketConnection createConnection(String name, String provider,
			Properties settings, Properties credentials, boolean autoConnect) throws MarketGatewayException
	{
		synchronized (connections)
		{
			if (!connections.containsKey(name))
			{
				try
				{
					MarketConnection marketConnection =
						(MarketConnection) Class.forName(provider).
							getConstructor(MarketGateway.class, 
									String.class, Properties.class, Properties.class).
								newInstance(this, name, settings, credentials);
					
					connections.put(name, marketConnection);
					
					return marketConnection;
				}
				catch (Exception e)
				{
					throw new MarketGatewayException(e);
				}
			}
		}
		return null;
	}
	
	@Override
	public void addConnection(MarketConnection connection)
	{
		synchronized (connections)
		{
			connections.put(connection.getName(), connection);
		}
	}

	@Override
	public void removeConnection(String name)
	{
		synchronized (connections)
		{
			connections.remove(name);
		}

	}

	@Override
	public Collection<MarketConnection> getConnections()
	{
		return connections.values();
	}

	@Override
	public Collection<MarketDataPipeline> getMDPipelines()
	{
		return pipelines.values();
	}

	@Override
	public Collection<Instrument> getInstruments()
	{
		return instruments.values();
	}

	@Override
	public Instrument getInstrument(String Id)
	{
		return instruments.get(Id);
	}
		
	@Override
	public Instrument getInstrumentBySymbol(String symbol)
	{
		for (Instrument instrument : getInstruments())
			if (instrument.getSymbol().equals(symbol)
					|| (instrument.getBase().concat(instrument.getLocal())).equals(symbol))
				return instrument;
		
		return null;
	}

	@Override
	public boolean isConnectionAutoStart(String name)
	{
		if (connectionsSettings.containsKey(name))
			return connectionsSettings.get(name).isAutoConnect();
		
		return MarketConnectionSettings.DEFAULT_AUTO_CONNECT;
	}
}
