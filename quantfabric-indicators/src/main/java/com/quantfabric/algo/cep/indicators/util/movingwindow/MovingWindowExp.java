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
package com.quantfabric.algo.cep.indicators.util.movingwindow;

public class MovingWindowExp extends MovingWindowMean
{
	private double ema = -1;
	
	private final double smoothingConstant;
	
	public MovingWindowExp(int period) {
		super(period);
		
		this.smoothingConstant = 2. / ((double)period + 1.);
	}
		
	@Override
	public void add(double value)
	{
		super.add(value);			
		ema = calc(value);
	}
	
	public double calc(double value)
	{
		double ema = this.ema;
		double prevEma = this.ema;
		
		if (isFull())
		{
			if (ema == -1) 
				ema = getMean();			
			else 		
			{
				ema = (smoothingConstant * value) + ((1. - smoothingConstant) * prevEma);
				
				//optimized??? 
				//ema = smoothingConstant * (value - prevEma) + prevEma; 
			}
		}
		return ema;		
	}
	
	public double getEMA()
	{
		return ema;
	}
	
	public static void main (String[] args)
	{
		double[] input = {1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8};
		
		MovingWindowExp mw = new MovingWindowExp(12);
		
		
		for (double value : input)
		{
			mw.add(value);
			System.out.println(value + " - " + mw.getEMA());
		}
		System.out.println("---");
		for (int i=0; i < 10; i++)
		{
			System.out.println(i + " - " + mw.calc(i));
		}
		System.out.println("---");
		mw.add(9);
		System.out.println(9 + " - " + mw.getEMA());
		
	}
}
