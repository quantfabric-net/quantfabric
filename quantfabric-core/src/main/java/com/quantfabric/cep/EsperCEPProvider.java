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
package com.quantfabric.cep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.cep.StatementDefinitionImpl.PersistModes;
import com.quantfabric.persistence.PersisterSettingsBlock;
import com.quantfabric.persistence.StorageProvider.StoragingException;
import com.quantfabric.persistence.esper.PersistingUpdateListener;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;

public class EsperCEPProvider implements ICEPProvider
{
	private static final Logger log = LoggerFactory.getLogger(EsperCEPProvider.class);
	
	private final String uri;
	private final EPAdministrator epAdministrator;
	private final EPServiceProvider epService;
	private final EPRuntime epRuntime;

	private final UpdateConsoleWriter updateConsoleWriter;

	private final Collection<PersistingUpdateListenerConfig> persisterConfigs;

	private final List<PersistingUpdateListener> createdPersisters = new ArrayList<>();
	
	private static Configuration addDefaultSettings(Configuration configuration)
	{
		configuration.addImport("com.quantfabric.algo.market.datamodel.*");
		configuration.addImport("com.quantfabric.algo.order.*");
		configuration.addImport("java.lang.*");
		configuration.addImport("java.math.*");
		configuration.addImport("java.text.*");
		configuration.addImport("java.util.*");
		configuration.getEngineDefaults().getExpression().setUdfCache(false);
		
		return configuration;
	}
	
	public EsperCEPProvider(String URI, Configuration config)
	{
		this(URI, config, new ArrayList<>());
	}	
		
	public EsperCEPProvider(String URI, Configuration config,
			Collection<PersistingUpdateListenerConfig> persisterConfigs)
	{
		this(EPServiceProviderManager.getProvider(URI, 
				(config == null ? addDefaultSettings(new Configuration()) :  addDefaultSettings(config))),
				persisterConfigs);
	}

	public EsperCEPProvider(EPServiceProvider epServiceProvider)
	{
		this(epServiceProvider, new ArrayList<>());
	}
	
	public EsperCEPProvider(EPServiceProvider epServiceProvider, 
			Collection<PersistingUpdateListenerConfig> persisterConfigs)
	{
		this.uri = epServiceProvider.getURI();
		this.persisterConfigs = persisterConfigs;		
		this.epService = epServiceProvider;		
		this.epAdministrator = this.epService.getEPAdministrator();
		this.updateConsoleWriter = new UpdateConsoleWriter();
		
		createdPersisters.add(updateConsoleWriter);
		
		this.epRuntime = this.epService.getEPRuntime();
	}

	@Override
	public void destroy()	
	{
		epAdministrator.stopAllStatements();
		epService.destroy();
		
		for (PersistingUpdateListener persister : createdPersisters)
			try
			{
				persister.dispose();
			}
			catch (StoragingException e)
			{
				log.error("Error during persister disposing.", e);
			}
		
		createdPersisters.clear();
	}
	
	public void addPersisterConfig(PersistingUpdateListenerConfig config)
	{
		persisterConfigs.add(config);
	}
	
	public EPStatement registerStatement(String statementID, String statement,
			boolean persist, boolean debug) throws CEPProviderException
	{
		return registerStatement(statementID, statement, 
				persist ? StatementDefinitionImpl.DEFAULT_PERSIST_MODE : PersistModes.NONE,
				debug);
	}

	public EPStatement registerStatement(String statementID, String statement,
			PersistModes persistMode, boolean debug)
			throws CEPProviderException
	{
		return registerStatement(statementID, statement, persistMode, null, debug);
	}
	
