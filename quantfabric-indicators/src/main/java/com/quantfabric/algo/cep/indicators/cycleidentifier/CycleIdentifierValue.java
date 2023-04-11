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

public class CycleIdentifierValue {
	
	
	
	private long majorCibarId;
	private long minorCiBarId;
	
	//private CImode CIValue = CImode.NOCYCLE;
	private int CIMajorValue = CImode.NOCYCLE;
	private int CIMinorValue = CImode.NOCYCLE;
	private boolean CIMajorIsConfirmed;
	private boolean CIMinorIsConfirmed;
	private int lastCIValue = CImode.NOCYCLE;
	
	private int CILastMajorValue = CImode.NOCYCLE;
	private int CILastMinorValue = CImode.NOCYCLE;
	
	private int cyclePrice = 0;

	private int sweepMinor;
	private int sweepMajor;
	private int currentClosePrice;
	
	private int minorBuyCyclePrice;
	private int minorSellCyclePrice;
	private int majorBuyCyclePrice;
	private int majorSellCyclePrice;

	
	public CycleIdentifierValue(){}
	public CycleIdentifierValue(long majorCiBarId, long minorCiBarId, int CIMajorValue, int CIMinorValue,
				boolean CIMajorIsConfirmed, boolean CIMinorIsConfirmed, int lastCIValue,
				int CILastMajorValue, int CILastMinorValue, int sweepMajor, int sweepMinor,  int currentClosePrice, 
				int minorBuyCyclePrice, int minorSellCyclePrice, int majorBuyCyclePrice, int majorSellCyclePrice
				)
	{
		this.setMinorCiBarId(minorCiBarId);
		this.setMajorCibarId(majorCiBarId);
		this.setCIMajorValue(CIMajorValue);
		this.setCIMinorValue(CIMinorValue);
		this.setCIMajorIsConfirmed(CIMajorIsConfirmed);
		this.setCIMinorIsConfirmed(CIMinorIsConfirmed);
		this.setLastCIValue(lastCIValue);
		
		this.setCyclePrice(cyclePrice);
		this.setCILastMajorValue(CILastMajorValue);
		this.setCILastMinorValue(CILastMinorValue);
		
		this.setSweepMajor(sweepMajor);
		this.setSweepMinor(sweepMinor);
		this.setCurrentClosePrice(currentClosePrice);
		this.minorBuyCyclePrice = minorBuyCyclePrice;
		this.minorSellCyclePrice = minorSellCyclePrice;
		this.majorBuyCyclePrice = majorBuyCyclePrice;
		this.majorSellCyclePrice = majorSellCyclePrice;
	}
	public long getMajorCibarId() {
		return majorCibarId;
	}
	public void setMajorCibarId(long majorCibarId) {
		this.majorCibarId = majorCibarId;
	}
	public long getMinorCiBarId() {
		return minorCiBarId;
	}
	public void setMinorCiBarId(long ciMinorBarId) {
		this.minorCiBarId = ciMinorBarId;
	}
	public int getCIMajorValue() {
		return CIMajorValue;
	}
	public void setCIMajorValue(int CIMajorValue) {
		this.CIMajorValue = CIMajorValue;
	}
	public int getCIMinorValue() {
		return CIMinorValue;
	}
	public void setCIMinorValue(int CIMinorValue) {
		this.CIMinorValue = CIMinorValue;
	}
	public int getLastNotNonCycleCIValue() {
		return getLastCIValue();
	}
	public void setLastNotNonCycleCIValue(int lastNotNonCycleCIValue) {
		this.setLastCIValue(lastNotNonCycleCIValue);
	}
	public boolean getCIMinorIsConfirmed() {
		return CIMinorIsConfirmed;
	}
	public void setCIMinorIsConfirmed(boolean cIMinorIsConfirmed) {
		CIMinorIsConfirmed = cIMinorIsConfirmed;
	}
	public boolean getCIMajorIsConfirmed() {
		return CIMajorIsConfirmed;
	}
	public void setCIMajorIsConfirmed(boolean cIMajorIsConfirmed) {
		CIMajorIsConfirmed = cIMajorIsConfirmed;
	}
	public int getLastCIValue() {
		return lastCIValue;
	}
	public void setLastCIValue(int lastCIValue) {
		this.lastCIValue = lastCIValue;
	}
	@Override
	public String toString() {
		return "CycleIdentifierValue [majorCibarId=" + majorCibarId
				+ ", minorCiBarId=" + minorCiBarId + ", CIMajorValue="
				+ CIMajorValue + ", CIMinorValue=" + CIMinorValue
				+ ", CIMajorIsConfirmed=" + CIMajorIsConfirmed
				+ ", CIMinorIsConfirmed=" + CIMinorIsConfirmed
				+ ", lastCIMajorValue=" + CILastMajorValue
				+ ", lastCIMinorValue=" + CILastMinorValue
				+ ", lastCIValue=" + lastCIValue + "]";
	}

	public void setCyclePrice(int cyclePrice) {
		this.cyclePrice = cyclePrice;
	}
	public int getCyclePrice() {
		return cyclePrice;
	}
	public void setCILastMajorValue(int cILastMajorValue) {
		CILastMajorValue = cILastMajorValue;
	}
	public int getCILastMajorValue() {
		return CILastMajorValue;
	}
	public void setCILastMinorValue(int cILastMinorValue) {
		CILastMinorValue = cILastMinorValue;
	}
	public int getCILastMinorValue() {
		return CILastMinorValue;
	}
	public void setSweepMinor(int sweepMinor) {
		this.sweepMinor = sweepMinor;
	}
	public int getSweepMinor() {
		return sweepMinor;
	}
	public void setSweepMajor(int sweepMajor) {
		this.sweepMajor = sweepMajor;
	}
	public int getSweepMajor() {
		return sweepMajor;
	}
	public void setCurrentClosePrice(int currentClosePrice) {
		this.currentClosePrice = currentClosePrice;
	}
	public int getCurrentClosePrice() {
		return currentClosePrice;
	}
	public void setMinorBuyCyclePrice(int minorBuyCyclePrice) {
		this.minorBuyCyclePrice = minorBuyCyclePrice;
	}
	public int getMinorBuyCyclePrice() {
		return minorBuyCyclePrice;
	}
	public void setMinorSellCyclePrice(int minorSellCyclePrice) {
		this.minorSellCyclePrice = minorSellCyclePrice;
	}
	public int getMinorSellCyclePrice() {
		return minorSellCyclePrice;
	}
	public void setMajorBuyCyclePrice(int majorBuyCyclePrice) {
		this.majorBuyCyclePrice = majorBuyCyclePrice;
	}
	public int getMajorBuyCyclePrice() {
		return majorBuyCyclePrice;
	}
	public void setMajorSellCyclePrice(int majorSellCyclePrice) {
		this.majorSellCyclePrice = majorSellCyclePrice;
	}
	public int getMajorSellCyclePrice() {
		return majorSellCyclePrice;
	}
}
