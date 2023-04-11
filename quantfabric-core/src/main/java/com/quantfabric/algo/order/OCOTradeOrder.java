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

import com.quantfabric.algo.order.OCOSettings.LegSide;
import com.quantfabric.algo.order.OCOSettings.LegType;
import com.quantfabric.algo.order.OCOSettings.StopSide;

public class OCOTradeOrder extends TradeOrder
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6108168535196473033L;
	public OCOTradeOrder()
	{
		super();
		setOcoSettings(new OCOSettings());
	}
	
	public int getOcoLeg1LimitRate()
	{
		return getOcoSettings().getLeg1LimitRate();
	}
	public void setOcoLeg1LimitRate(int leg1LimitRate)
	{
		getOcoSettings().setLeg1LimitRate(leg1LimitRate);
	}
	public LegType getOcoLeg2Type()
	{
		return getOcoSettings().getLeg2Type();
	}
	public void setOcoLeg2Type(LegType leg2Type)
	{
		getOcoSettings().setLeg2Type(leg2Type);
	}
	public LegSide getOcoLeg2Side()
	{
		return getOcoSettings().getLeg2Side();
	}
	public void setOcoLeg2Side(LegSide leg2Side)
	{
		getOcoSettings().setLeg2Side(leg2Side);
	}
	public int getOcoLeg2StopRate()
	{
		return getOcoSettings().getLeg2StopRate();
	}
	public void setOcoLeg2StopRate(int leg2StopRate)
	{
		getOcoSettings().setLeg2StopRate(leg2StopRate);
	}
	public StopSide getOcoLeg2StopSide()
	{
		return getOcoSettings().getLeg2StopSide();
	}
	public void setOcoLeg2StopSide(StopSide leg2StopSide)
	{
		getOcoSettings().setLeg2StopSide(leg2StopSide);
	}
	public int getOcoLeg2StopLimitRate()
	{
		return getOcoSettings().getLeg2StopLimitRate();
	}
	public void setOcoLeg2StopLimitRate(int leg2StopLimitRate)
	{
		getOcoSettings().setLeg2StopLimitRate(leg2StopLimitRate);
	}
}
