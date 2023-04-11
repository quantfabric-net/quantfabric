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
package com.quantfabric.algo.cep.indicators.cycleidentifier;

import java.util.ArrayList;

import com.quantfabric.algo.cep.indicators.util.SimpleMovingAverage;


public class CycleIdentifierCalculator {

    private final int range = 0;
    private int barsCounter;
    private int currentClosePrice = 0;
    private int minorCycleBuyPrice = 0;
    private int minorCycleSellPrice = 0;
    private int majorCycleBuyPrice = 0;
    private int majorCycleSellPrice = 0;

    private int minorCyclePosShift = 0;
    private int majorCyclePosShift = 0;

    private long minorCycleBarId = 0;
    private long majorCycleBarId = 0;

    private long currentBarId = 0;
    private long previousBarId = 0;
    private long lastConfirmedMinorCIBarId = 0;
    private long lastConfirmedMajorCIBarId = 0;

    private long outputMinorCiBarId = 0;
    private long outputMajorCiBarId = 0;

    private int lastConfirmedCiMinorValue = CImode.NOCYCLE;
    private int lastConfirmedCiMajorValue = CImode.NOCYCLE;

    private int lastActiveCiMinorValue = CImode.NOCYCLE;
    private int lastActiveCiMajorValue = CImode.NOCYCLE;

    private final ArrayList<Integer> firstBarHighPrices = new ArrayList<Integer>();
    private final ArrayList<Integer> firstBarLowPrices = new ArrayList<Integer>();

    private int SweepA;
    private int SweepB;
    private int SweepMinor;
    private int SweepMajor;

    private enum CheckBuyOrSell {BUY, SELL, NONE}

	private CheckBuyOrSell checkMinorBuyOrSell = CheckBuyOrSell.NONE;
    private CheckBuyOrSell checkMajorBuyOrSell = CheckBuyOrSell.NONE;

    private boolean updateMinorCiParams = false;
    private boolean updateMajorCiParams = false;
    private boolean cyclePricesAreInitialized = false;

    private boolean confirmedMinorCi = false;
    private boolean confirmedMajorCi = false;

    private int majorCycleMultiplier;
    private final int minShiftAfterCiActivation = 1;

    private final SimpleMovingAverage sma;
    private int averageBarMultiplier = 1;
    private int maxBarSize;
    private int percentileBarInclude;

    public CycleIdentifierCalculator(int smaPeriod, int averageBarMultiplier, int majorCycleMultiplier, int percentileBarInclude) {
        sma = new SimpleMovingAverage(smaPeriod);
        this.setMajorCycleMultiplier(majorCycleMultiplier);
        this.setAverageBarMultiplier(averageBarMultiplier);
        this.setPercentileBarInclude(percentileBarInclude);
    }

