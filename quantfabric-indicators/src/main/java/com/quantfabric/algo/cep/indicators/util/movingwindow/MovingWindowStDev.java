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

public class MovingWindowStDev extends MovingWindow
{
	private double sum, sumSquared;

	public MovingWindowStDev(int size)
	{
		super(size);
	}

	@Override
	public void add(double value)
	{
		double oldest = getFirst();
		sum = sum + value - oldest;
		sumSquared = sumSquared + (value * value) - (oldest * oldest);
		super.add(value);
	}

	public double getMean()
	{
		return sum / getCapacity();
	}

	public double getStdev()
	{
		int capacity = getCapacity();
		double num = capacity * sumSquared - (sum * sum);
		double denom = capacity * (capacity - 1);
		return Math.sqrt(num / denom);
	}

	@Override
	public void clear()
	{
		sum = sumSquared = 0;
		super.clear();
	}
}
