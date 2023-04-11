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
package com.quantfabric.algo.market.gate.jmx;


import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.gate.jmx.mbean.InstrumentMBean;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketConnectionMBean;
import com.quantfabric.algo.market.gateway.CreditLimit;
import com.quantfabric.algo.market.gateway.MarketConnection;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeedImpl;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeedImpl;


public class MarketConnectionMgmt implements MarketConnectionMBean {

	private final MarketConnection connection;
	private final MGatewayJMXProvider jmxProvider;
	
	public MarketConnectionMgmt(MGatewayJMXProvider jmxProvider, MarketConnection connection){
		this.connection = connection;
		this.jmxProvider = jmxProvider;
	}

	@Override
	public String getName()
	{
		return connection.getName();
	}
	@Override
	public String getIdentifier() {
		return connection.getIdentifier();
	}
	@Override
	public void connect() throws MarketConnectionException
	{
		connection.connect();		
	}

	@Override
	public void disconnect() throws MarketConnectionException
	{
		connection.disconnect();		
	}

	@Override
	public ObjectName createMarketDataFeed(String name, ObjectName instrument)
	{ 	
		MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();

        String instrumentId =
			JMX.newMBeanProxy(mbs, instrument, InstrumentMBean.class).getId();
		
		MarketDataFeed mdf = new MarketDataFeedImpl(new FeedNameImpl(name), connection, instrumentId, false);
		connection.addFeed(mdf);
		
		return jmxProvider.registerMarketDataFeed(connection, mdf);
	}
	
	@Override
	public ObjectName createExecutionFeed(String name)
	{ 		
		ExecutionFeed ef = new ExecutionFeedImpl(new FeedNameImpl(name));
		connection.addFeed(ef);
		
		return jmxProvider.registerExecutionFeed(connection, ef);
	}

	@Override
	public boolean isConnected()
	{
		return connection.isConnected();
	}

	@Override
	public void enterLogonPassword(String password) throws MarketConnectionException
	{
		connection.setLogonPassword(password);
	}

	@Override
	public boolean isTradingMarketConnection()
	{
		return !connection.getExecutionFeeds().isEmpty();
	}

	@Override
	public String getDisplayName()
	{
		return connection.getName();
	}

	@Override
	public Set<String> getCreditLimitedInstruments()
	{		
		Set<String> limitedInstruments = new TreeSet<String>();
		for (CreditLimit cl : connection.getCreditLimits())
			limitedInstruments.add(cl.getInstrument().getId());
		
		return limitedInstruments;
	}

	@Override
	public double getInstrumentCreditLimit(String instrumentId)
	{
		return connection.getCreditLimit(connection.getInstrument(instrumentId)).getLimitValue();
	}

	@Override
	public void setInstrumentCreditLimit(String instrumentId, double creditLimit)
	{
		connection.setCreditLimit(connection.getInstrument(instrumentId), creditLimit);		
	}

	@Override
	public double getCurrentCreditValue(String instrumentId)
	{
		return connection.getCreditCalculator().getCreditValue(connection.getInstrument(instrumentId));
	}

	@Override
	public int getMode()
	{
		return connection.getMode().ordinal();
	}

	@Override
	public Collection<ObjectName> getMarketDataFeeds()
	{
		return jmxProvider.getMarketDataFeeds(connection);
	}

	@Override
	public Collection<ObjectName> getExecutionFeeds()
	{
		return jmxProvider.getExecutionFeeds(connection);
	}

	
}
