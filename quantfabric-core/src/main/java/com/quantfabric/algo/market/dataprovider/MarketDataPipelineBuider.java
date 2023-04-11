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

import java.util.Collection;

import org.w3c.dom.Node;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;

public interface MarketDataPipelineBuider
{
	class MarketDataPipelineBuiderException extends Exception
	{
		private static final long serialVersionUID = 8418072846189880584L;

		public MarketDataPipelineBuiderException()
		{
			super();
		}
		public MarketDataPipelineBuiderException(String message, Throwable cause)
		{
			super(message, cause);
		}
		public MarketDataPipelineBuiderException(String message)
		{
			super(message);
		}
		public MarketDataPipelineBuiderException(Throwable cause)
		{
			super(cause);
		}
	}
	
	MarketDataPipeline buildPipeline(Node piplelineConfigRoot, 
			MarketGateway marketGateway, 
			Configuration cepConfig,
			Collection<PersistingUpdateListenerConfig> persisterConfigs) throws Exception;
}
