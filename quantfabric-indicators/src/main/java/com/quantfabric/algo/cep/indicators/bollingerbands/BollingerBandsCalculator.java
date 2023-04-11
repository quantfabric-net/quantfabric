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
package com.quantfabric.algo.cep.indicators.bollingerbands;

import com.quantfabric.algo.cep.indicators.util.movingwindow.MovingWindowStDev;

public class BollingerBandsCalculator
{
	private final int multiple;
	
	private final MovingWindowStDev values;
	
	private double ma = 0.;
	private double sigma = 0.;
	private double upperBand = 0.;
	private double lowerBand = 0.;
	
	public BollingerBandsCalculator(int period, int multiple)
	{
		this.multiple = multiple;		
		this.values = new MovingWindowStDev(period);
	}
		
	public void addValue(double value)
	{
		values.add(value);
		
		if(values.isFull())
		{
			ma = values.getMean();			
			sigma = values.getStdev();
			
			upperBand = (ma + (multiple * sigma));
			lowerBand = (ma - (multiple * sigma));
		}
	}
	
	public void restCurrentValue()
	{
		values.clear();
		ma = 0.;
		upperBand = 0;
		lowerBand = 0;
	}
	
	public BollingerBandsValue getCurrentValue()
	{
		return new BollingerBandsValue(ma, upperBand, lowerBand);
	}
	
	
}
