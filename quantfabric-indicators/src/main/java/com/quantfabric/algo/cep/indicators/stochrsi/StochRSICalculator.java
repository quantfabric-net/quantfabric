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
package com.quantfabric.algo.cep.indicators.stochrsi;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.quantfabric.algo.cep.indicators.rsi.RSICalculator;
import com.quantfabric.util.BoundedFifoHashMap;

public class StochRSICalculator
{
	private final int rPeriod;
	private final int kPeriod;
	private final int dPeriod;
	private final int slowing;
	
	private final RSICalculator rsiCalculator;
		
	private final Map<Long, Double> rsiValues;
	private final Map<Long, Double> minRsiValues;
	private final Map<Long, Double> maxRsiValues;
	
	private double currentMainValue;
	private double sumMainValuesForMa;
	private double currentSignalValue;
	
	private final CircularFifoBuffer mainValuesHistory;
	
	public StochRSICalculator(int rPeriod, int kPeriod, int dPeriod, int slowing)
	{
		super();
		this.rPeriod = rPeriod;
		this.kPeriod = kPeriod;
		this.dPeriod = dPeriod;
		this.slowing = slowing;
		
		this.rsiCalculator = new RSICalculator(this.rPeriod);
		this.rsiValues = new BoundedFifoHashMap<Long, Double>(this.kPeriod);
		this.minRsiValues = new BoundedFifoHashMap<Long, Double>(this.kPeriod);
		this.maxRsiValues = new BoundedFifoHashMap<Long, Double>(this.kPeriod);
		
		this.mainValuesHistory = new CircularFifoBuffer(dPeriod);
	}
		
	public StochRSIValue getCurrentValue()
	{
		return new StochRSIValue(currentMainValue, currentSignalValue);
	}
	
	public void resetCurrentValue()
	{
		rsiCalculator.resetCurrentState();
	}
	
	
	public void addPrice(long barId, int price, boolean isClosedBar)
	{
		double removingMainValue = 0;
	    
	    if (mainValuesHistory.isFull())
	    	removingMainValue = (Double)(mainValuesHistory.toArray()[0]);
				
		if (isClosedBar)
			rsiCalculator.addPrice(price);	
		else
		{			
			rsiCalculator.updatePrice(price);
			
			rsiValues.put(barId, rsiCalculator.getCurrentValue().getRsi());
				
			minRsiValues.put(barId, Collections.min(rsiValues.values())); 
			maxRsiValues.put(barId, Collections.max(rsiValues.values()));
	
			double sumlow = 0.0;
		    double sumhigh = 0.0;
		    
		    if (rsiValues.size() < slowing)
		    	return;
		    
		    Double[] rsi = rsiValues.values().toArray(new Double[] {});
		    Double[] lowes = minRsiValues.values().toArray(new Double[] {});
		    Double[] highes = maxRsiValues.values().toArray(new Double[] {});
		    		    
		    for (int i = rsiValues.size() - slowing; i < rsiValues.size(); i++)
		    {
		    	sumlow += rsi[i] - lowes[i];
		    	sumhigh += highes[i] - lowes[i];
		    }
		    
		    if(sumhigh == 0.0) 
		    	currentMainValue = 100.0;
		    else 
		    	currentMainValue = sumlow / sumhigh * 100;
					    			    
		    currentSignalValue = (sumMainValuesForMa - removingMainValue + currentMainValue) / (double)dPeriod;	
		}
		
		if (isClosedBar)
		{
			mainValuesHistory.add(currentMainValue);	    
			sumMainValuesForMa -= removingMainValue; 
			sumMainValuesForMa += currentMainValue;
		}	
	}
}
