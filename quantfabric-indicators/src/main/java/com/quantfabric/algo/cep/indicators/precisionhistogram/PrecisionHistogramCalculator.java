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
package com.quantfabric.algo.cep.indicators.precisionhistogram;

import java.util.ArrayList;

import com.quantfabric.algo.cep.indicators.precisionhistogram.PrecisionHistogramValue.Trend;

public class PrecisionHistogramCalculator {
	
	private final int LENGTH = 21;
	private final int CYCLE = 4;
	private final int SLIDING_WINDOW_LENGTH = LENGTH * CYCLE + LENGTH -  1; //104

	private final double[] precisionCoeffs;
	private final double[] precisionCoeffsWeightedSums;
	
	private final ArrayList<Integer> prices;
	
	private double closedBarsLwma;
	private double lwma;
	private double currentPrecision;
	private double previousPrecision;
		
	private Trend trend;
	private int closedBarsCounter = 0;
	
	public PrecisionHistogramCalculator(){
		precisionCoeffs = new double[SLIDING_WINDOW_LENGTH];
		precisionCoeffsWeightedSums = new double[SLIDING_WINDOW_LENGTH];
		prices = new ArrayList<Integer>(SLIDING_WINDOW_LENGTH);
		initCoefincients();
		trend = Trend.NOTREND;
	}
	public void addPrice(long barId, int price, boolean isBarClosed) {
		//we are able to calculate anything only after 1-st bar has closed
		//so wait until close
		//isBarClosed = True indicates the duplicated last bar event
		if(isBarClosed)
		{
			//increment bars counter
			closedBarsCounter++;
			//recalculate lwma
			synchronized (this) {
				prices.add(price);
				//do prices shift if reached sliding window length
				if(closedBarsCounter >= SLIDING_WINDOW_LENGTH)
					prices.remove(0);
				//do calculate lwma only on closed bars	
			}
			calculateLwma();
			if(closedBarsCounter == 1)
			{
				previousPrecision = getFirstBarPrecisionValue();
			}
			else
			{	
				previousPrecision = currentPrecision;
			}
		}
		else
		{
			if(closedBarsCounter > 0)
			{
				//calculate
				//increase lwma with current bar price evaluated with lwma coefficient
				//get the index of coeff and coeff sum for the last bar
				int index = prices.size();
				lwma = price * precisionCoeffs[0] + closedBarsLwma;
				double coeffSum = precisionCoeffsWeightedSums[0];
				
				if(coeffSum > 0)
					currentPrecision = lwma / coeffSum;
				if(currentPrecision - previousPrecision > 0 )  trend = Trend.UPTREND;
				if(previousPrecision - currentPrecision > 0 )  trend = Trend.DOWNTREND;
			}	
		}
	}
	public Object getCurrentValue() {
		return new PrecisionHistogramValue(trend);
	}
	public void resetCurrentState(){
		
	}
	private void initCoefincients(){
		//not obvious variable names were used because 
		//of not clear understanding of what they meant in MT4 code
		double calcResult1, calcResult2, calcCosResult, coeffSum = 0;
		for(int i = 0; i < SLIDING_WINDOW_LENGTH; i++)
		{
			if( i < LENGTH - 1)
				calcResult1 = 1.0 * i / (LENGTH - 2);
			else
				calcResult1 = ( i - ( LENGTH - 1) + 1 ) * ( 2.0 * CYCLE - 1.0 ) / ( CYCLE * LENGTH - 1.0) + 1.0;
			
			calcCosResult = Math.cos( Math.PI * calcResult1 );
			calcResult2 = 1.0 / ( 3.0 * Math.PI * calcResult1 + 1.0 );
			
			if(calcResult1 <= 0.5) 
				calcResult2 = 1;
			precisionCoeffs[i] = calcResult2 * calcCosResult;
			coeffSum += precisionCoeffs[i]; 
			precisionCoeffsWeightedSums[i] = coeffSum;
		}
	}
	private void calculateLwma(){
		int size = prices.size();
		closedBarsLwma = 0;
		int coefIndex;
		synchronized (this) {
			for(int i = size - 1; i >= 0; i--)
			{
				coefIndex = size - i;
	 			closedBarsLwma += prices.get(i) * precisionCoeffs[coefIndex];
			}	
		}
	}
	private double getFirstBarPrecisionValue(){
		if(precisionCoeffsWeightedSums[0] > 0)
			return closedBarsLwma / precisionCoeffsWeightedSums[0];
		else 
			return 0;
	}
}
