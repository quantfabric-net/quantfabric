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
package com.quantfabric.algo.market.datamodel;

import com.quantfabric.algo.market.gateway.MarketConnection.MarketConnectionMode;

public abstract class MarketConnectionAlert extends MDEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7398690786920268111L;
	private String connectionName;
	private MarketConnectionMode connectionMode;
	
	public MarketConnectionAlert()
	{
		super();
	}
	
	public MarketConnectionAlert(String connectionName, MarketConnectionMode connectionMode)
	{
		super();
		setConnectionName(connectionName);
		setConnectionMode(connectionMode);
	}

	public String getConnectionName()
	{
		return connectionName;
	}

	public void setConnectionName(String connectionName)
	{
		this.connectionName = connectionName;
	}

	public MarketConnectionMode getConnectionMode()
	{
		return connectionMode;
	}

	public void setConnectionMode(MarketConnectionMode connectionMode)
	{
		this.connectionMode = connectionMode;
	}	
}
