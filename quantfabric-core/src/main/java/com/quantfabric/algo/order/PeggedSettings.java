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

public class PeggedSettings
{
	public static final OrdTypes UNSPECIFIED_ORD_TYPE = null;
	public static final OrdSides UNSPECIFIED_ORD_SIDE = null;
	public static final double DEFAULT_ORD_OFFSET = 0;
	public static final double DEFAULT_ORD_DISCRETION = 0;
	public static final double DEFAULT_ORD_PROTECT = 1;
	public static final double DEFAULT_ORD_AT_OR_BETTER = 0;
	
	public enum OrdTypes
	{
		FOREIGN_EXCHANGE_LIMIT,
		FOREGIEN_EXCANGE_ICEBERG
	}
	
	public enum OrdSides
	{
		BID,
		OFFER,
		MID
	}
	
	private OrdTypes ordType = UNSPECIFIED_ORD_TYPE;
	private OrdSides ordSide = UNSPECIFIED_ORD_SIDE;
	private double ordOffset = DEFAULT_ORD_OFFSET;
	private double ordDiscretion = DEFAULT_ORD_DISCRETION;
	private double ordProtect = DEFAULT_ORD_PROTECT;
	private double ordAtOrBetter = DEFAULT_ORD_AT_OR_BETTER; 
	
	public PeggedSettings(){}

	public OrdTypes getOrdType()
	{
		return ordType;
	}

	public void setOrdType(OrdTypes ordType)
	{
		this.ordType = ordType;
	}

	public OrdSides getOrdSide()
	{
		return ordSide;
	}

	public void setOrdSide(OrdSides ordSide)
	{
		this.ordSide = ordSide;
	}

	public double getOrdOffset()
	{
		return ordOffset;
	}

	public void setOrdOffset(double ordOffset)
	{
		this.ordOffset = ordOffset;
	}

	public double getOrdDiscretion()
	{
		return ordDiscretion;
	}

	public void setOrdDiscretion(double ordDiscretion)
	{
		this.ordDiscretion = ordDiscretion;
	}

	public double getOrdProtect()
	{
		return ordProtect;
	}

	public void setOrdProtect(double ordProtect)
	{
		this.ordProtect = ordProtect;
	}

	public double getOrdAtOrBetter()
	{
		return ordAtOrBetter;
	}

	public void setOrdAtOrBetter(double ordAtOrBetter)
	{
		this.ordAtOrBetter = ordAtOrBetter;
	}
}
