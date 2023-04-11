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
package com.quantfabric.algo.market.gateway;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.quantfabric.algo.backtesting.player.MDPTask;
import com.quantfabric.algo.market.gateway.MarketConnection.MarketConnectionMode;

public class MarketConnectionSettings implements Serializable {
	
	public static final boolean DEFAULT_AUTO_CONNECT = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = -7088542942581546826L;
	
	private String provider = null;
	private final Properties settings = new Properties();
	private final Properties credentials = new Properties();
	
	private String marketDataStorageProviderClassName = null;
	private final Properties marketDataStorageProviderSettings = new Properties();
	
	private MarketConnectionMode mode = MarketConnectionMode.BASIC;
	
	private final List<MDPTask> marketDataPlayerPlaylist = new LinkedList<MDPTask>();
	
	private boolean autoConnect = DEFAULT_AUTO_CONNECT;
	
	public boolean isAutoConnect()
	{
		return autoConnect;
	}

	public void setAutoConnect(boolean autoConnect)
	{
		this.autoConnect = autoConnect;
	}

	public List<MDPTask> getMarketDataPlayerPlaylist()
	{
		return marketDataPlayerPlaylist;
	}
		
	public MarketConnectionMode getMode()
	{
		return mode;
	}
	public void setMode(MarketConnectionMode mode)
	{
		this.mode = mode;
	}
	
	public String getMarketDataStorageProviderClassName()
	{
		return marketDataStorageProviderClassName;
	}
	public void setMarketDataStorageProviderClassName(
			String marketDataStorageProviderClassName)
	{
		this.marketDataStorageProviderClassName = marketDataStorageProviderClassName;
	}
	
	public void addMarketDataStorageProviderSetting(String name, Object value)
	{
		marketDataStorageProviderSettings.put(name, value);
	}
			
	public Properties getMarketDataStorageProviderSettings()
	{
		return marketDataStorageProviderSettings;
	}
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public Properties getSettings() {
		return settings;
	}
	public Properties getCredentials() {
		return credentials;
	}
	
	public void addSetting(String name, Object value)
	{
		settings.put(name, value);
	}
	
	public void addCredential(String name, Object value)
	{
		credentials.put(name, value);
	}
}