    public void calculate(long barId, int closePrice, int highPrice, int lowPrice, boolean isBarClosed) {
        currentBarId = barId;
        //ohlc option : send last bar close price on a close event of bar,
        //so don't consider this closePrice once again as we did at previous tick
        if (!isBarClosed)
            currentClosePrice = closePrice;

        if (isBarClosed && previousBarId != currentBarId) {
            previousBarId = currentBarId;
            int highLowDifference = highPrice - lowPrice;
            boolean allowAddPrice = true;
            if (barsCounter == sma.getPeriod()) {
                maxBarSize = sma.getMaxValue();
            }
            if (maxBarSize != 0 && highLowDifference != 0) {
                if (maxBarSize / highLowDifference < 100 / percentileBarInclude)
                    allowAddPrice = false;
            }
            if (maxBarSize == 0 || allowAddPrice)
                sma.addPrice(highLowDifference);
            SetSweeps();
            //init cycle prices only after 2-nd bar has closed
            if (!cyclePricesAreInitialized && barsCounter == 2)
                InitCyclePrices();
            barsCounter++;
        }
        //keep in memory first bar high and low to get range value after first bar has closed

        //begin calculation
        if (barsCounter > 2) {
            if (checkMinorBuyOrSell == CheckBuyOrSell.BUY || checkMinorBuyOrSell == CheckBuyOrSell.NONE) {
                //if(checkBuyFactor(minorCyclePrice))
                if (checkBuyFactor(minorCycleBuyPrice)) {
                    //check if the Bar where CI was activated has closed state so we can validate it with condition confirmation
                    //remember, that we can not validate and release the CI status as confirmed on current Bar because close price
                    //changes with every tick on current bar
                    //so if lastActiveCycleBarId != currentBarId - assume that CI bar is closed. Do validate the CI.

                    if (lastActiveCiMinorValue == CImode.NOCYCLE) {
                        lastActiveCiMinorValue = CImode.MINORBUY;
                        outputMinorCiBarId = minorCycleBarId;
                        minorCyclePosShift = barsCounter - 1;
                        SweepMinor = SweepA;
                    }

                    if (isBarClosed) {
                        boolean condition = (currentClosePrice - minorCycleBuyPrice) >= SweepA;
                        if (condition && barsCounter - 1 - minorCyclePosShift >= minShiftAfterCiActivation) {
                            checkMinorBuyOrSell = CheckBuyOrSell.SELL;
                            minorCycleSellPrice = currentClosePrice;
                            lastConfirmedCiMinorValue = CImode.MINORBUY;
                            updateMinorCiParams = true;
                            lastConfirmedMinorCIBarId = minorCycleBarId;
                            confirmedMinorCi = true;
                            minorCycleBarId = currentBarId;
                            minorCyclePosShift = 0;
                        }
                    }
                } else {
                    //if(isBarClosed)
                    //{
                    if (isBarClosed) {
                        minorCycleBuyPrice = currentClosePrice;
                        minorCycleBarId = currentBarId;
                    }
                    minorCyclePosShift = barsCounter - 1;
                    if (lastActiveCiMinorValue == CImode.MINORBUY) {
                        //outputMinorCiValue = true;
                        lastActiveCiMinorValue = CImode.NOCYCLE;
                    }
                    //}
                    //if we were spinning within attempts to confirm minor CI before we lost it - send NOCYCLE
                    //if(lastActiveCiMinorValue == CImode.MINORBUY && lastActiveMinorCIBarId != currentBarId)
                    //{
						/*lastActiveCiMinorValue = CImode.NOCYCLE;
						outputMinorCiValue = true;
						updateMinorCiParams = true;
						minorCyclePrice = currentCyclePrice;*/
                    //}
                }
            } else if (checkMinorBuyOrSell == CheckBuyOrSell.SELL || checkMinorBuyOrSell == CheckBuyOrSell.NONE) {
                if (checkSellFactor(minorCycleSellPrice)) {
                    if (lastActiveCiMinorValue == CImode.NOCYCLE) {
                        lastActiveCiMinorValue = CImode.MINORSELL;
                        outputMinorCiBarId = minorCycleBarId;
                        minorCyclePosShift = barsCounter - 1;
                        SweepMinor = SweepA;
                        //lastActiveMinorCIBarId = currentBarId;
                        //outputMinorCiValue = true;
                    }
                    if (isBarClosed) {
                        boolean condition = (minorCycleSellPrice - currentClosePrice) >= SweepA;
                        if (condition && barsCounter - 1 - minorCyclePosShift >= minShiftAfterCiActivation) {
                            checkMinorBuyOrSell = CheckBuyOrSell.BUY;
                            minorCycleBuyPrice = currentClosePrice;
                            lastConfirmedCiMinorValue = CImode.MINORSELL;
                            updateMinorCiParams = true;
                            lastConfirmedMinorCIBarId = minorCycleBarId;
                            confirmedMinorCi = true;
                            //outputMinorCiValue = true;
                            minorCycleBarId = currentBarId;
                            minorCyclePosShift = 0;
                        }
                    }
					/*if(lastActiveCiMinorValue == CImode.NOCYCLE)
					{
						lastActiveCiMinorValue = CImode.MINORSELL;
						lastActiveMinorCIBarId = currentBarId;
						outputMinorCiValue = true;
					}
					if(lastActiveMinorCIBarId != currentBarId)
					{
						boolean condition = (minorCyclePrice - currentCyclePrice ) >= SweepA;
						if(condition && barsCounter - lastActiveMinorCIBarId >= minShiftAfterCiActivation)
						{
							checkMinorBuyOrSell = CheckBuyOrSell.BUY;
							minorCyclePrice = currentCyclePrice;
							lastConfirmedCiMinorValue = CImode.MINORSELL;
							updateMinorCiParams = true;
							lastConfirmedMinorCIBarId = lastActiveMinorCIBarId;
							confirmedMinorCi = true;
							outputMinorCiValue = true;
						}
					}*/
                } else {
                    if (isBarClosed) {
                        minorCycleSellPrice = currentClosePrice;
                        minorCycleBarId = currentBarId;
                    }
                    minorCyclePosShift = barsCounter - 1;
                    if (lastActiveCiMinorValue == CImode.MINORSELL) {
                        //outputMinorCiValue = true;
                        lastActiveCiMinorValue = CImode.NOCYCLE;
                    }
					/*if(lastActiveCiMinorValue == CImode.MINORSELL && lastActiveMinorCIBarId != currentBarId)
					{
						lastActiveCiMinorValue = CImode.NOCYCLE;
						outputMinorCiValue = true;
						updateMinorCiParams = true;
					}*/
                }
            }
            if (checkMajorBuyOrSell == CheckBuyOrSell.BUY || checkMajorBuyOrSell == CheckBuyOrSell.NONE) {

                if (checkBuyFactor(majorCycleBuyPrice)) {
                    if (lastActiveCiMajorValue == CImode.NOCYCLE) {
                        lastActiveCiMajorValue = CImode.MAJORBUY;
                        outputMajorCiBarId = majorCycleBarId;
                        majorCyclePosShift = barsCounter - 1;
                        SweepMajor = SweepB;
                        //lastActiveMinorCIBarId = currentBarId;
                        //outputMajorCiValue = true;
                    }
                    if (isBarClosed) {
                        boolean condition = (currentClosePrice - majorCycleBuyPrice) >= SweepB;
                        if (condition && barsCounter - 1 - majorCyclePosShift >= minShiftAfterCiActivation) {
                            checkMajorBuyOrSell = CheckBuyOrSell.SELL;
                            majorCycleSellPrice = currentClosePrice;
                            lastConfirmedCiMajorValue = CImode.MAJORBUY;
                            updateMajorCiParams = true;
                            lastConfirmedMajorCIBarId = majorCycleBarId;
                            confirmedMajorCi = true;
                            //outputMajorCiValue = true;
                            majorCycleBarId = currentBarId;
                            majorCyclePosShift = 0;
                        }
                    }
					/*if(lastActiveCiMajorValue == CImode.NOCYCLE)
					{
						lastActiveCiMajorValue = CImode.MAJORBUY;
						lastActiveMajorCIBarId = currentBarId;
						outputMajorCiValue = true;
					}
					if(lastActiveMajorCIBarId != currentBarId)
					{
						boolean condition = (currentCyclePrice - majorCyclePrice) >= SweepB;
						if(condition && barsCounter - lastActiveMajorCIBarId >= minShiftAfterCiActivation)
						{
							checkMajorBuyOrSell = CheckBuyOrSell.SELL;
							majorCyclePrice = currentCyclePrice;
							lastConfirmedCiMajorValue = CImode.MAJORBUY;
							updateMajorCiParams = true;
							lastConfirmedMajorCIBarId = lastActiveMajorCIBarId;
							confirmedMajorCi = true;
							outputMajorCiValue = true;
						}
					}*/
                } else {
                    if (isBarClosed) {
                        majorCycleBuyPrice = currentClosePrice;
                        majorCycleBarId = currentBarId;
                    }
                    majorCyclePosShift = barsCounter - 1;
                    if (lastActiveCiMajorValue == CImode.MAJORBUY) {
                        //outputMajorCiValue = true;
                        lastActiveCiMajorValue = CImode.NOCYCLE;
                    }
					/*if(lastActiveCiMajorValue == CImode.MAJORBUY && lastActiveMajorCIBarId != currentBarId)
					{
						lastActiveCiMajorValue = CImode.NOCYCLE;
						outputMajorCiValue = true;
						updateMajorCiParams = true;
					}*/
                }
            } else if (checkMajorBuyOrSell == CheckBuyOrSell.SELL || checkMajorBuyOrSell == CheckBuyOrSell.NONE) {
                if (checkSellFactor(majorCycleSellPrice)) {
                    if (lastActiveCiMajorValue == CImode.NOCYCLE) {
                        lastActiveCiMajorValue = CImode.MAJORSELL;
                        outputMajorCiBarId = majorCycleBarId;
                        majorCyclePosShift = barsCounter - 1;
                        SweepMajor = SweepB;
                        //lastActiveMinorCIBarId = currentBarId;
                        //outputMajorCiValue = true;
                    }
                    if (isBarClosed) {
                        boolean condition = (majorCycleSellPrice - currentClosePrice) >= SweepB;
                        if (condition && barsCounter - majorCyclePosShift >= minShiftAfterCiActivation) {
                            checkMajorBuyOrSell = CheckBuyOrSell.BUY;
                            majorCycleBuyPrice = currentClosePrice;
                            lastConfirmedCiMajorValue = CImode.MAJORSELL;
                            updateMajorCiParams = true;
                            lastConfirmedMajorCIBarId = majorCycleBarId;
                            confirmedMajorCi = true;
                            //outputMajorCiValue = true;
                            majorCycleBarId = currentBarId;
                            majorCyclePosShift = 0;
                        }
                    }
					/*if(lastActiveCiMajorValue == CImode.NOCYCLE)
					{
						lastActiveCiMajorValue = CImode.MAJORSELL;
						lastActiveMajorCIBarId = currentBarId;
						outputMajorCiValue = true;
					}
					if(lastActiveMajorCIBarId != currentBarId)
					{
						boolean condition = (majorCyclePrice - currentCyclePrice) >= SweepB;
						if(condition && barsCounter - lastActiveMajorCIBarId >= minShiftAfterCiActivation)
						{
							checkMajorBuyOrSell = CheckBuyOrSell.BUY;
							majorCyclePrice = currentCyclePrice;
							lastConfirmedCiMajorValue = CImode.MAJORSELL;
							updateMajorCiParams = true;
							lastConfirmedMajorCIBarId = lastActiveMajorCIBarId;
							confirmedMajorCi = true;
							outputMajorCiValue = true;
						}
					}*/
                } else {
                    if (isBarClosed) {
                        majorCycleSellPrice = currentClosePrice;
                        majorCycleBarId = currentBarId;
                    }
                    majorCyclePosShift = barsCounter - 1;
                    if (lastActiveCiMajorValue == CImode.MAJORSELL) {
                        //outputMajorCiValue = true;
                        lastActiveCiMajorValue = CImode.NOCYCLE;
                    }
					/*if(lastActiveCiMajorValue == CImode.MAJORSELL && lastActiveMajorCIBarId != currentBarId)
					{
						lastActiveCiMajorValue = CImode.NOCYCLE;
						outputMajorCiValue = true;
						updateMajorCiParams = true;
					}*/
                }
            }
        }
    }

