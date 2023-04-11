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
package com.quantfabric.algo.order;

public class OCOSettings
{
	public enum LegType
	{
		STOP_LOSS,
		STOP_LIMIT
	}
	
	public enum LegSide
	{
		BUY,
		SELL
	}
	
	public enum StopSide
	{
		BID,
		OFFER
	}
	
	private int leg1LimitRate;
	private LegType leg2Type;
	private LegSide leg2Side;
	private int leg2StopRate;
	private StopSide leg2StopSide;
	private int leg2StopLimitRate;
	
	public OCOSettings() 
	{		
	}
	
	public int getLeg1LimitRate()
	{
		return leg1LimitRate;
	}
	public void setLeg1LimitRate(int leg1LimitRate)
	{
		this.leg1LimitRate = leg1LimitRate;
	}
	public LegType getLeg2Type()
	{
		return leg2Type;
	}
	public void setLeg2Type(LegType leg2Type)
	{
		this.leg2Type = leg2Type;
	}
	public LegSide getLeg2Side()
	{
		return leg2Side;
	}
	public void setLeg2Side(LegSide leg2Side)
	{
		this.leg2Side = leg2Side;
	}
	public int getLeg2StopRate()
	{
		return leg2StopRate;
	}
	public void setLeg2StopRate(int leg2StopRate)
	{
		this.leg2StopRate = leg2StopRate;
	}
	public StopSide getLeg2StopSide()
	{
		return leg2StopSide;
	}
	public void setLeg2StopSide(StopSide leg2StopSide)
	{
		this.leg2StopSide = leg2StopSide;
	}
	public int getLeg2StopLimitRate()
	{
		return leg2StopLimitRate;
	}
	public void setLeg2StopLimitRate(int leg2StopLimitRate)
	{
		this.leg2StopLimitRate = leg2StopLimitRate;
	}

}
