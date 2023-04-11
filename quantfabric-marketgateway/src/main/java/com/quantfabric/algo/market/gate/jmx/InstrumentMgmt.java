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
package com.quantfabric.algo.market.gate.jmx;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.gate.jmx.mbean.InstrumentMBean;

import java.math.BigDecimal;

public class InstrumentMgmt implements InstrumentMBean
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5224876159220459625L;
	private final Instrument instrument;
	
	public InstrumentMgmt(Instrument instrument)
	{
		this.instrument = instrument;
	}

	@Override
	public String getId()
	{
		return instrument.getId();
	}

	@Override
	public String getBase()
	{
		return instrument.getBase();
	}

	@Override
	public String getLocal()
	{
		return instrument.getLocal();
	}

	@Override
	public String getSymbol()
	{
		return instrument.getSymbol();
	}

	@Override
	public int getPointsInOne()
	{		
		return instrument.getPointsInOne();
	}

	@Override
	public long castToLong(double price)
	{
		return instrument.castToLong(price);
	}

	@Override
	public long castToLong(BigDecimal price) {
		return instrument.castToLong(price);
	}

	@Override
	public double castToDecimal(long price)
	{
		return instrument.castToDecimal(price);
	}

}
