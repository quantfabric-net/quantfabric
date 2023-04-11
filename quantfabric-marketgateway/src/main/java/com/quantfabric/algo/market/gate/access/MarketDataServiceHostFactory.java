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
package com.quantfabric.algo.market.gate.access;

import java.util.Properties;

public abstract class MarketDataServiceHostFactory
{
	public static class MarketDataServiceHostFactoryException extends Exception
	{
		private static final long serialVersionUID = -5950024892077954770L;

		public MarketDataServiceHostFactoryException(String message,
				Throwable cause)
		{
			super(message, cause);
		}

		public MarketDataServiceHostFactoryException(String message)
		{
			super(message);
		}		
	}
	
	public abstract MarketDataServiceHost createMarketDataServiceHost(String serviceHostName, Properties settings) 
			throws MarketDataServiceHostFactoryException;
}
