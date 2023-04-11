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

import java.io.IOException;
import java.util.Map;


public interface StrategyLoadRunnerMXBean {
	
	Map<String, String> getGlobalSettings() throws IOException;
	//public ObjectName[] getTradingStrategies() throws IOException;
    boolean isStarted() throws IOException;
	
	void setGlobalSetting(String key,String value) throws IOException;
	void createStrategy(String filePath, boolean isAutoRun, int pushStrategyOnPort, String persistersNames) throws Exception;
	void removeStrategy(String strategyName) throws Exception;
	void addPersisters(String filePath) throws Exception;
	void start();
	void stop();
	void reload();
	void reloadStrategy(String strategyName) throws Exception;
}
