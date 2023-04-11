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
package com.quantfabric.algo.trading.strategyrunner;

public class StrategyLoadContext
{
	private String strategyName;	
	private String configFile;
	private boolean isAutoStart; 
	private int pushStrategyDataOnPort;
	private String[] persisterNames;
	
	public StrategyLoadContext()
	{
		this(null, null, false, 0, null);
	}
	
	public StrategyLoadContext(String strategyName, String configFile,
			boolean isAutoStart, int pushStrategyDataOnPort,
			String[] persisterNames)
	{
		super();
		this.strategyName = strategyName;
		this.configFile = configFile;
		this.isAutoStart = isAutoStart;
		this.pushStrategyDataOnPort = pushStrategyDataOnPort;
		this.persisterNames = persisterNames;
	}
	
	public String getStrategyName()
	{
		return strategyName;
	}
	public String getConfigFile()
	{
		return configFile;
	}
	public boolean isAutoStart()
	{
		return isAutoStart;
	}
	public int getPushStrategyDataOnPort()
	{
		return pushStrategyDataOnPort;
	}
	public String[] getPersisterNames()
	{
		return persisterNames;
	}
	public void setStrategyName(String strategyName)
	{
		this.strategyName = strategyName;
	}
	public void setConfigFile(String configFile)
	{
		this.configFile = configFile;
	}
	public void setAutoStart(boolean isAutoStart)
	{
		this.isAutoStart = isAutoStart;
	}
	public void setPushStrategyDataOnPort(int pushStrategyDataOnPort)
	{
		this.pushStrategyDataOnPort = pushStrategyDataOnPort;
	}
	public void setPersisterNames(String[] persisterNames)
	{
		this.persisterNames = persisterNames;
	}
}
