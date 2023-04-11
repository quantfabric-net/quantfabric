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
package com.quantfabric.algo.trading.strategy.events;

public class StrategyStateChangedEvent extends StrategyEvent 
{
	private boolean isRunning;
	private boolean isEnabled;
	private boolean isExecutionAllowed;
		
	public StrategyStateChangedEvent()
	{
		this(null, null, false, false, false);
	}
	
	public StrategyStateChangedEvent(String strategyName, String strategyId, boolean isEnabled, boolean isRunning, boolean isExecutionAllowed)
	{
		super(strategyName, strategyId);
		this.isEnabled = isEnabled;
		this.isRunning = isRunning;
		this.isExecutionAllowed = isExecutionAllowed;
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public void setRunning(boolean isRunning)
	{
		this.isRunning = isRunning;
	}

	public boolean isEnabled()
	{
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled)
	{
		this.isEnabled = isEnabled;
	}

	public boolean isExecutionAllowed()
	{
		return isExecutionAllowed;
	}

	public void setExecutionAllowed(boolean isExecutionAllowed)
	{
		this.isExecutionAllowed = isExecutionAllowed;
	}	
}
