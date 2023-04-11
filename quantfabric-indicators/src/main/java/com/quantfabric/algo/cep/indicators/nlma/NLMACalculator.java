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

import java.util.Arrays;

import com.quantfabric.algo.cep.indicators.nlma.NLMAValue.Trend;
import com.quantfabric.util.BoundedFifoHashMap;

public class NLMACalculator
{
	private static final int DEFAULT_CYCLE = 4;
	
	private int period;
	
	private int length;
	private int phase;
	private final int cycle = DEFAULT_CYCLE;
	
	private double coefficient;
	private double beta;
	private double t;
	private double weight;
	private double g;
		
	//private double[] alfa;
	
	private double deviation;
	private double pctFilter;
	
	private final BoundedFifoHashMap<Long, Double> prices;
	
	private double sum = 0;
	
	private double previousBarValue;
	private double currentValue;
	
	private Trend previousBarTrend = Trend.NOTREND;
	private Trend currentTrend;
	
	public NLMACalculator(int period)
	{
		super();
		this.period = period;
		
		calcCoefficients();
		
		this.prices = new BoundedFifoHashMap<Long, Double>(length);
	}
	
	public void setPeriod(int period)
	{
		
			if (this.period != period)
			{
				synchronized (this)
				{
					this.period = period;
					calcCoefficients();		
					this.prices.setMaxSize(this.length);
				}
			}
	}

	public void calcCoefficients()
	{
		synchronized (this)
		{
			coefficient = 3 * Math.PI;
			phase = period - 1;
			length = period * cycle + phase;
		}
	}
	
	public void addPrice(long barId, int price, long openSourceTimestamp, boolean isClosedBar)
	{				
		synchronized (this)
		{
			if (isClosedBar)
			{			
				previousBarValue = currentValue;
				previousBarTrend = currentTrend;
			}
			else
			{
				prices.put(barId, (double)price);		
					
				Double[] pricesArray = new Double[length];		
				Double[] availablePricesArray =	prices.values().toArray(new Double[]{});
				Arrays.fill(pricesArray, availablePricesArray[0]);
				
				for (int i = availablePricesArray.length - 1, j = pricesArray.length - 1; j >=0 && i >= 0; i--, j--)
					pricesArray[j] = availablePricesArray[i];
						
				weight=0; sum=0; t=0;
				
				for (int i = 0; i < length; i++)
				{			
					g = 1. / (coefficient * t + 1.);
					
					if (t <= 0.5)
						g = 1;
					
					beta = Math.cos(Math.PI * t);
					
					double alfa = g * beta;
					
					int priceIndex = length - i - 1;
					
					sum += pricesArray[priceIndex] * alfa;	
					weight += alfa;
					
					if ( t < 1 ) 
						t += 1.0 / (double)(phase - 1);
			        else 
			        	if ( t < length - 1 )  
			        		t += (double)(2 * cycle- 1) / (double)(cycle * period -1);
				}
				
				if (weight > 0)
					currentValue = (1. + deviation / 100.) * sum / weight;
						
				if (pctFilter > 0)
				{
					if (Math.abs(currentValue - previousBarValue) < pctFilter)
						currentValue = previousBarValue;
				}		
				
				currentTrend = previousBarTrend;
						
				if (previousBarValue != 0)	
					if ((currentValue - previousBarValue) > pctFilter)
						currentTrend = Trend.UPTREND;
					else
						if ((previousBarValue - currentValue) > pctFilter)
							currentTrend = Trend.DOWNTREND;					
			}
		}
	}
	
	public void setDeviation(double deviation)
	{
		this.deviation = deviation;
	}
	
	public void setPctFilter(double pctFilter)	
	{
		this.pctFilter = pctFilter;
	}
	
	public NLMAValue getCurrentValue()
	{
		return new NLMAValue((int)(currentValue), currentTrend);
	}
	
	public void resetCurrentState()
	{
		previousBarValue = 0;
		currentValue = 0;
		previousBarTrend = Trend.NOTREND;
		currentTrend = Trend.NOTREND;
	}
}
