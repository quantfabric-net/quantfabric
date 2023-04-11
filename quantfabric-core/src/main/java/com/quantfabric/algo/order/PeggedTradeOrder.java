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

import com.quantfabric.algo.order.PeggedSettings.OrdSides;
import com.quantfabric.algo.order.PeggedSettings.OrdTypes;

public class PeggedTradeOrder extends TradeOrder
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6006128489661496626L;

	public PeggedTradeOrder()
	{
		super();
		setPeggedSettings(new PeggedSettings());
	}
	
	public OrdTypes getPeggedOrdType()
	{
		return getPeggedSettings().getOrdType();
	}

	public void setPeggedOrdType(OrdTypes ordType)
	{
		getPeggedSettings().setOrdType(ordType);
	}

	public OrdSides getPeggedOrdSide()
	{
		return getPeggedSettings().getOrdSide();
	}

	public void setPeggedOrdSide(OrdSides ordSide)
	{
		getPeggedSettings().setOrdSide(ordSide);
	}

	public double getPeggedOrdOffset()
	{
		return getPeggedSettings().getOrdOffset();
	}

	public void setPeggedOrdOffset(double ordOffset)
	{
		getPeggedSettings().setOrdOffset(ordOffset);
	}

	public double getPeggedOrdDiscretion()
	{
		return getPeggedSettings().getOrdDiscretion();
	}

	public void setPeggedOrdDiscretion(double ordDiscretion)
	{
		getPeggedSettings().setOrdDiscretion(ordDiscretion);
	}

	public double getPeggedOrdProtect()
	{
		return getPeggedSettings().getOrdProtect();
	}

	public void setPeggedOrdProtect(double ordProtect)
	{
		getPeggedSettings().setOrdProtect(ordProtect);
	}

	public double getPeggedOrdAtOrBetter()
	{
		return getPeggedSettings().getOrdAtOrBetter();
	}

	public void setPeggedOrdAtOrBetter(double ordAtOrBetter)
	{
		getPeggedSettings().setOrdAtOrBetter(ordAtOrBetter);
	}
}
