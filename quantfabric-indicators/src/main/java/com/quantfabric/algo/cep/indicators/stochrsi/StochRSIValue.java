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

public class StochRSIValue
{
	double mainValue;
	double signalValue;
		
	public StochRSIValue()
	{
		this(0., 0.);
	}

	public StochRSIValue(double mainValue, double signalValue)
	{
		super();
		this.mainValue = mainValue;
		this.signalValue = signalValue;
	}
	
	public double getMainValue()
	{
		return mainValue;
	}
	public void setMainValue(double mainValue)
	{
		this.mainValue = mainValue;
	}
	public double getSignalValue()
	{
		return signalValue;
	}
	public void setSignalValue(double signalValue)
	{
		this.signalValue = signalValue;
	}
}
