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
package com.quantfabric.algo.cep.indicators.precisionhistogram;

public class PrecisionHistogramValue
{
	public enum Trend {UPTREND, DOWNTREND, NOTREND}

    private Trend trend;
	
	public PrecisionHistogramValue()
	{
		this.trend = Trend.NOTREND;
	}
		
	public PrecisionHistogramValue(Trend trend)
	{
		super();
		this.trend = trend;
	}

	public Trend getTrend()
	{
		return trend;
	}
	public void setTrend(Trend value)
	{
		this.trend = value;
	}
	@Override
	public String toString() {
		return "PrecisionHistogramValue [trend=" + trend + "]";
	}
}
