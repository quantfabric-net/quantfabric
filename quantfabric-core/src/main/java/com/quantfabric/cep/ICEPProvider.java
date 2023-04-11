/**
 * 
 */
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
package com.quantfabric.cep;

import java.util.Map;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UpdateListener;
import com.quantfabric.cep.StatementDefinitionImpl.PersistModes;
import com.quantfabric.persistence.PersisterSettingsBlock;

/**
 * @author Constantin
 *
 */
public interface ICEPProvider 
{
	class CEPProviderException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8148090685943567964L;

		public CEPProviderException()
		{
			super();
		}

		public CEPProviderException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public CEPProviderException(String message)
		{
			super(message);
		}

		public CEPProviderException(Throwable cause)
		{
			super(cause);
		}
	}
 
    
    void addEventType(Class<?> type);
    void removeEventType(String type);
    void addNamedEventTypes(String namespace);
    void addVariable(String name, String type, Object value, boolean constant);
    void setVariableValue(String name, Object value);
    Object getVariableValue(String name);
    boolean isExistVariable(String name);
    void removeVariable(String name);
    EPStatement registerStatement(String statementID, String statement, boolean persist, boolean debug) throws CEPProviderException;
    EPStatement registerStatement(String statementID, String statement, PersistModes persistMode, boolean debug) throws CEPProviderException;
    EPStatement registerStatement(String statementID, String statement, PersistModes persistMode, Map<String, PersisterSettingsBlock> customPersistingSettingBlocks, boolean debug) throws CEPProviderException;

    void addListener(String statementID, UpdateListener listener);
    void setSubscriber(String statementID, Object subscriber);
    void setSubscriber(EPStatement statement, Object subscriber);
    void loadProcessModel();

    void sendEvent(Object event);
    void sendEvent(Map<?,?> event, String eventTypeName);
    
    void destroy();
}