    //private void InitSweeps(int highPrice, int lowPrice)
    private void SetSweeps() {
		/*if(highPrice != lowPrice)
		{
			//double range = getRange();
			double range = ((highPrice - lowPrice)/2.0)*3.0;
			SweepA = range;
			SweepB = range * majorCycleStrength;
			sweepsAreInitialized = true;
		}*/
        int smaResult = sma.getResult();
        SweepA = smaResult * getAverageBarMultiplier();
        SweepB = SweepA * getMajorCycleMultiplier();
    }

    private void InitCyclePrices() {
        //minorCyclePrice = majorCyclePrice = currentClosePrice;
        minorCycleBuyPrice = minorCycleSellPrice = majorCycleBuyPrice = majorCycleSellPrice = currentClosePrice;
        cyclePricesAreInitialized = true;
        majorCycleBarId = minorCycleBarId = currentBarId;
    }

    private double getRange() {
        int size = firstBarHighPrices.size();
        int _highPrice = 0;
        int _lowPrice = 0;
        for (int i = size - 1; i >= 0; i--) {
            _highPrice = firstBarHighPrices.get(i);
            _lowPrice = firstBarLowPrices.get(i);
            if (_highPrice != _lowPrice)
                return ((_highPrice - _lowPrice) / 2.0) * 3.0;
        }
        return ((firstBarHighPrices.get(size - 1) - firstBarLowPrices.get(size - 1)) / 2) * 3;
    }