	public EPStatement registerStatement(String statementID, String statement,
			PersistModes persistMode, 
			Map<String, PersisterSettingsBlock> customPersistingSettingBlocks,
			boolean debug)
			throws CEPProviderException
	{
		String statementName;
		
		if (statementID != null && !statementID.isEmpty())
			statementName = statementID;
		else
			statementName = String.valueOf(statement.hashCode());
		
		EPStatement stmt = epAdministrator.getStatement(statementName);		

		if (stmt == null || !stmt.getText().equals(statement))
		{
			try
			{
				stmt = epAdministrator.createEPL(statement, statementName);
			}
			catch (Exception ex)
			{
				String errMsg = "can't register statement - " + statementName;
				log.error(errMsg, ex);
				throw new CEPProviderException(errMsg, ex);
			}
			if (debug) stmt.addListener(updateConsoleWriter);
		}
		else
		{
			String errMsg = "Statement (" + statementName + ") already registered";
			log.error(errMsg);
			throw new CEPProviderException(errMsg);
		}		
		log.info("Statement (" + statementName + ") was registered");
		return stmt;
	}

	public void addListener(String statementID, UpdateListener listener)
	{
		if (listener == null)
			throw new IllegalArgumentException("listener is missing");
		EPStatement stmt = epAdministrator.getStatement(statementID);
		if (stmt == null)
			throw new QuantfabricCEPException(String.format(
					"Can't set listener, the statement '%s' doesn't exist",
					statementID));
		try
		{
			stmt.addListener(listener);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException(String.format("Set listener failed %s", ex));
		}
	}

	public void setSubscriber(String statementID, Object subscriber)
	{
		if (subscriber == null)
			throw new IllegalArgumentException("subscriber is missing");
		EPStatement stmt = epAdministrator.getStatement(statementID);
		if (stmt == null)
			throw new QuantfabricCEPException(String.format(
					"Can't set subscriber, the statement '%s' doesn't exist",
					statementID));
		try
		{
			stmt.setSubscriber(subscriber);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException(String.format("Set subscriber failed %s",
					ex));
		}

	}

	public void setSubscriber(EPStatement statement, Object subscriber)
	{
		if (subscriber == null)
			throw new IllegalArgumentException("subscriber is missing");
		try
		{
			statement.setSubscriber(subscriber);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException(String.format("Set subscriber failed%s",
					ex));
		}
	}

	public void addEventType(Class<?> type)
	{
		try
		{
			epAdministrator.getConfiguration().addEventType(type);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't add event type '" +
					type + "'", ex);
		}

	}

	public void removeEventType(String type)
	{
		try
		{
			epAdministrator.getConfiguration().removeEventType(type, true);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't remove event type '" +
					type + "'", ex);
		}

	}

	public void addNamedEventTypes(String namespace)
	{
		try
		{
			epAdministrator.getConfiguration().addEventTypeAutoName(namespace);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't add event types from namespace '" +
					namespace +
					"'", ex);
		}

	}

	public void addVariable(String name, String type, Object value, boolean constant)
	{

		try
		{
			epAdministrator.getConfiguration().addVariable(name, type, value, constant);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't create variable name '" +
					name +
					"' type '" + type + "' init value '" +
					value + "'", ex);

		}
	}
	
	@Override
	public void setVariableValue(String name, Object value)
	{		
		try
		{			
			epRuntime.setVariableValue(name, value);	
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't change variable '" +
					name +
					"' value '" +
					value + "'", ex);

		}
	}
	
	@Override
	public boolean isExistVariable(String name)
	{
		return epRuntime.getVariableValueAll().containsKey(name);
	}

	@Override
	public Object getVariableValue(String name)
	{
		try
		{
			return epRuntime.getVariableValue(name);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("Unknown variable : " + name);
		}
	}

	public void removeVariable(String name)
	{
		try
		{
			epAdministrator.getConfiguration().removeVariable(name, true);
		}
		catch (Exception ex)
		{
			throw new QuantfabricCEPException("can't remove variable name '" +
					name + "'", ex);
		}
	}

	public void loadProcessModel()
	{

	}

	public void sendEvent(Object event)
	{
		epRuntime.sendEvent(event);
	}

	@SuppressWarnings("rawtypes")
	public void sendEvent(Map event, String eventTypeName)
	{
		epRuntime.sendEvent(event, eventTypeName);
	}

	public String getUri()
	{
		return uri;
	}

}
