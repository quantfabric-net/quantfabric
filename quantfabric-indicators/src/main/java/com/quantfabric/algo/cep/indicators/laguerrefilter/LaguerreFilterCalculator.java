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
package com.quantfabric.algo.cep.indicators.laguerrefilter;

public class LaguerreFilterCalculator
{
	private static final double NOT_INITIALIZED = -1;
	
	private double gamma;
	private double currentValue;

	double currentL0;
	double currentL1;
	double currentL2;
	double currentL3;
	
	double previousL0 = NOT_INITIALIZED;
	double previousL1 = NOT_INITIALIZED;
	double previousL2 = NOT_INITIALIZED;
	double previousL3 = NOT_INITIALIZED;

	boolean previousValuesIsInitialized = false;
	
	public LaguerreFilterCalculator()
	{

	}

	public void calculate(long barId, int closePrice, boolean isBarClosed)
	{
		if (!previousValuesIsInitialized)
		{
			previousL0 = closePrice;
			previousL1 = closePrice;
			previousL2 = closePrice;
			previousL3 = closePrice;
			
 			previousValuesIsInitialized = true;
		}
				
		if (!isBarClosed)
		{		
			currentL0 = (1.0 - gamma) * closePrice + gamma * previousL0;
			currentL1 = -gamma * currentL0 + previousL0 + gamma * previousL1;
			currentL2 = -gamma * currentL1 + previousL1 + gamma * previousL2;
			currentL3 = -gamma * currentL2 + previousL2 + gamma * previousL3;
	
			double CU = 0, CD = 0;
			
			if (currentL0 >= currentL1)
				CU = currentL0 - currentL1;
			else
				CD = currentL1 - currentL0;
	
			if (currentL1 >= currentL2)
				CU = CU + currentL1 - currentL2;
			else
				CD = CD + currentL2 - currentL1;
	
			if (currentL2 >= currentL3)
				CU = CU + currentL2 - currentL3;
			else
				CD = CD + currentL3 - currentL2;
	
			if (CU + CD != 0)
			{
				currentValue = (currentL0 + 2 * currentL1 + 2 * currentL2 + currentL3) / 6.0;
			}
		}
		else
		{
			previousL0 = currentL0;
			previousL1 = currentL1;
			previousL2 = currentL2;
			previousL3 = currentL3;
		}
	}

	public LaguerreFilterValue getCurrentValue()
	{
		return new LaguerreFilterValue(currentValue);
	}

	public void setGamma(double gamma)
	{
		this.gamma = gamma;
	}
}