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
package com.quantfabric.algo.market.dataprovider;

import java.util.List;
import java.util.Set;

import com.quantfabric.cep.StatementDefinition;

public interface DataView
{
	String getCompatibleVesion();
	boolean isStatic();
	//public abstract void setCompatibleVesion(String compatibleVesion);
    String getName();
	String getEventType();
	//public abstract void setEventType(String eventType);
    String getPrefix();
	//public abstract void setPrefix(String prefix);
    List<StatementDefinition> getStatements();
	Set<String> getDependences();
	Set<String> getParameters();

}