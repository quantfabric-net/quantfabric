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
package com.quantfabric.algo.market.gateway;

import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.gateway.feed.FeedProvider;

public interface MarketAdapter 
	extends 
		MarketFeeder, 
		CommandExecutor,
		OrderBookSnapshotsProvider
{
	enum AdapterStatus
	{
		CONNECTED,
		DISCONNECTED
	}
	
	interface LogonListener
	{
		void loggedin(MarketAdapter sender);
		void logout(MarketAdapter sender);
	}
	
	class MarketAdapterException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 119891092003840520L;

		public MarketAdapterException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public MarketAdapterException(String message)
		{
			super(message);
		}

		public MarketAdapterException(Throwable cause)
		{
			super(cause);
		}
	}
	
	void logon() throws MarketAdapterException;
	void logout() throws MarketAdapterException;

	void setPassword(String password) throws MarketAdapterException;
	
	void addLogonListerner(LogonListener logonListener);
	void removeLogonListerner(LogonListener logonListener);
	
	AdapterStatus getStatus();
	
	void setFeedProvider(FeedProvider feedProvider);
	
	String getVenueName();
	
	void setMarketDataCacheProvider(
			MarketDataCacheProvider marketDataCacheProvider);
	
	void setInstrumentProvider(InstrumentProvider instrumentProvider);
}
