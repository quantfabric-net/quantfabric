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
package com.quantfabric.algo.trading.strategy.settings;

public class StrategySettingImpl implements StrategySetting
{
	private final String name;
	private final String type;
	private final Scope scope;
	private final ModificationMode modificationMode;
	private String value;
	private final String regionName;
	private final String displayName;
	private final String parametersViewId;
	private final String groupId;
	
	public StrategySettingImpl(String name, String type, Scope scope,
			ModificationMode modificationMode, String regionName,
			String displayName, String parametersViewId, String groupId)
	{
		this.name = name;
		this.type = type;
		this.scope = scope;
		this.modificationMode = modificationMode;
		this.regionName = regionName;
		this.displayName = displayName;
		this.parametersViewId = parametersViewId;
		this.groupId = groupId;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;		
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getType()
	{
		return type;
	}

	@Override
	public Scope getScope()
	{
		return scope;
	}

	@Override
	public ModificationMode getModificationMode()
	{
		return modificationMode;
	}

	@Override
	public String getRegionName()
	{
		return regionName;
	}

	@Override
	public String getGroupId() 
	{
		return groupId;
	}

	@Override
	public String getParametersViewId() 
	{
		return parametersViewId;
	}

	@Override
	public String getDisplayName()
	{
		return displayName;
	}
}
