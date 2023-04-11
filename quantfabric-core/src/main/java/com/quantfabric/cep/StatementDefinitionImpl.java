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

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

import com.quantfabric.persistence.PersisterSettingsBlock;

public class StatementDefinitionImpl implements StatementDefinition {

	public enum PersistModes
	{
		NONE,
		NEW,
		APPEND
	}
	
	public static final PersistModes DEFAULT_PERSIST_MODE = PersistModes.APPEND;
	
	private String name;
	private String statement;
	private boolean debugMode;
	private PersistModes persistMode;
	
	private final Map<String, PersisterSettingsBlock> persisterCustomSettingBlocks;

	public StatementDefinitionImpl()
	{
		this("", "", false);
	}
	
	@ConstructorProperties({"name", "statement", "persisted"})
	public StatementDefinitionImpl(String name, String statement, boolean persisted)
	{
		this(name, statement, 
				persisted ? DEFAULT_PERSIST_MODE : PersistModes.NONE);
	}
	@ConstructorProperties({"name", "statement", "persistMode"})
	public StatementDefinitionImpl(String name, String statement, PersistModes persistMode)
	{
		this(name, statement, persistMode, false);
	}
	@ConstructorProperties({"name", "statement", "persisted", "debug"})
	public StatementDefinitionImpl(String name, String statement, boolean persisted, boolean debug)
	{
		this(name, 
			statement, 
			persisted ? DEFAULT_PERSIST_MODE : PersistModes.NONE,
			debug);
	}
	@ConstructorProperties({"name", "statement", "persistMode", "debug"})
	public StatementDefinitionImpl(String name, String statement, 
			PersistModes persistMode,
			boolean debug)
	{
		this(name, 
			statement, 
			persistMode,
			new HashMap<String, PersisterSettingsBlock>(),
			debug);
	}
	@ConstructorProperties({"name", "statement", "persistMode", "persisterCustomSettingBlocks", "debug"})
	public StatementDefinitionImpl(String name, String statement, 
			PersistModes persistMode, 
			Map<String, PersisterSettingsBlock> persisterCustomSettingBlocks,
			boolean debug)
	{
		this.statement = statement;
		this.name = name;
		this.persistMode = persistMode;
		this.persisterCustomSettingBlocks = persisterCustomSettingBlocks;
		this.debugMode=debug;
	}

	
	
	
	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#getPersistMode()
	 */
	public PersistModes getPersistMode()
	{
		return persistMode;
	}
	
	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#setPersistMode(com.quantfabric.cep.StatementDefinition.PersistModes)
	 */
	public void setPersistMode(PersistModes persistMode)
	{
		this.persistMode = persistMode;
	}
	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#getStatement()
	 */
	public String getStatement() {
		return statement;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#setStatement(java.lang.String)
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#isDebugMode()
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/* (non-Javadoc)
	 * @see com.quantfabric.cep.IStatementDefinition#setDebugMode(boolean)
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	@Override
	public int hashCode() {
		int h = 0;
		int n= statement.length();
		for (int i = 0; i < n; i++) {
		    h = 31*h + statement.charAt(i);
		}
		return h;
	}

	@Override
	public String toString()
	{
		return getStatement();
	}

	public void setPersisterCustomSettingsBlock(String blockName,
			PersisterSettingsBlock settingsBlock)
	{
		this.persisterCustomSettingBlocks.put(blockName, settingsBlock);		
	}

	
	public Map<String, PersisterSettingsBlock> getPersisterCustomSettingBlocks()
	{
		return this.persisterCustomSettingBlocks;
	}	
}