    /*private int calculateSSMA()
    {
        return closePrices[1] - previousCycleSSMA + closePrices[0];
    }*/
    private boolean checkBuyFactor(int cyclePrice) {
        if (currentClosePrice < cyclePrice)
            return false;
        else return currentClosePrice > cyclePrice;
	}

    private boolean checkSellFactor(int cyclePrice) {
        if (currentClosePrice > cyclePrice)
            return false;
        else return currentClosePrice < cyclePrice;
	}

    public void update() {
        if (updateMinorCiParams) {
            //lastActiveCiMinorValue = CImode.NOCYCLE;
            //lastActiveMinorCIBarId = 0;
            updateMinorCiParams = false;
            if (confirmedMinorCi) {
                lastActiveCiMinorValue = CImode.NOCYCLE;
                outputMinorCiBarId = minorCycleBarId;
            }
            confirmedMinorCi = false;
        }
        if (updateMajorCiParams) {
            //lastActiveCiMajorValue = CImode.NOCYCLE;
            //lastActiveMajorCIBarId = 0;
            updateMajorCiParams = false;
            if (confirmedMajorCi) {
                lastActiveCiMajorValue = CImode.NOCYCLE;
                outputMajorCiBarId = majorCycleBarId;
            }
            confirmedMajorCi = false;
        }
        //outputMajorCiValue = false;
        //outputMinorCiValue = false;
    }

