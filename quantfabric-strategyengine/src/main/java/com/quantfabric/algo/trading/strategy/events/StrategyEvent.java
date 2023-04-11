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
package com.quantfabric.algo.trading.strategy.events;

public class StrategyEvent
{
	private String strategyName;
	private String strategyId;
	private String text;
	
	public StrategyEvent()
	{
		this(null, null);
	}
	
	public StrategyEvent(String strategyName, String strategyId)
	{
		super();
		this.strategyName = strategyName;
		this.strategyId = strategyId;
	}

	public String getStrategyName()
	{
		return strategyName;
	}

	public void setStrategyName(String strategyName)
	{
		this.strategyName = strategyName;
	}
	
	public String getStrategyId()
	{
		return strategyId;
	}

	public void setStrategyId(String strategyId)
	{
		this.strategyId = strategyId;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}
}
