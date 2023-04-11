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

public class StrategySettingChangedEvent extends StrategyEvent
{
	private String settingName;
	private String settingTypeName;
	private Object previousValue;
	private Object newValue;
	
	public StrategySettingChangedEvent()
	{
		this(null, null, null, null, null, null);
	}
	
	public StrategySettingChangedEvent(String strategyName, String strategyId, String settingName, String settingTypeName,
			Object previousValue, Object newValue)
	{
		super(strategyName, strategyId);
		this.settingName = settingName;
		this.settingTypeName = settingTypeName;
		this.previousValue = previousValue;
		this.newValue = newValue;
	}

	public String getSettingName()
	{
		return settingName;
	}

	public void setSettingName(String settingName)
	{
		this.settingName = settingName;
	}

	public String getSettingTypeName()
	{
		return settingTypeName;
	}

	public void setSettingTypeName(String settingTypeName)
	{
		this.settingTypeName = settingTypeName;
	}

	public Object getPreviousValue()
	{
		return previousValue;
	}

	public void setPreviousValue(Object previousValue)
	{
		this.previousValue = previousValue;
	}

	public Object getNewValue()
	{
		return newValue;
	}

	public void setNewValue(Object newValue)
	{
		this.newValue = newValue;
	}
}
