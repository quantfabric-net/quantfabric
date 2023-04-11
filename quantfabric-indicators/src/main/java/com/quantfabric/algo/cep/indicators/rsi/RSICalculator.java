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
package com.quantfabric.algo.cep.indicators.rsi;

import java.util.ArrayList;
import java.util.List;


public class RSICalculator
{
    private Double previousClose = null;
    private Double lastClose = null;
    private double stableAvgGain;
    private double stableAvgLoss;
    private double rs;
    private double rsi;
    private int period = 0;
    private final List<Double> lstAvgGain = new ArrayList<Double>();
    private final List<Double> lstAvgLoss = new ArrayList<Double>();
    private boolean firstStageCompleted = false;
    				
	public RSICalculator(int period)
    {
        this.period = period;
    }
	
    public void addPrice(double price)
    {    	   		
    	previousClose = lastClose;
    	lastClose = price;
        calculate(price);
    }
    
    private static class ChangeValue
    {	 
    	double change = 0;
        double gain = 0;
        double loss = 0;       
    }		
    
    private static ChangeValue calcPriceChange(double previousClose, double price) 
    {
    	ChangeValue cv = new ChangeValue();
    	cv.change = price - previousClose;
    	cv.gain = cv.change > 0 ? cv.change : 0;
    	cv.loss = cv.change < 0 ? - cv.change  : 0;
    	
    	return cv;
    }
    
    private void calculate(double price)
    {
        if (previousClose == null)
            return;
        
        ChangeValue cv = calcPriceChange(previousClose, price);
        
        if (lstAvgGain.size() < period && !firstStageCompleted)
        {
            lstAvgGain.add(cv.gain);
            lstAvgLoss.add(cv.loss);
        }
        if (lstAvgGain.size() == period && !firstStageCompleted)
        {
            stableAvgGain = calcListAverage(lstAvgGain);
            stableAvgLoss = calcListAverage(lstAvgLoss);
            firstStageCompleted = true;
            calculateRSI(stableAvgGain, stableAvgLoss);
        }
        else if (firstStageCompleted)
        {
            stableAvgGain = (stableAvgGain * (period - 1) + cv.gain) / period;
            stableAvgLoss = (stableAvgLoss * (period - 1) + cv.loss) /period;
            calculateRSI(stableAvgGain, stableAvgLoss);
        }
        
    }
    
	public void updatePrice(int price)
	{
		if (firstStageCompleted && previousClose != null)
		{
	        ChangeValue cv = calcPriceChange(previousClose, price);
	        
	        double tAvgGain = (stableAvgGain * (period - 1) + cv.gain) / period;
	        double tAvgLoss = (stableAvgLoss * (period - 1) + cv.loss) /period;
	        
	        calculateRSI(tAvgGain, tAvgLoss);
		}
		
	}  
    	
    private void calculateRSI(double avgGain,  double avgLoss)
    {
        rs = avgGain / avgLoss;
        rsi = avgLoss == 0 ? 100 : 100 - (100 /( 1 + rs));
    }
   
    public boolean isResultsValid()
    {
    	return firstStageCompleted;
    }
    
    public RSIValue getCurrentValue()    
    {
    	return new RSIValue(rs, rsi);
    }
    
    public void resetCurrentState()
	{
    	lstAvgGain.clear();
    	lstAvgLoss.clear();
    	firstStageCompleted = false;
    	rs = 0.;
    	rsi = 0.;
    	stableAvgGain = 0.;
    	stableAvgLoss = 0.;
    	previousClose = null;
	}
    
    private static double calcListAverage(List<Double> list) 
    {
    	double sum =0;
		for(int i=0; i < list.size(); i++)   
        {  
            sum += list.get(i);  
        }  
        return sum/list.size();
    } 
}