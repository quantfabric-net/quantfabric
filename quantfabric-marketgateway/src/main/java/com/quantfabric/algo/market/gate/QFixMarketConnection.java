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

import java.io.File;
import java.util.Properties;

import com.quantfabric.algo.market.gateway.MarketAdapter;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.MarketConnectionImp;
import com.quantfabric.algo.market.gateway.MarketGateway;
import quickfix.ConfigError;
import quickfix.SessionSettings;

import com.quantfabric.algo.market.gateway.MarketConnectionImp.MarketConnectionConfigException.ConfigPropertyIssue;
import com.quantfabric.algo.runtime.QuantfabricRuntime;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;

public abstract class QFixMarketConnection extends MarketConnectionImp
{
	protected QFixMarketConnection(
			MarketGateway parent,
			String name, 
			Properties adapterSettings,
			Properties credentials) throws MarketConnectionException
	{
		super(parent, name, adapterSettings, credentials);
	}

	@Override
	protected void initializeAdapter(Properties adapterSettings, Properties credentials) throws MarketConnectionConfigException
	{
		String settingsUrl = null;
		boolean logFixMessage = false;
		
		if (adapterSettings.containsKey(CONFIG_URL))
			settingsUrl = adapterSettings.get(CONFIG_URL).toString();
		else
			throw new MarketConnectionConfigException(ConfigPropertyIssue.NOT_SPECIFIED, CONFIG_URL);
	
		if (adapterSettings.containsKey(LOG_MESSAGES))
			logFixMessage = Boolean.parseBoolean(adapterSettings.get(LOG_MESSAGES).toString());
		
		try
		{
			String extSettingsFilePath = QuantfabricRuntime.getAbsolutePath(settingsUrl);
			if (!new File(extSettingsFilePath).exists())
				throw new MarketConnectionConfigException(ConfigPropertyIssue.EXTERNAL_RESOURCE_ISNOT_EXISTS, extSettingsFilePath);
			
			SessionSettings sessionSettings = new SessionSettings(extSettingsFilePath);
			if (credentials.containsKey(PASSWORD))
				sessionSettings.setString("Password", credentials.getProperty(PASSWORD));
			if (credentials.containsKey(USERNAME))
				sessionSettings.setString("Username", credentials.getProperty(USERNAME));
			if (credentials.containsKey(ACCOUNT))
				sessionSettings.setString("Account", credentials.getProperty(ACCOUNT));
			
			this.setMarketAdapter(createMarketAdapter(sessionSettings, logFixMessage));
		}
		catch (ConfigError e)
		{
			throw new MarketConnectionConfigException(e);
		}		
	}
	
	protected abstract MarketAdapter createMarketAdapter(
			SessionSettings sessionSettings, 
			boolean logFixMessages) 
		throws ConfigError;

}
