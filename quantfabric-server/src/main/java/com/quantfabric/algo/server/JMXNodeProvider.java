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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gate.jmx.mbean.ExecutionFeedMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.InstrumentMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketConnectionMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataFeedMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataPipelineMBean;
import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;
import com.quantfabric.algo.trading.strategy.DataSinkInfo;
import com.quantfabric.algo.trading.strategy.ExecutionPoint;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting.ModificationMode;
import com.quantfabric.algo.trading.strategyrunner.jmx.TradingJMXProvider;
import com.quantfabric.algo.trading.strategyrunner.jmx.mbean.TradingStrategyMXBean;
import com.quantfabric.algo.trading.strategyrunner.jmx.notifications.StrategyCreatedNotification;
import com.quantfabric.algo.trading.strategyrunner.jmx.notifications.StrategyRemovedNotification;
import com.quantfabric.util.Converter;
import com.quantfabric.util.PropertiesViewer;

public class JMXNodeProvider implements NodeProvider {

	private static final Logger logger = LoggerFactory.getLogger(JMXNodeProvider.class);

	private final Map<String, TradingStrategyMXBean> strategies = new HashMap<String, TradingStrategyMXBean>();
	private final Map<String, Instrument> unitInstruments = new HashMap<String, Instrument>();
	private final Map<String, MarketConnectionMBean> marketConnections = new HashMap<String, MarketConnectionMBean>();
	private final Map<String, Map<String, MarketDataFeedMBean>> marketDataFeeds = new HashMap<String, Map<String, MarketDataFeedMBean>>();
	private final Map<String, Map<String, ExecutionFeedMBean>> executionFeeds = new HashMap<String, Map<String, ExecutionFeedMBean>>();
	private final Map<String, MarketDataPipelineMBean> pipelines = new HashMap<String, MarketDataPipelineMBean>();

	private String name;
	private String description;

	private JMXServiceURL jmxServiceUrl;
	private String strategiesObjectNameReference;
	private String instrumentsObjectNameReference;
	private String marketConnectionsObjectNameReference;
	private String pipelinesObjectNameReference;

	private boolean configured = false;

	private boolean loaded = false;

	private static Map<String, String> getStrategySettings(TradingStrategyMXBean strategy) {
		return strategy.getSettingValues();
	}

	protected TradingStrategyMXBean getStrategy(String strategyName) {
		if (strategies.containsKey(strategyName))
			return strategies.get(strategyName);

		logger.error("Unknown strategy \"{}\"", strategyName);
		return null;
	}

	protected void addStrategy(TradingStrategyMXBean strategy) {
		strategies.put(strategy.getName(), strategy);
	}

	private void removeStrategy(String strategyName) {

		strategies.remove(strategyName);
	}

	private boolean startStrategy(TradingStrategyMXBean strategy) {
		try {
			if (!strategy.isRunning())
				strategy.start();
		}
		catch (Exception e) {
			logger.error("Strategy failed to start", e);
			return false;
		}
		return true;
	}

	private boolean stopStrategy(TradingStrategyMXBean strategy) {
		try {
			if (strategy.isRunning())
				strategy.stop();
		}
		catch (Exception e) {
			logger.error("Strategy failed to stop", e);
			return false;
		}
		return true;
	}

	@Override
	public void shutdown() {
		for (TradingStrategyMXBean strategy : strategies.values())
			try {
				if (strategy.isRunning())
					strategy.stop();
			}
			catch (Exception e) {
				logger.error("Strategy failed to stop", e);
			}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Set<String> getStrategies() {
		return strategies.keySet();
	}

	@Override
	public Map<String, String> getStrategyInfo(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) {
			HashMap<String, String> strategInfo = new HashMap<String, String>();
			strategInfo.put("name", strategy.getName());
			strategInfo.put("description", strategy.getDescription());
			strategInfo.put("type", strategy.getType());
			return strategInfo;
		}

		return null;
	}

