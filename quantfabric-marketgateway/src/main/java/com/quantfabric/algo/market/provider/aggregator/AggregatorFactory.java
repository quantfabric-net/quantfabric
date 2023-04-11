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
package com.quantfabric.algo.market.provider.aggregator;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregatorFactory
{
	private static final AtomicInteger aggregatorsCount = new AtomicInteger(0);
	private static int getAggregatorNumber()
	{
		return aggregatorsCount.getAndIncrement();
	}
	
	public enum AggregatorTypes
	{
		TopMDQuote(TopMDQuoteAggregator.class),
		VWAP(VWAPAggregator.class),		
		ComplexMarketView(ComplexMarketViewAggregator.class),
		OrderBook(OrderBookAggregator.class),
		OHLC(OHLCAggregator.class),
		TradeOHLC(TradeOHLCAggregator.class),
		ComplexOHLC(ComplexOHLCAggregator.class);
		
		private final Class<? extends MarketViewAggregator> aggregatorClass;
		
		AggregatorTypes(Class<? extends MarketViewAggregator> aggregatorClass)
		{
			this.aggregatorClass = aggregatorClass;
		}
		
		public MarketViewAggregator createInstance(String name, Properties properties) 
				throws IllegalArgumentException, SecurityException, InstantiationException, 
					IllegalAccessException, InvocationTargetException, NoSuchMethodException
		{
			return crateAggregatorInstance(aggregatorClass, name, properties);
		}

		public Class<? extends MarketViewAggregator> getAggregatorClass()
		{
			return aggregatorClass;
		}	
	}
	
	public MarketViewAggregator createAggregator(AggregatorDefinition definition)
	{
		return createAggregator(definition.getAggregatorClass(), 
				definition.getAggregatorClass().getName() + getAggregatorNumber(), definition.getProperties());
	}
	
	public MarketViewAggregator createAggregator(AggregatorTypes type, String name, Properties properties)
	{
		return createAggregator(type.getAggregatorClass(), name, properties);
	}
	
	public MarketViewAggregator createAggregator(Class<? extends MarketViewAggregator> aggregatorClass, 
			String name, Properties properties)
	{
		try
		{
			return crateAggregatorInstance(aggregatorClass, name, properties);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}	
	}
	
	protected static MarketViewAggregator crateAggregatorInstance(
			Class<? extends MarketViewAggregator> aggregatorClass, 
			String name, Properties properties) 
					throws IllegalArgumentException, SecurityException, InstantiationException, 
						IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		return aggregatorClass.getConstructor(String.class, Properties.class).
				newInstance(name, properties);
	}
}
