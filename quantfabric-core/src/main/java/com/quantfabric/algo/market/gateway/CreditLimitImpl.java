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

import com.quantfabric.algo.instrument.Instrument;

public class CreditLimitImpl implements CreditLimit
{
	Instrument instrument;
	double limitValue;
	
	public CreditLimitImpl(Instrument instrument, double limitValue)
	{
		super();
		this.instrument = instrument;
		this.limitValue = limitValue;
	}
	
	public CreditLimitImpl(Instrument instrument)
	{
		this(instrument, 0D);
	}
	
	public double getLimitValue()
	{
		return limitValue;
	}
	public void setLimitValue(Double limitValue)
	{
		this.limitValue = limitValue;
	}
	public Instrument getInstrument()
	{
		return instrument;
	}
}
