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
package com.quantfabric.algo.market.connector.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Message;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.connector.QFixMarketAdapter;
import com.quantfabric.algo.market.connector.QFixMarketAdapter.TypeConverter.ConversionError;

public abstract class SendQFixMessage implements ConcreteCommand
{
	private static final Logger log = LoggerFactory.getLogger(SendQFixMessage.class);
	
	@Override
	public void execute(CommandExecutor commandExecutor)
	{
		try
		{
			QFixMarketAdapter marketAdapter = ((QFixMarketAdapter)commandExecutor);
			Message message = getMessage(marketAdapter);
			marketAdapter.sendMessage(message);
			onSend(marketAdapter, message);
		}
		catch (ConversionError e)
		{
			log.error("can't convert : " + this, e);
			e.printStackTrace();			
		}
		catch  (Exception e)
		{
			log.error("Can't execute command.", e);
		}
	}
	
	protected abstract Message getMessage(QFixMarketAdapter marketAdapter) throws Exception;
	protected void onSend(QFixMarketAdapter marketAdapter, Message message)
	{		
	}
}