	@Override
	public Properties getStrategyState(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) {
			Properties strategState = new Properties();
			strategState.put("isEnabled", strategy.isEnabled());
			strategState.put("isPlugged", strategy.isPlugged());
			strategState.put("isRunning", strategy.isRunning());
			return strategState;
		}

		return null;
	}

	@Override
	public String getStrategySettingsLayoutDefinition(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) { return strategy.getSettingsLayoutDefinition(); }

		return null;
	}

	@Override
	public Properties getStrategyProperties(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) { return Converter.mapToProperties(getStrategySettings(strategy)); }

		return null;
	}

	@Override
	public Set<String> getStrategyPropetiesList(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			return getStrategySettings(strategy).keySet();

		return null;
	}

	@Override
	public String getStrategyPropertyValue(String strategyName, String propertyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			return getStrategySettings(strategy).get(propertyName);

		return null;
	}

	@Override
	public void setStrategyPropertyValue(String strategyName, String propertyName, String value) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.setSettingValue(propertyName, value);
	}

	@Override
	public ModificationMode getStrategyPropertyModificationMode(String strategyName, String propertyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			return strategy.getSettings().get(propertyName).getModificationMode();

		return null;
	}

	@Override
	public boolean startStrategy(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			return startStrategy(strategy);
		else
			return false;
	}

	@Override
	public boolean stopStrategy(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			return stopStrategy(strategy);
		else
			return false;
	}

	@Override
	public void submitMarketOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size,
			TimeInForceMode timeInForce, int expireSec) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitMarketOrder(source, executionPoint, side, instrumentId, size, timeInForce, expireSec);
	}

	@Override
	public void submitLimitOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size, int price,
			TimeInForceMode timeInForce, int expireSec) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitLimitOrder(source, executionPoint, side, instrumentId, size, price, timeInForce, expireSec);
	}

	@Override
	public void submitOCOOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size, OCOSettings ocoSettings) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitOCOOrder(source, executionPoint, side, instrumentId, size, ocoSettings);
	}

	@Override
	public void submitStopLimitOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size, int price,
			StopSides stopSide, int stopPrice, TimeInForceMode timeInForce, int expireSec) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitStopLimitOrder(source, executionPoint, side, instrumentId, size, price, stopSide, stopPrice, timeInForce, expireSec);
	}

	@Override
	public void submitStopLossOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size,
			StopSides stopSide, int stopPrice, TimeInForceMode timeInForce, int expireSec) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitStopLossOrder(source, executionPoint, side, instrumentId, size, stopSide, stopPrice, timeInForce, expireSec);
	}

	@Override
	public void submitTrailingStopOrder(String strategyName, String source, String executionPoint, OrderSide side, String instrumentId, int size, int price,
			StopSides stopSide, int stopPrice, int trailBy, int maxSlippage, int initialTriggerRate, TimeInForceMode timeInForce, int expireSec) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.submitTrailingStopOrder(source, executionPoint, side, instrumentId, size, price, stopSide, stopPrice, trailBy, maxSlippage,
					initialTriggerRate, timeInForce, expireSec);
	}

	@Override
	public void submitCustomOrder(String strategyName, String source, String executionPoint, TradeOrder tradeOrder) {
		/*
		 * TradingStrategyMXBean strategy; if ((strategy =
		 * getStrategy(strategyName)) != null)
		 * strategy.submitCustomOrder(source, executionPoint, tradeOrder);
		 */

		logger.debug("submitCustomOrder(" + "strategyName=" + strategyName + ", source=" + source + ", executionPoint=" + executionPoint + ", tradeOrder="
				+ Converter.toString(tradeOrder) + ")");
		logger.info("submitCustomOrder - not implemented");
	}

	@Override
	public void cancelOrder(String strategyName, String source, String executionPoint, String originalOrderReference) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.cancelOrder(source, executionPoint, originalOrderReference);
	}

	@Override
	public void replaceOrder(String strategyName, String source, String executionPoint, String originalOrderReference, String newOrderReference, int size,
			int price) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			strategy.replaceOrder(source, executionPoint, originalOrderReference, newOrderReference, size, price);
	}

	protected void addUnitInstrument(InstrumentMBean instrument) {
		unitInstruments.put(instrument.getId(), instrument);
	}

	protected void addMarketConnection(MarketConnectionMBean marketConnection) {
		marketConnections.put(marketConnection.getName(), marketConnection);
	}

	protected void addPipeline(MarketDataPipelineMBean pipeline) {
		pipelines.put(pipeline.getName(), pipeline);
	}

	private void loadStrategies(MBeanServerConnection mbsc) throws Exception {

		Set<ObjectName> names = mbsc.queryNames(new ObjectName(strategiesObjectNameReference), null);

		for (ObjectName name : names) 
			addStrategy(JMX.newMXBeanProxy(mbsc, name, TradingStrategyMXBean.class, true));
	}

	private void reloadStrategies(MBeanServerConnection mbsc) throws Exception {

		strategies.clear();

		loadStrategies(mbsc);
		initializeStrategyRunnerListener(mbsc);
	}

	private void loadInstruments(MBeanServerConnection mbsc) throws Exception {
		Set<ObjectName> names = mbsc.queryNames(new ObjectName(instrumentsObjectNameReference), null);

		for (ObjectName name : names)
			addUnitInstrument(JMX.newMBeanProxy(mbsc, name, InstrumentMBean.class, true));
	}

	private void loadMarketConnections(MBeanServerConnection mbsc) throws Exception {
		Set<ObjectName> names = mbsc.queryNames(new ObjectName(marketConnectionsObjectNameReference), null);

		for (ObjectName name : names) {
			MarketConnectionMBean marketConnection = JMX.newMBeanProxy(mbsc, name, MarketConnectionMBean.class, true);
			addMarketConnection(marketConnection);
			loadFeeds(mbsc, marketConnection);
		}
	}

	private void loadFeeds(MBeanServerConnection mbsc, MarketConnectionMBean marketConnection) {
		Collection<ObjectName> marketDataFeedsNames = marketConnection.getMarketDataFeeds();

		for (ObjectName name : marketDataFeedsNames)
			addMarketDataFeed(marketConnection.getName(), JMX.newMBeanProxy(mbsc, name, MarketDataFeedMBean.class, true));

		Collection<ObjectName> executionFeedsNames = marketConnection.getExecutionFeeds();

		for (ObjectName name : executionFeedsNames)
			addExecutionFeed(marketConnection.getName(), JMX.newMBeanProxy(mbsc, name, ExecutionFeedMBean.class, true));
	}

	private void addMarketDataFeed(String marketConnectionName, MarketDataFeedMBean feed) {
		if (!marketDataFeeds.containsKey(marketConnectionName))
			marketDataFeeds.put(marketConnectionName, new HashMap<String, MarketDataFeedMBean>());

		marketDataFeeds.get(marketConnectionName).put(feed.getName(), feed);
	}

	private void addExecutionFeed(String marketConnectionName, ExecutionFeedMBean feed) {
		if (!executionFeeds.containsKey(marketConnectionName))
			executionFeeds.put(marketConnectionName, new HashMap<String, ExecutionFeedMBean>());

		executionFeeds.get(marketConnectionName).put(feed.getName(), feed);
	}

	private void loadPipelines(MBeanServerConnection mbsc) throws Exception {
		Set<ObjectName> names = mbsc.queryNames(new ObjectName(pipelinesObjectNameReference), null);

		for (ObjectName name : names)
			addPipeline(JMX.newMBeanProxy(mbsc, name, MarketDataPipelineMBean.class, true));
	}

	@Override
	public void load() throws Exception {
		if (configured) {
			JMXConnector jmxc = JMXConnectorFactory.connect(jmxServiceUrl);
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			loadStrategies(mbsc);
			loadInstruments(mbsc);
			loadMarketConnections(mbsc);
			loadPipelines(mbsc);

			initializeMBeanServerListener(mbsc);
			initializeStrategyRunnerListener(mbsc);

			loaded = true;
		}
		else
			throw new Exception("Node Manager is not configured.");
	}

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public void configure(Properties properties) throws Exception {
		name = PropertiesViewer.getProperty(properties, "name");
		description = PropertiesViewer.getProperty(properties, "description");
		jmxServiceUrl = new JMXServiceURL(PropertiesViewer.getProperty(properties, "jmxServiceUrl"));
		// "service:jmx:rmi://localhost/jndi/rmi://localhost:7000/jmxrmi"

		strategiesObjectNameReference = PropertiesViewer.getProperty(properties, "strategiesObjectNameReference");
		// "Xcellerate:type=AlgoServer,group=services,serviceName=Trading,servicegroup=Strategies,*"

		instrumentsObjectNameReference = PropertiesViewer.getProperty(properties, "instrumentsObjectNameReference");
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Instruments,*";

		marketConnectionsObjectNameReference = PropertiesViewer.getProperty(properties, "marketConnectionsObjectNameReference");
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,*"

		pipelinesObjectNameReference = PropertiesViewer.getProperty(properties, "pipelinesObjectNameReference");
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Pipelines,pipelineName=*"

		configured = true;
	}

	@Override
	public int getStrategyDataStreamPort(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) { return strategy.getStrategyDataStreamPort(); }
		return 0;
	}

	@Override
	public Set<String> getInstruments() {
		return unitInstruments.keySet();
	}

	@Override
	public String getSymbol(String instrumentId) {
		if (unitInstruments.containsKey(instrumentId))
			return unitInstruments.get(instrumentId).getSymbol();

		return null;
	}

	@Override
	public double castPriceToDecimal(String instrumentId, long price) {
		if (unitInstruments.containsKey(instrumentId))
			return unitInstruments.get(instrumentId).castToDecimal(price);

		return 0;
	}

	@Override
	public long castPriceToLong(String instrumentId, double price) {
		if (unitInstruments.containsKey(instrumentId))
			return unitInstruments.get(instrumentId).castToLong(price);

		return 0;
	}

	@Override
	public Set<String> getMarketConnections() {
		return marketConnections.keySet();
	}

	@Override
	public boolean isTradingMarketConnection(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).isTradingMarketConnection();

		return false;
	}

	@Override
	public boolean isConnectedMarketConnection(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).isConnected();

		return false;
	}

	@Override
	public String getMarketConnectionDisplayName(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).getDisplayName();

		return null;
	}

	@Override
	public Set<String> getCreditLimitedInstruments(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).getCreditLimitedInstruments();

		return null;
	}

	@Override
	public double getInstrumentCreditLimit(String marketConnection, String instrumentId) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).getInstrumentCreditLimit(instrumentId);

		return 0;
	}

	@Override
	public void setInstrumentCreditLimit(String marketConnection, String instrumentId, double creditLimit) {
		if (marketConnections.containsKey(marketConnection))
			marketConnections.get(marketConnection).setInstrumentCreditLimit(instrumentId, creditLimit);
	}

	protected Map<String, ExecutionPoint> getStrategyExecutionPointsDictionary(String strategyName) {
		Map<String, ExecutionPoint> excutionPoints = new HashMap<String, ExecutionPoint>();

		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null)
			;
		for (ExecutionPoint ep : strategy.getExecutionEndPoints())
			excutionPoints.put(ep.getTargetMarket(), ep);

		return excutionPoints;
	}

	@Override
	public Set<String> getStrategyExecutionPoints(String strategyName) {
		return getStrategyExecutionPointsDictionary(strategyName).keySet();
	}

	@Override
	public String getExecutionPointMarketConnection(String strategyName, String executionPoint) {
		return getStrategyExecutionPointsDictionary(strategyName).get(executionPoint).getConnection();
	}

	@Override
	public boolean getExecutionPointIsActive(String strategyName, String executionPoint) {
		return getStrategyExecutionPointsDictionary(strategyName).get(executionPoint).isActive();
	}

	@Override
	public Set<String> getPipelines() {
		return pipelines.keySet();
	}

	@Override
	public Map<String, String> getPipelineFeeds(String pipeline) {
		if (pipelines.containsKey(pipeline))
			return pipelines.get(pipeline).getPipelineFeeds();

		return null;
	}

	@Override
	public boolean isPipelineStarted(String pipeline) {
		if (pipelines.containsKey(pipeline))
			return pipelines.get(pipeline).isStrated();

		return false;
	}

	@Override
	public Set<String> getStrategyDataSinks(String strategyName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) {
			@SuppressWarnings("unused")
			Object o = strategy.getDataSinks();

			Set<String> dataSinksNames = new LinkedHashSet<String>();
			for (DataSinkInfo dataSink : strategy.getDataSinks())
				dataSinksNames.add(dataSink.getName());
			return dataSinksNames;
		}
		return null;
	}

	@Override
	public boolean getStrategyDataSinkIsActive(String strategyName, String dataSinkName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) {
			for (DataSinkInfo dataSink : strategy.getDataSinks())
				if (dataSink.getName().equals(dataSinkName))
					return dataSink.isActive();
		}
		return false;
	}

	@Override
	public String getStrategyDataSinkPipeline(String strategyName, String dataSinkName) {
		TradingStrategyMXBean strategy;
		if ((strategy = getStrategy(strategyName)) != null) {
			for (DataSinkInfo dataSink : strategy.getDataSinks())
				if (dataSink.getName().equals(dataSinkName))
					return dataSink.getPipeline();
		}
		return null;
	}

	@Override
	public String getMarketConnectionIdentifier(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).getIdentifier();

		return null;
	}

	@Override
	public int getMarketConnectionMode(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			return marketConnections.get(marketConnection).getMode();

		return 0;
	}

	@Override
	public Set<String> getMarketDataFeeds(String marketConnection) {
		if (marketDataFeeds.containsKey(marketConnection))
			return marketDataFeeds.get(marketConnection).keySet();

		return null;
	}

	@Override
	public String getMarketDataFeedInstrument(String marketConnection, String feedName) {
		if (marketDataFeeds.containsKey(marketConnection)) {
			Map<String, MarketDataFeedMBean> feeds = marketDataFeeds.get(marketConnection);
			if (feeds.containsKey(feedName))
				return feeds.get(feedName).getInstrumentId();
		}

		return null;
	}

	@Override
	public String getMarketDataFeedMarketDepth(String marketConnection, String feedName) {
		if (marketDataFeeds.containsKey(marketConnection)) {
			Map<String, MarketDataFeedMBean> feeds = marketDataFeeds.get(marketConnection);
			if (feeds.containsKey(feedName))
				return feeds.get(feedName).getMarketDepth();
		}

		return null;
	}

	@Override
	public Set<String> getExecutionFeeds(String marketConnection) {
		if (executionFeeds.containsKey(marketConnection))
			return executionFeeds.get(marketConnection).keySet();

		return null;
	}

	public static void main(String[] arg) throws Exception {
		JMXServiceURL jmxServiceUrl = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:7000/jmxrmi");

		String objectNameReference =
		// "Xcellerate:type=AlgoServer,group=services,serviceName=Trading,servicegroup=Strategies,strategyName=*";
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Instruments,instrumentName=*";
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=*";
		"Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Pipelines,pipelineName=*";

		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=*,connection_group=Feeds,feed_type=MarketData,feedName=*";
		// "Xcellerate:type=AlgoServer,group=services,serviceName=MarketGateway,service_group=Connections,connectionName=*,connection_group=Feeds,feed_type=Execution,feedName=*";

		JMXConnector jmxc = JMXConnectorFactory.connect(jmxServiceUrl);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		Set<ObjectName> names = mbsc.queryNames(new ObjectName(objectNameReference), null);

		for (ObjectName name : names)
			System.out.println(name);
	}

	@Override
	public boolean connectMarketConnection(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			try {
				marketConnections.get(marketConnection).connect();
			}
			catch (MarketConnectionException e) {
				logger.error("Error during marketConnection connect : ", e);
				return false;
			}

		return true;
	}

	@Override
	public boolean disconnectMarketConnection(String marketConnection) {
		if (marketConnections.containsKey(marketConnection))
			try {
				marketConnections.get(marketConnection).disconnect();
			}
			catch (MarketConnectionException e) {
				logger.error("Error during marketConnection disconnect : ", e);
				return false;
			}

		return true;
	}

	private void initializeMBeanServerListener(MBeanServerConnection server) throws Exception {

		StrategyNotificationListener serverListener = new StrategyNotificationListener(server);		
		MBeanServerNotificationFilter filter = new MBeanServerNotificationFilter();
		
		filter.enableObjectName(new ObjectName(TradingJMXProvider.JMX_OBJECT_NAME));
		server.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, serverListener, filter, null);
	}

	private void initializeStrategyRunnerListener(MBeanServerConnection server) throws Exception {

		ObjectName name = new ObjectName(TradingJMXProvider.JMX_OBJECT_NAME);
		StrategyNotificationListener strategylistener = new StrategyNotificationListener(server);
		StrategyNotificationFilter filter = new StrategyNotificationFilter();

		if (server.isRegistered(name)) {
			server.addNotificationListener(name, strategylistener, filter, name);
			logger.info("Notification Listener attached to MBean [ {} ]", name);
		}
		else
			logger.info("MBean [ {} ] is not registered on server", name);
	}

	private class StrategyNotificationListener implements NotificationListener {

		private final MBeanServerConnection server;

		StrategyNotificationListener(MBeanServerConnection server) {
			this.server = server;
		}

		@Override
		public void handleNotification(Notification notification, Object handback) {

			String type = notification.getType();

			if ((notification instanceof MBeanServerNotification)) {

				MBeanServerNotification mbsn = (MBeanServerNotification) notification;
				String message = null;
				if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
					try {
						reloadStrategies(server);
						message = "MBean registered";
					}
					catch (Exception e) {
						logger.error("Failed to reload strategies", e);
					}
				}
				else if (type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
					message = "MBean unregistered";
				else
					message = "Unknown type " + type;

				logger.info("Received MBean Server notification [ {} ]-[ {} ]", message, mbsn.getMBeanName());
			}
			else {
				if (type.equals(StrategyCreatedNotification.TYPE)) {
					try {
						loadStrategies(server);
					}
					catch (Exception e) {
						logger.error("Failed to load strategies from [ {} ]", notification.getSource());
					}
				}
				else if (type.equals(StrategyRemovedNotification.TYPE)) {
					try {
						removeStrategy(notification.getMessage());
					}
					catch (Exception e) {
						logger.error("Failed to remove strategy [ {} }]", notification.getMessage());
					}
				}
				logger.info("Handled notification [ {} ]-[ {} ]", notification.getType(), notification.getMessage());
			}
		}
	}

	public static class StrategyNotificationFilter implements NotificationFilter {

		private static final long serialVersionUID = -3733662418007093260L;

		public boolean isNotificationEnabled(Notification notification) {
			return notification.getType().equals(StrategyCreatedNotification.TYPE) ||
					notification.getType().equals(StrategyRemovedNotification.TYPE);
		}
	}

}
