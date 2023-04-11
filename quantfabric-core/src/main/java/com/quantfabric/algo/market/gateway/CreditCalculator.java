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

import java.util.HashMap;
import java.util.Map;

import com.quantfabric.algo.instrument.Instrument;

public class CreditCalculator
{
	private final Map<Instrument, Double> creditValues = new HashMap<Instrument, Double>();
	
	public void loan(Instrument instrument, double value)
	{
		synchronized (creditValues)
		{
			if (!creditValues.containsKey(instrument))
				creditValues.put(instrument, value);
			else
				creditValues.put(instrument, creditValues.get(instrument) + value);
		}
	}
	
	public void cancel(Instrument instrument, double value)
	{
		synchronized (creditValues)
		{
			if (creditValues.containsKey(instrument))
				creditValues.put(instrument, creditValues.get(instrument) - value);
		}
	}
	
	public double getCreditValue(Instrument instrument)
	{
		synchronized (creditValues)
		{
			return creditValues.containsKey(instrument) ? creditValues.get(instrument) : 0;
		}
	}
}