    /*public CycleIdentifierValue[] getCIValues()
    {
        CycleIdentifierValue[] ciValues = new CycleIdentifierValue[2];
        if(outputMinorCiValue)
            ciValues[0] = new CycleIdentifierValue(lastActiveMinorCIBarId, lastActiveCiMinorValue, outputMinorCiBarId, outputMinorCiValue, getLastNonNoCycleCiValue(),
                    getLastCiMajorValue(), getLastCiMinorValue(), confirmedMinorCi);
            ciValues[0] = new CycleIdentifierValue(outputMinorCiBarId, lastActiveCiMinorValue, getLastNonNoCycleCiValue(),
                    getLastCiMajorValue(), getLastCiMinorValue(), confirmedMinorCi);
        if(outputMajorCiValue)
            ciValues[1] = new CycleIdentifierValue(outputMajorCiBarId, lastActiveCiMajorValue, getLastNonNoCycleCiValue(),
                    getLastCiMajorValue(), getLastCiMinorValue(), confirmedMajorCi);
        return ciValues;
    }*/
    public CycleIdentifierValue getCIValue() {
        return new CycleIdentifierValue(outputMajorCiBarId, outputMinorCiBarId, lastActiveCiMajorValue, lastActiveCiMinorValue, confirmedMajorCi,
                confirmedMinorCi, getLastCiValue(), getLastCiMajorValue(), getLastCiMinorValue(), SweepB, SweepA, currentClosePrice, minorCycleBuyPrice,
                minorCycleSellPrice, majorCycleBuyPrice, majorCycleSellPrice);
    }

    private int getLastCiValue() {
        // check active CI values (not confirmed)
        int compareActiveCiBarIds = compareBarIds(outputMinorCiBarId,
                outputMajorCiBarId);
        if (compareActiveCiBarIds == 1
                && lastActiveCiMinorValue != CImode.NOCYCLE)
            return lastActiveCiMinorValue;
        else if (compareActiveCiBarIds == -1
                && lastActiveCiMajorValue != CImode.NOCYCLE)
            return lastActiveCiMajorValue;
        else {
            if (lastActiveCiMajorValue != CImode.NOCYCLE
                    && compareActiveCiBarIds == 0)
                return lastActiveCiMajorValue;
        }
        // check confirmed CI values
        int compareConfirmedCiBarIds = compareBarIds(lastConfirmedMinorCIBarId,
                lastConfirmedMajorCIBarId);
        if (compareConfirmedCiBarIds == 1
                && lastConfirmedCiMinorValue != CImode.NOCYCLE)
            return lastConfirmedCiMinorValue;
        else if (compareConfirmedCiBarIds == -1
                && lastConfirmedCiMajorValue != CImode.NOCYCLE)
            return lastConfirmedCiMajorValue;
        else {
            if (lastConfirmedMajorCIBarId > 0)
                return lastConfirmedCiMajorValue;
        }
        // assume that we haven't any CI value yet
        return CImode.NOCYCLE;
    }

    private int getLastCiMajorValue() {
        if (lastActiveCiMajorValue != CImode.NOCYCLE)
            return lastActiveCiMajorValue;
        else return lastConfirmedCiMajorValue;
    }

    private int getLastCiMinorValue() {
        if (lastActiveCiMinorValue != CImode.NOCYCLE)
            return lastActiveCiMinorValue;
        else return lastConfirmedCiMinorValue;
    }

    private int compareBarIds(long barA, long barB) {
        if (barA > barB)
            return 1;
        else if (barA < barB)
            return -1;
        else return 0;
    }

    public void setMajorCycleMultiplier(int majorCycleMultiplier) {
        this.majorCycleMultiplier = majorCycleMultiplier;
    }

    public int getMajorCycleMultiplier() {
        return majorCycleMultiplier;
    }

    public void setAverageBarMultiplier(int averageBarMultiplier) {
        this.averageBarMultiplier = averageBarMultiplier;
    }

    public int getAverageBarMultiplier() {
        return averageBarMultiplier;
    }

    public void setPercentileBarInclude(int percentileBarInclude) {
        this.percentileBarInclude = percentileBarInclude;
    }

    public int getPercentileBarInclude() {
        return percentileBarInclude;
    }
}
