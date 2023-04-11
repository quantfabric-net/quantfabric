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

public class TradingPermission extends MDEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8468489411856390395L;
	private boolean allowTrading;
	
	public TradingPermission()
	{
		this(false);
	}
	
	public TradingPermission(boolean allowTrading)
	{
		super();
		setAllowTrading(allowTrading);
	}

	public boolean isAllowTrading()
	{
		return allowTrading;
	}

	public void setAllowTrading(boolean allowTrading)
	{
		this.allowTrading = allowTrading;
	}
}
