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
package com.quantfabric.algo.cep.indicators.util;

import java.util.Vector;

public class SimpleMovingAverage {
	private final int period;
	private final Vector<Integer> prices;
	private int average;
	
	public SimpleMovingAverage(int period)
	{
		this.period = period;
		prices = new Vector<Integer>(period);
	}
	public void addPrice(int price)
	{
		if(prices.size() == period)
			prices.removeElementAt(0);
		prices.add(price);
		countAverage();
	}
	public int getResult()
	{
		return average;
	}
	private void countAverage()
	{
		int sum = 0;
		int size = prices.size();
		for(int i=0; i < size; i++)
		{
			sum += prices.get(i);
		}
		if(prices.size() == period)
			average = sum / period;
		else
			average = sum / size;
	}
	public int getPeriod() {
		return period;
	}
	public int getMaxValue()
	{
		int maxValue = prices.get(0);
		int nextValue;
		int size = prices.size();
		for(int i=1; i < size; i++)
		{
			nextValue = prices.get(i);
			if(nextValue > maxValue)
				maxValue = nextValue;
		}
		return maxValue;
	}
}
