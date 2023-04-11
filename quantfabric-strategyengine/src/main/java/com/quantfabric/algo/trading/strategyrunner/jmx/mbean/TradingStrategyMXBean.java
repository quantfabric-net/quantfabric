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
package com.quantfabric.algo.trading.strategyrunner.jmx.mbean;

import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.trading.strategy.DataSinkInfo;
import com.quantfabric.algo.trading.strategy.ExecutionPoint;
import com.quantfabric.algo.trading.strategy.ManualTradingProvider;
import com.quantfabric.algo.trading.strategy.StrategyEpStatement;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting;
import com.quantfabric.util.Startable;

public interface TradingStrategyMXBean extends Startable, ManualTradingProvider 
{
	String getName();
	String getType();
	String getDescription();
	void setDescription(String desc);
	
	Map<String,StrategySetting> getSettings();	
	Map<String,String> getSettingValues();	
	void setSettingValue(String name, String value);
	String getSettingsLayoutDefinition();
	boolean saveSettingsLayoutDefinitionToFile(String path);
	
	Set<ExecutionPoint> getExecutionEndPoints();
	Set<DataSinkInfo> getDataSinks();
	Set<StrategyEpStatement> getStrategyStatements();
	boolean isEnabled();
	void setEnabled(boolean isEnabled);
	boolean isRunning();
	boolean isPlugged();
	boolean isExecutionAllowed();
	void setExecutionAllowed(boolean isExecutionAllowed);
	int getStrategyDataStreamPort();
}
