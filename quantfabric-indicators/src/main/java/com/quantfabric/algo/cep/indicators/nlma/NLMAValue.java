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
package com.quantfabric.algo.cep.indicators.nlma;

public class NLMAValue
{
	public enum Trend {UPTREND, DOWNTREND, NOTREND}

    private Trend trend;
	private int value;
	
	public NLMAValue()
	{
		this.trend = Trend.NOTREND;
		this.value = 0;
	}
		
	public NLMAValue(int value, Trend trend)
	{
		super();
		this.trend = trend;
		this.value = value;
	}

	public Trend getTrend()
	{
		return trend;
	}
	public void setTrend(Trend trend)
	{
		this.trend = trend;
	}

	public int getValue()
	{
		return value;
	}
	public void setValue(int value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return " trend=" + trend + "; value=" + value + ";";
	}		
}
