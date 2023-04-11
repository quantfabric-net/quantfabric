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
package com.quantfabric.algo.instrument;

import java.math.BigDecimal;

public class InstrumentImpl implements Instrument
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5820559830392476463L;
	public static final String DEFAULT_ID = null;
	public static final String DEFAULT_BASE = null;
	public static final String DEFAULT_LOCAL = null;
	
	private static String composeSymbol(String instrumetId, String base, String local)
	{
		if (instrumetId != null)
		{
			int id = Integer.parseInt(instrumetId);
			if (id >= 100 && id < 1000) //CME instruments range
				if (base != null && local != null)
					return base + local;
				else
					return null;
			else if (id >= 1000 && id < 1100) //VirtualCoin instruments
				if (base != null && local != null)
					return base + "_" + local;
				else
					return null;
			else
			{		
				if (base != null && local != null)
					return base + "/" + local;
				else
					return null;
			}
		}
		
		return null;
	}
	
	private String base;
	private String local;
	private String id;
	private String symbol;
	
	private int pointsInOne;	
	
	public InstrumentImpl(String base, String local, int pointsInOne)
	{
		this(DEFAULT_ID, base, local, pointsInOne);
	}
	
	public InstrumentImpl(String id, String base, String local, int pointsInOne)
	{
		this.id = id;
		this.base = base;
		this.local = local;
		this.symbol = composeSymbol(id, base, local);
		this.pointsInOne = pointsInOne;
	}
	
	public InstrumentImpl()
	{
		this(DEFAULT_BASE, DEFAULT_LOCAL, 0);
	}
	
	private synchronized void setSymbol(String symbol)
	{
		this.symbol = symbol;
	}
	
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}

	public String getBase()
	{
		return base;
	}
	
	public void setBase(String base)
	{
		this.base = base;
		setSymbol(composeSymbol(id, base, this.local));
	}
	
	public String getLocal()
	{
		return local;
	}

	public void setLocal(String local) 
	{
		this.local = local;
		setSymbol(composeSymbol(id, this.base, local));
	}
	
	public String getSymbol()
	{
		return symbol;
	}
	
	public int getPointsInOne()
	{
		return pointsInOne;
	}

	public void setPointsInOne(int pipValue)
	{
		this.pointsInOne = pipValue;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && this.getClass() == obj.getClass())
		{
			InstrumentImpl instrument = (InstrumentImpl)obj;
			return base.equals(instrument.getBase())
				&& local.equals(instrument.getLocal());
		}
		else
			return false;
	}

	@Override
	public int hashCode()
	{		
		return base.hashCode() * local.hashCode();
	}	

	@Override
	public long castToLong(double price)
	{
		return (long)(price * getPointsInOne());
	}

	@Override
	public long castToLong(BigDecimal price) {
		return price.multiply(new BigDecimal(getPointsInOne())).longValueExact();
	}



	@Override
	public double castToDecimal(long price)
	{
		return (double)price / (double)getPointsInOne();
	}
}
