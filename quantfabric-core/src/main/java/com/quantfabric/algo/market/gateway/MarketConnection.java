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

import java.util.Collection;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.FeedProvider;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

public interface MarketConnection 
	extends 
		CommandExecutor, 
		MarketFeeder,
		InstrumentProvider,
		FeedProvider,
		OrderBookSnapshotsProvider,
		LoanCancelProvider
{	
	enum MarketConnectionMode
	{
		BASIC,
		BACK_TESTING,
		BITCOIN_BACK_TESTING
	}
	
	interface StatusChangedListener
	{
		boolean onStatusChanged(StatusChanged event);
	}
	
	String getName();
	Collection<MarketDataFeed> getMarketDataFeeds();
	Collection<ExecutionFeed> getExecutionFeeds();
	boolean isConnected();
	void setLogonPassword(String password) throws MarketConnectionException;
	void connect() throws MarketConnectionException;
	void disconnect() throws MarketConnectionException;
	MarketConnectionMode getMode();
	void addStatusChangedListener(StatusChangedListener listener);
	void setCreditLimit(Instrument instrument, double limitValue);
	Collection<CreditLimit> getCreditLimits();
	CreditLimit getCreditLimit(Instrument instrument);
	CreditCalculator getCreditCalculator();
}
	
	
