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
package com.quantfabric.algo.cep.indicators.macd;

import com.quantfabric.algo.cep.indicators.util.movingwindow.MovingWindowExp;

public class MACDCalculator
{
	private final int fastEMAPeriod;
	private final int slowEMAPeriod;
	private final int signalEMAPeriod;
	
	private MovingWindowExp fastEMA;
	private MovingWindowExp slowEMA;	
	private MovingWindowExp signalEMA;
	
	private double fastEMAValue;
	private double slowEMAValue;
	private double macdValue;
	private double signalValue;
	private double divergenceValue;
	
	public MACDCalculator(int fastEMAPeriod, int slowEMAPeriod, int signalEMAPeriod)
	{
		this.fastEMAPeriod = fastEMAPeriod;
		this.slowEMAPeriod = slowEMAPeriod;
		this.signalEMAPeriod = signalEMAPeriod;
		
		resetCurrentState();
	}

	public int getFastEMAPeriod()
	{
		return fastEMAPeriod;
	}
	public int getSlowEMAPeriod()
	{
		return slowEMAPeriod;
	}
	public int getSignalEMAPeriod()
	{
		return signalEMAPeriod;
	}	
	
	public void addPrice(int price, boolean isClosedBar)
	{				
		synchronized (this)
		{
			if (isClosedBar)
			{
				fastEMA.add(price);
				fastEMAValue = fastEMA.getEMA();
				
				slowEMA.add(price);
				slowEMAValue = slowEMA.getEMA();
			}
			else
			{
				fastEMAValue = fastEMA.calc(price);
				slowEMAValue = slowEMA.calc(price);
			}
						
			macdValue = fastEMAValue != -1 && slowEMAValue != -1 ? fastEMAValue - slowEMAValue : -1;
			
			if (macdValue != -1)
			{
				if (isClosedBar)
				{
					signalEMA.add(macdValue);
					signalValue = signalEMA.getEMA();
				}
				else
					signalValue = signalEMA.calc(macdValue);
			
				if (signalValue != -1)
					divergenceValue = macdValue - signalValue;
			}			
		}
	}
	
	public MACDValue getCurrentValue()
	{
		if (signalValue == -1 || macdValue == -1)
			return new MACDValue(-1, -1, -1, -1, -1);
		
		return new MACDValue(fastEMAValue, slowEMAValue, macdValue, signalValue, divergenceValue);
	}
	
	public void resetCurrentState()
	{
		this.fastEMAValue = -1;
		this.slowEMAValue = -1;
		this.macdValue = -1;
		this.signalValue = -1;
		this.divergenceValue = -1;
		
		this.fastEMA = new MovingWindowExp(fastEMAPeriod);
		this.slowEMA = new MovingWindowExp(slowEMAPeriod);
		this.signalEMA = new MovingWindowExp(signalEMAPeriod);
	}
	
	public static void main (String[] args)
	{
		double[] input = {1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9};
		
		MACDCalculator mc = new MACDCalculator(6, 12, 4);
				
		for (double value : input)
		{
			mc.addPrice((int)value, true);
			System.out.println(value + " - " + mc.getCurrentValue().getFastEMA() + " - " + mc.getCurrentValue().getSlowEMA() + " - "   + mc.getCurrentValue().getMACD() + " - " + mc.getCurrentValue().getSignal() + " - " + mc.getCurrentValue().getDivergence());
		}
		/*System.out.println("---");
		for (int i=0; i < 10; i++)
		{
			System.out.println(i + " - " + mw.calc(i));
		}
		System.out.println("---");
		mw.add(9);
		System.out.println(9 + " - " + mw.getEMA());*/
	}
}
