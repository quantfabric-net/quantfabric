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


public interface StrategyConfiguration {

	void addSetting(String name, String value, String type, String scope, String modificationMode, String regionName, String displayName, String parametersViewId, String groupId);
	void removeSetting(String name);
	ExecutionPoint addExecutionPoint(ExecutionPoint point);
	void removeExecutionPoint(String point);
	DataSink addDataSink(DataSink sink);
	void removeDataSink(String sinkName);
    void addStatement(StrategyEpStatement definition);
    void removeStatement(String name);
}
