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
package com.quantfabric.algo.server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quickserver.net.server.QuickServer;
import com.sun.tools.attach.VirtualMachine;

import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.runtime.QuantfabricRuntime.ApplicationInfo;
import com.quantfabric.algo.server.configuration.AlgoHostConfig;
import com.quantfabric.algo.server.configuration.AlgoServerConfig;
import com.quantfabric.algo.server.configuration.Config;
import com.quantfabric.algo.server.jmx.JMXAlgoServerService;
import com.quantfabric.algo.server.process.ProcessLauncher;
import com.quantfabric.algo.server.process.ProcessManager;
import com.quantfabric.algo.server.qserver.CommandExecutor;
import com.quantfabric.algo.server.qserver.commands.ClientCommand;
import com.quantfabric.algo.server.qserver.commands.StrategyDataStreamingRequest;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting.ModificationMode;
import com.quantfabric.net.rpc.RpcServer;
import com.quantfabric.net.rpc.json.JsonRpcServerProvider;
import com.quantfabric.net.stream.ZMQReceiver;
import com.quantfabric.util.PropertiesViewer;
import com.quantfabric.util.SessionId;
import com.quantfabric.util.ShutdownHook;

public class AlgoServer implements HostManager, AlgoServerUnitProvider, CommandExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(AlgoServer.class);
	private static final String CONNECTOR_ADDRESS =
			"com.sun.management.jmxremote.localConnectorAddress";

	public static RpcServerFactory getRpcServerFactory()
	{
		return 
			new RpcServerFactory() 
			{			
				@Override
				public RpcServer create(Object service)
				{					
					return new JsonRpcServerProvider(service);
				}
			};
	}
	
	public static RpcServer getRpcServer()
	{
		return getRpcServerFactory().create(getInstance());
	}
	
	public static CommandExecutor getCommandExecutor()
	{
		return getInstance();
	}
	
	private static AlgoServer server = null;
	public static AlgoServer getInstance() 
	{
		if(null == server) 
		{
			synchronized(AlgoServer.class) 
			{
				QuantfabricRuntime.setAppInfo(
						new ApplicationInfo("Xcellerate Algo", "2.1", "Quantfabric, Inc."));

				server = new AlgoServer();
			}
		}
		return server;
	}
	
	private final HashMap<String, Properties> hostsConfiguration =
		new HashMap<>();
	
	private ProcessManager processManager = null;
	private QuickServer mgmtServer = null;	
	
	private final Map<String, TradingUnitProvider> unitProviders =
		new HashMap<>();
	
	private final String name;
	private final String description;
	
	private JMXAlgoServerService jmxAlgoServerService;
		
	private AlgoServer() 
	{		
		this.name = "AlgoServer";
		this.description = QuantfabricRuntime.getAppInfo().toString();
		
		//load configuration set
		Map<String, Properties> configSet = new HashMap<String, Properties>() 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put("default", 
					new Properties() 
					{
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;
	
						{
							put(ProcessLauncher.CLASS_NAME,
									"com.quantfabric.algo.server.AlgoHost");
						}
					});
			}
		};
		
		// init processManager
		processManager = new ProcessManager();
				
		for(Entry<String, Properties> configEntry : configSet.entrySet()) 
		{
			addHostConfiguration(configEntry.getKey(), configEntry.getValue());
		}
		
		// create mgmt connectivity
		mgmtServer = new QuickServer();
		//mgmtServer.initServer(new Object[] {"config\\Server.xml"});
		mgmtServer.setClientCommandHandler(com.quantfabric.algo.server.qserver.CommandHandler.class.getName());
		mgmtServer.setClientData(com.quantfabric.algo.server.qserver.ClientContext.class.getName());
		
		mgmtServer.setName("QuantfabricAlgoServer v.7.02.1334.5003");	
	}	
	
	public void startManagementServices(int port) throws Exception
	{
		mgmtServer.setPort(port); 
		
		for (TradingUnitProvider nodeProvider : unitProviders.values())
			nodeProvider.load();
		
		mgmtServer.startServer();
	}
	
	private void startJMXService(String algoServerJMXhost, CountDownLatch shutdownLatch, int algoServerJMXPort)
	{
		jmxAlgoServerService = new JMXAlgoServerService(this, shutdownLatch, algoServerJMXhost, algoServerJMXPort);
		
		jmxAlgoServerService.start();

	}
	
	public void addHostConfiguration(String name, Properties configuration)
	{
		processManager.addProcessBuilder(configuration.hashCode(), configuration);
		hostsConfiguration.put(name, configuration);
	}
	
	@Override
	public Set<String> getAvailableUnitConfigurations() 
	{
		return hostsConfiguration.keySet();
	}

	@Override
	public void createUnit(String unitName, String configuration) 
	{
		//get configuration set
		if(hostsConfiguration.containsKey(configuration)) 
		{
			int id = hostsConfiguration.get(configuration).hashCode();
			processManager.startProcess(unitName,id);	
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			
			long processId = processManager.getProcessId(unitName);	
			
			TradingUnitProvider nodeProvider = new JMXNodeProvider();

			try
			{
				final String unitProviderName = unitName;			
				final String unitProviderDescription = "PID:" + processId;

				VirtualMachine vm = VirtualMachine.attach(String.valueOf(processId));
				final String jmxServiceUrl = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

				final String strategiesObjectNameReference =				
					"Xcellerate:type=AlgoServer,group=services,serviceName=Trading,servicegroup=Strategies,strategyName=*" ;
				
				final String instrumentsObjectNameReference =
					"Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Instruments,instrumentName=*";
				
				final String marketConnectionsObjectNameReference =
					"Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=*";
				
				final String pipelinesObjectNameReference =
					"Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Pipelines,pipelineName=*";
				
				nodeProvider.configure(
					new Properties()
					{
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						{
							put("name", unitProviderName);
							put("description", unitProviderDescription);
							put("jmxServiceUrl", jmxServiceUrl);
							put("strategiesObjectNameReference", strategiesObjectNameReference);
							put("instrumentsObjectNameReference", instrumentsObjectNameReference);
							put("marketConnectionsObjectNameReference", marketConnectionsObjectNameReference);
							put("pipelinesObjectNameReference", pipelinesObjectNameReference);
						}
					});

				unitProviders.put(unitName, nodeProvider);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Unit creation failed. UnitName=" + unitName, e);
			}
		}		
	}

	@Override
	public void removeUnit(String name) 
	{
		try
		{
			shutdownUnit(name);
		}
		catch (Exception e)
		{
			processManager.killProcess(name);
		}
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		mgmtServer.stopServer();
		for (String host : getUnits())
			removeUnit(host);
		
		processManager.finalize();
		
		super.finalize();
	}
	
	@Override
	public String getName()
	{		
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public Set<String> getUnits()
	{
		return unitProviders.keySet();
	}
	
	@Override
	public NodeProvider getUnit(String unitName)
	{
		return (NodeProvider) unitProviders.get(unitName);		
	}

	public void shutdownUnit(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).shutdown();
	}

	@Override
	public String getUnitDescription(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getDescription();
		
		return null;
	}

	@Override
	public Set<String> getStrategies(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategies();
		
		return Collections.emptySet();
	}

	@Override
	public Map<String, String> getStrategyInfo(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyInfo(strategy);
		
		return Collections.emptyMap();
	}

	@Override
	public Properties getStrategyState(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyState(strategy);
		
		return null;
	}
	
	@Override
	public String getStrategySettingsLayoutDefinition(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategySettingsLayoutDefinition(strategy);
		
		return null;
	}

	@Override
	public Properties getStrategyProperties(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyProperties(strategy);
		
		return null;
	}
	
	@Override
	public void setStrategyProperties(String unitName, String strategy,
			Properties properties)
	{
		for (Object propertyKey : properties.keySet())
		{
			String propertyName = propertyKey.toString();
			setStrategyPropertyValue(unitName, strategy, propertyName, properties.getProperty(propertyName));
		}		
	}

	@Override
	public Set<String> getStrategyPropetiesList(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyPropetiesList(strategy);
		
		return Collections.emptySet();
	}

	@Override
	public String getStrategyPropertyValue(String unitName, String strategy,
			String property)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyPropertyValue(strategy, property);
		
		return null;
	}

	@Override
	public void setStrategyPropertyValue(String unitName, String strategy,
			String property, String value)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).setStrategyPropertyValue(strategy, property, value);
	}

		
	@Override
	public ModificationMode getStrategyPropertyModificationMode(
			String unitName, String strategy, String property)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyPropertyModificationMode(strategy, property);
		
		return null;
	}

	@Override
	public boolean startStrategy(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).startStrategy(strategy);
		
		return false;
	}

	@Override
	public boolean stopStrategy(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).stopStrategy(strategy);
		
		return false;
	}
	
	@Override
	public void submitMarketOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side,
			String instrumentId, int size,TimeInForceMode timeInForce,int expireSec)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitMarketOrder(strategy, source, executionPoint, side, instrumentId, size, timeInForce, expireSec);
	}
	
	@Override
	public void submitLimitOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side,
			String instrumentId, int size, int price,TimeInForceMode timeInForce,int expireSec)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitLimitOrder(strategy, source, executionPoint, side, instrumentId, size, price, timeInForce, expireSec);		
	}
	
	@Override
	public void submitOCOOrder(String unitName, String strategy, String source,
			String executionPoint, OrderSide side, String instrumentId,
			int size, OCOSettings ocoSettings)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitOCOOrder(strategy, source, executionPoint, side, instrumentId, size, ocoSettings);
	}

	@Override
	public void submitStopLimitOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side,
			String instrumentId, int size, int price, StopSides stopSide,
			int stopPrice,TimeInForceMode timeInForce,int expireSec)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitStopLimitOrder(strategy, source, executionPoint, side, instrumentId, size, price, stopSide, stopPrice, timeInForce, expireSec);		
	}

	@Override
	public void submitStopLossOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side,
			String instrumentId, int size, StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitStopLossOrder(strategy, source, executionPoint, side, instrumentId, size, stopSide, stopPrice, timeInForce, expireSec);		
	}

	@Override
	public void submitTrailingStopOrder(String unitName, String strategy,
			String source, String executionPoint, OrderSide side,
			String instrumentId, int size, int price, StopSides stopSide, int stopPrice,
			int trailBy, int maxSlippage, int initialTriggerRate,TimeInForceMode timeInForce,int expireSec)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitTrailingStopOrder(strategy, source, executionPoint, side, instrumentId, size, price, stopSide, stopPrice, trailBy, maxSlippage, initialTriggerRate, timeInForce, expireSec);		
	}

	@Override
	public void submitCustomOrder(String unitName, String strategy,
			String source, String executionPoint, TradeOrder tradeOrder)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).submitCustomOrder(strategy, source, executionPoint, tradeOrder);		
	}

	@Override
	public void cancelOrder(String unitName, String strategy, String source,
			String executionPoint, String originalOrderReference)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).cancelOrder(strategy, source, executionPoint, originalOrderReference);		
	}
	
	@Override
	public void replaceOrder(String unitName, String strategy, String source,
			String executionPoint, String originalOrderReference,
			String newOrderReference, int size, int price)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).replaceOrder(strategy, source, executionPoint, originalOrderReference, newOrderReference, size, price);
	}

	@Override
	public Set<String> getStrategyExecutionPoints(String unitName,
			String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyExecutionPoints(strategy);
			
		return Collections.emptySet();
	}

	@Override
	public String getExecutionPointMarketConnection(String unitName,
			String strategy, String executionPoint)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getExecutionPointMarketConnection(strategy, executionPoint);
			
		return null;
	}
	
	@Override
	public boolean getExecutionPointIsActive(String unitName, String strategy,
			String executionPoint)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getExecutionPointIsActive(strategy, executionPoint);
		
		return false;
	}

	@Override
	public Set<String> getInstruments(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getInstruments();
		
		return Collections.emptySet();
	}
	
	@Override
	public String getSymbol(String unitName, String instrumentId)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getSymbol(instrumentId);
		
		return null;
	}

	@Override
	public double castPriceToDecimal(String unitName, String instrumentId,
									 long price)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).castPriceToDecimal(instrumentId, price);
		
		return 0;
	}

	@Override
	public long castPriceToLong(String unitName, String instrumentId,
								double price)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).castPriceToLong(instrumentId, price);
		
		return 0;
	}

	@Override
	public Set<String> getMarketConnections(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketConnections();
		
		return Collections.emptySet();
	}
	
	@Override
	public Set<String> getTradingMarketConnections(String unitName)
	{
		if (unitProviders.containsKey(unitName))
		{
			Set<String> tradingMarketConnections = new HashSet<String>();
			
			TradingUnitProvider tradingUnitProvider = unitProviders.get(unitName);
			
			for (String mc : tradingUnitProvider.getMarketConnections())
				if (tradingUnitProvider.isTradingMarketConnection(mc))
					tradingMarketConnections.add(mc);
			
			return tradingMarketConnections;
		}
		
		return Collections.emptySet();
	}

	@Override
	public boolean isTradingMarketConnection(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).isTradingMarketConnection(marketConnection);
		
		return false;
	}
	
	@Override
	public boolean isConnectedMarketConnection(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).isConnectedMarketConnection(marketConnection);
		
		return false;
	}

	@Override
	public String getMarketConnectionDisplayName(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketConnectionDisplayName(marketConnection);
		
		return null;
	}

	@Override
	public Set<String> getCreditLimitedInstruments(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getCreditLimitedInstruments(marketConnection);
		
		return Collections.emptySet();
	}

	@Override
	public double getInstrumentCreditLimit(String unitName, String marketConnection,
			String instrumentId)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getInstrumentCreditLimit(marketConnection, instrumentId);
		
		return 0;
	}	

	@Override
	public void setInstrumentCreditLimit(String unitName,
			String marketConnection, String instrumentId, double creditLimit)
	{
		if (unitProviders.containsKey(unitName))
			unitProviders.get(unitName).setInstrumentCreditLimit(marketConnection, instrumentId, creditLimit);		
	}

	@Override
	public boolean sendCommand(String command, Map<String, Object> args)
	{
		return false;
	}

	@Override
	public String getViewModel()
	{
		return "Not Implemented";
	}

	private static int PROXY_BUFFER_SIZE = 40960;
	
	@Override
	public boolean execute(ClientCommand command)
	{
		if (command instanceof StrategyDataStreamingRequest)
		{
			final StrategyDataStreamingRequest sdsRequest = (StrategyDataStreamingRequest)command;
			
			if (!unitProviders.containsKey(sdsRequest.getUnitName()))
				return false;
			
			final int port = unitProviders.get(sdsRequest.getUnitName()).getStrategyDataStreamPort(sdsRequest.getStrategyName());
			
			if (port == 0)
				return false;
			
			sdsRequest.getClientHandler().setTimeout(0);
								
			Thread proxy = new Thread() {
				
				@Override
				public void run()
				{
					ZMQReceiver zmqReceiver = null;
					OutputStream output = null;
					try
					{
						zmqReceiver = new ZMQReceiver("localhost", port);
						output = sdsRequest.getClientHandler().getSocket().getOutputStream();
						
						byte[] buffer = new byte[PROXY_BUFFER_SIZE];
						InputStream input = zmqReceiver.getInput();
						while(true)
						{							
							int readedSize = input.read(buffer);
							if (readedSize == -1)
								break;
							output.write(buffer, 0, readedSize);
							output.flush();
						}
					}
					catch(Exception e)
					{						
						sdsRequest.getClientHandler().closeConnection();						
					}
					finally
					{
						try
						{
							zmqReceiver.endInput();
							output.close();
						}
						catch (Exception e)
						{
							logger.error(e.getMessage());
						}						
					}
				}
			};
			proxy.setDaemon(true);
			proxy.run();
			
			return true;
		}
		
		return false;
	}

	@Override
	public Set<String> getPipelines(String unitName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getPipelines();
		
		return null;
	}
	
	@Override
	public boolean isPipelineStarted(String unitName, String pipeline)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).isPipelineStarted(pipeline);
		
		return false;
	}

	@Override
	public Map<String, String> getPipelineFeeds(String unitName, String pipeline)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getPipelineFeeds(pipeline);
		
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getStrategyDataSinks(String unitName, String strategy)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyDataSinks(strategy);
		
		return Collections.emptySet();
	}

	@Override
	public boolean getStrategyDataSinkIsActive(String unitName, String strategy,
			String dataSink)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyDataSinkIsActive(strategy, dataSink);
		
		return false;
	}

	@Override
	public String getStrategyDataSinkPipeline(String unitName, String strategy,
			String dataSink)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getStrategyDataSinkPipeline(strategy, dataSink);
		
		return null;
	}
		
	@Override
	public String getMarketConnectionIdentifier(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketConnectionIdentifier(marketConnection);
			
		return null;
	}

	@Override
	public int getMarketConnectionMode(String unitName, String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketConnectionMode(marketConnection);
		
		return 0;
	}

	@Override
	public Set<String> getMarketDataFeeds(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketDataFeeds(marketConnection);
		
		return Collections.emptySet();
	}

	@Override
	public String getMarketDataFeedInstrument(String unitName,
			String marketConnection, String feedName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketDataFeedInstrument(marketConnection, feedName);
		
		return null;
	}

	@Override
	public String getMarketDataFeedMarketDepth(String unitName,
			String marketConnection, String feedName)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getMarketDataFeedMarketDepth(marketConnection, feedName);
		
		return null;
	}

	@Override
	public Set<String> getExecutionFeeds(String unitName,
			String marketConnection)
	{
		if (unitProviders.containsKey(unitName))
			return unitProviders.get(unitName).getExecutionFeeds(marketConnection);
		
		return Collections.emptySet();
	}
	

	public static void main(String[] args) throws Exception
	{
		String pathToConfigFile;
		
		if (args.length > 0)
		{
			pathToConfigFile = args[0];
		}			
		else
		{
			pathToConfigFile = "config/quantfabric.server.cfg.xml";
		}
		
		File configFile = new File(pathToConfigFile);		
		
		logger.info("Loading config : \"{}\"", configFile.getAbsolutePath());
		Config config = Config.load(configFile);
		
		if (config == null)
		{
			logger.error("Failed to read the configuration file.");
			return;
		}

		List<AlgoServerConfig> algoServerConfigs = config.getAlgoServerConfigs();
		if (algoServerConfigs.isEmpty())
		{
			logger.error("Configuration file is corrupted.");
			return;
		}		
		
		AlgoServerConfig algoServerConfig = algoServerConfigs.get(0);
	
		int algoServerPort = Integer.parseInt(algoServerConfig.getPort());
		AlgoServer.PROXY_BUFFER_SIZE = Integer.parseInt(algoServerConfig.getProperties().getProperty("proxyBufferSize", "8192"));
		
		CountDownLatch shutdownLatch = new CountDownLatch(1);
						
		final AlgoServer server = AlgoServer.getInstance();
		
		String algoServerJMXhost = algoServerConfig.getProperties().getProperty("algoServerJMXhost");
		String algoServerJMXPortString = algoServerConfig.getProperties().getProperty("algoServerJMXPort");
		int algoServerJMXPort = 0;
		if (algoServerJMXPortString != null)
			algoServerJMXPort = Integer.parseInt(algoServerJMXPortString);
		
		if (algoServerJMXhost != null && algoServerJMXPort != 0)
				server.startJMXService(algoServerJMXhost, shutdownLatch, algoServerJMXPort);
		
		List<AlgoHostConfig> ahcList = algoServerConfig.getAlgoHostList();
		
		if (ahcList.isEmpty())
		{
			logger.error("Configuration file is corrupted.");
			return;
		}
		
		AlgoHostConfig algoHostConfig = ahcList.get(0);
		
		String algoHostName = algoHostConfig.getName();
		String pathToLoad = algoHostConfig.getPathToLoad();
		String algoHostMaxHeapSize = algoHostConfig.getProperties().getProperty("jvmMaxHeap");
		String algoHostMaxPermSize = algoHostConfig.getProperties().getProperty("jvmMaxPermSize");		
		
		Properties configuration = new Properties();
		
		//remote debug
		if (algoHostConfig.getProperties().containsKey("remote-debug"))
		{
			if (Boolean.parseBoolean(algoHostConfig.getProperties().getProperty("remote-debug")))
			{
				configuration.setProperty("-Xdebug", "");
				
				String debugAddress = "8000";
				if (algoHostConfig.getProperties().containsKey("remote-debug-address"))
				{
					debugAddress = algoHostConfig.getProperties().getProperty("remote-debug-address");
				}
				
				String suspendMode = "n";
				if (algoHostConfig.getProperties().containsKey("remote-debug-suspend"))
				{
					suspendMode = algoHostConfig.getProperties().getProperty("remote-debug-suspend");
				}
				
				configuration.setProperty("-Xrunjdwp:transport", 
						String.format("dt_socket,address=%s,server=y,suspend=%s", debugAddress, suspendMode));
			}
		}
		
		if (algoHostConfig.getProperties().containsKey("log-config-path"))
		{
			configuration.setProperty("log4j.configuration", algoHostConfig.getProperties().getProperty("log-config-path"));
			logger.info("Logging config for {} - {}", algoHostName, configuration.getProperty("log4j.configuration"));
		}
		
		configuration.setProperty(
				ProcessLauncher.CLASS_NAME, "com.quantfabric.algo.server.AlgoHost");
		configuration.setProperty("com.quantfabric.algo.server.config_root", pathToLoad);
		
		String algoHostConfigFile = System.getProperty("com.quantfabric.algo.server.config-file-path", null);
		if (algoHostConfigFile != null)
			configuration.setProperty("com.quantfabric.algo.server.config-file-path", algoHostConfigFile);
					
		configuration.setProperty(ProcessLauncher.JVM_MAX_HEAP, "-Xmx" + algoHostMaxHeapSize);
		configuration.setProperty(ProcessLauncher.JVM_MAX_PERM_SIZE, "-XX:PermSize=" + algoHostMaxPermSize);
		//Remote JMX		
		
		String jmxHost = System.getProperty("java.rmi.server.hostname", null);
		String jmxPort = System.getProperty("com.quantfabric.util.jmx-remote-port", "7000");
		
		if (jmxHost != null)
		{
			configuration.setProperty("com.sun.management.jmxremote", "");		
			configuration.setProperty("com.sun.management.jmxremote.authenticate", "false");
			configuration.setProperty("com.sun.management.jmxremote.ssl", "false");
			configuration.setProperty("java.rmi.server.hostname", jmxHost);
			configuration.setProperty("com.quantfabric.util.jmx-remote-port", jmxPort);
			configuration.setProperty("-javaagent:./agents/remote-jmx-agent.jar", "");
		}
		
		if (algoServerJMXhost != null && algoServerJMXPort != 0)
			configuration.setProperty("com.quantfabric.algo.server.jmx.connection-url",
					server.jmxAlgoServerService.getConnectionURL());
		
		server.addHostConfiguration(algoHostName, configuration);	
		server.createUnit(algoHostName, algoHostName);
		
		boolean startManagementServices = Boolean.parseBoolean(PropertiesViewer.getProperty(System.getProperties(),
				"com.quantfabric.algo.server.start-management-services", "true"));

		if (startManagementServices) {

			long timeout = 240000;

			if (args.length > 1)
				timeout = Long.parseLong(args[1]);

			Thread.sleep(timeout);

			server.startManagementServices(algoServerPort);
		}

		ShutdownHook shutdownHook = new ShutdownHook(shutdownLatch);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try
        {
            shutdownLatch.await();
            shutdownHook.setShutdown(true);           
        }
        catch(InterruptedException e)
        {
           logger.error("Shutdown interrupted. {} ", e.getMessage());
		   throw e;
        }

        logger.info("{} - QuantfabricAlgoServer is shut down", SessionId.getSessionID());
        
        System.exit(0);
	}
}
