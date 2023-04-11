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
package com.quantfabric.algo.trading.strategy;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Set;

import com.quantfabric.cep.StatementDefinitionImpl;


public class StrategyEpStatement extends StatementDefinitionImpl implements Serializable  
{
	private static final long serialVersionUID = -6912986892305028171L;

	public enum OnStrategyStartAction
	{
		NONE,
		CLEAR_TABLE
	}
	
	private final boolean isExecInvoker;
	private Set<OnStrategyStartAction> onStrategyStartActions;
	
	private final boolean isContextCreator;

	@ConstructorProperties({"name", "statement","persistMode","executionEventProvider","debugMode", "contextCreator"})
	public StrategyEpStatement(String name, String statement,
			PersistModes persistMode, boolean isExecInvoker,boolean debug, boolean isContextCreator) {
		super(name, statement, persistMode,debug);
		this.isExecInvoker = isExecInvoker;
		this.isContextCreator = isContextCreator;
	}

	/**
	 * @return the executionEventProvider
	 */
	public boolean isExecutionEventProvider() {
		return isExecInvoker;
	}
	
	public boolean isContextCreator() {
		
		return isContextCreator;
	}

	public Set<OnStrategyStartAction> getOnStrategyStartActions()
	{
		return onStrategyStartActions;
	}

	public void setOnStrategyStartActions(
			Set<OnStrategyStartAction> onStrategyStartActions)
	{
		this.onStrategyStartActions = onStrategyStartActions;
	}
}
