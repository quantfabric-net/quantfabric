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
package com.quantfabric.algo.trading.strategyrunner.jmx;

import java.io.IOException;
import java.util.Map;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategyrunner.ConfigProvider;
import com.quantfabric.algo.trading.strategyrunner.StrategyLoadContext;
import com.quantfabric.algo.trading.strategyrunner.StrategyLoadRunner;
import com.quantfabric.algo.trading.strategyrunner.jmx.mbean.StrategyLoadRunnerMXBean;
import com.quantfabric.algo.trading.strategyrunner.jmx.notifications.StrategyCreatedNotification;
import com.quantfabric.algo.trading.strategyrunner.jmx.notifications.StrategyRemovedNotification;

public class StrategyLoadRunnerMgmt implements StrategyLoadRunnerMXBean, NotificationBroadcaster {

	private StrategyLoadRunner theService = null;
	private final TradingJMXProvider jmxProvider;
	private final NotificationBroadcasterSupport broadcaster;
	private long notificationSequence = 0;

	public enum NotificationTypes {
		STRATEGY_CREATED, STRATEGY_REMOVED,
	}

	public StrategyLoadRunnerMgmt(StrategyLoadRunner service, TradingJMXProvider jmxProvider) {
		theService = service;
		this.jmxProvider = jmxProvider;
		broadcaster = new NotificationBroadcasterSupport();
	}

	@Override
	public Map<String, String> getGlobalSettings() throws IOException {
		return theService.getGlobalSettings();
	}

	@Override
	public void setGlobalSetting(String key, String value) throws IOException {
		theService.getGlobalSettings().put(key, value);
	}

	@Override
	public void reloadStrategy(String strategyName) throws Exception 
	{
		StrategyLoadContext strategyLoadContext = 
				theService.getStrategyLoadContext(strategyName);
		
		if (strategyLoadContext == null)
			throw new Exception("This strategy can't be reloaded.");
		
		removeStrategy(strategyName);
		
		StringBuilder persistersNamesBuilder = new StringBuilder();
		String[] persitersNamesArray = strategyLoadContext.getPersisterNames();
		for (int i = 0; i < persitersNamesArray.length; i++ )
		{
			persistersNamesBuilder.append(persitersNamesArray[i]);
			if ((i + 1) < persitersNamesArray.length)
				persistersNamesBuilder.append(";");
		}		
		
		createStrategy(
				strategyLoadContext.getConfigFile(), 
				strategyLoadContext.isAutoStart(), 
				strategyLoadContext.getPushStrategyDataOnPort(), 
				persistersNamesBuilder.toString());
	}

	@Override
	public void createStrategy(String strategyFilePath, boolean isAutoRun, int pushStrategyOnPort,
			String persistersNames) throws Exception {

		String[] values = persistersNames.trim().split(";");

		for (int i = 0; i < values.length; i++)
			values[i] = values[i].trim();

		TradingStrategy strategy = ConfigProvider.configureStrategy(theService, strategyFilePath, isAutoRun,
				pushStrategyOnPort, values);

		if (isAutoRun)
			strategy.start();

		jmxProvider.registerStrategy(strategy);
		jmxProvider.registerExecutionProvider(strategy.getExecutionProvider());

		sendNotification(strategy.getName(), NotificationTypes.STRATEGY_CREATED);
	}

	@Override
	public void removeStrategy(String strategyName) throws Exception {

		TradingStrategy strategy = theService.getStrategy(strategyName);

		if (strategy == null)
			throw new Exception("Can't remove strategy (" + strategyName + ")");
		else {

			theService.unloadStrategy(strategy);

			jmxProvider.unregisterStrategy(strategy);
			jmxProvider.unregisterExecutionProvider(strategy.getExecutionProvider());

			sendNotification(strategy.getName(), NotificationTypes.STRATEGY_REMOVED);
		}
	}

	@Override
	public void addPersisters(String persisterFilePath) throws Exception {

		ConfigProvider.createPersistersFromXML(theService, persisterFilePath);
	}

	@Override
	public boolean isStarted() throws IOException {
		return theService.isStarted();
	}

	@Override
	public void start() {
		theService.start();
	}

	@Override
	public void stop() {
		theService.stop();
	}

	@Override
	public void reload() {		
		theService.reload();
	}

	private void sendNotification(String message, NotificationTypes type) {

		switch (type) {
			case STRATEGY_CREATED:
				broadcaster.sendNotification(new StrategyCreatedNotification(this, ++notificationSequence, message));
				break;
			case STRATEGY_REMOVED:
				broadcaster.sendNotification(new StrategyRemovedNotification(this, ++notificationSequence, message));
				break;
			default:
				break;
		}
	}

	@Override
	public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
			throws IllegalArgumentException {

		broadcaster.addNotificationListener(listener, filter, handback);
	}

	@Override
	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {

		broadcaster.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {

		return new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(new String[] { StrategyCreatedNotification.TYPE },
						StrategyCreatedNotification.class.getName(), "Strategy created."),
				new MBeanNotificationInfo(new String[] { StrategyRemovedNotification.TYPE },
						StrategyRemovedNotification.class.getName(), "Strategy removed.")};
	}
	
	public NotificationBroadcasterSupport getBroadcaster() {
		return broadcaster;
	}
}