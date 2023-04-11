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

import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.trading.strategy.settings.StrategySetting;


public interface StrategyDefinition {
	boolean DEFAULT_ENABLE = false;
	
	String getName();
	void setName(String name);
	String getType();
	void setType(String type);
	String getDescription();
	void setDescription(String desc);
	Map<String, String> getSettingValues();
	Map<String, StrategySetting> getSettings();
	String getSettingsLayoutDefinition();
	void setSettingValue(String name, String value);
	Set<ExecutionPoint> getExecutionEndPoints();
	Set<DataSink> getDataSinks();
	Set<StrategyEpStatement> getStrategyStatements();
	boolean isEnabled();
	void setEnabled(boolean isEnabled);
	boolean isRunning();
	boolean isPlugged();
	boolean isExecutionAllowed();
	void setExecutionAllowed(boolean isExecutionAllowed);
	void setStrategyDataStreamPort(int port);
	int getStrategyDataStreamPort();
}
