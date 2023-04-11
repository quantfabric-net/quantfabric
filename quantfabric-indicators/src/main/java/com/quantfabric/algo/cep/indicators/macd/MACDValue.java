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
package com.quantfabric.algo.cep.indicators.macd;

public class MACDValue
{
	private double fastEMA;
	private double slowEMA;
	private double macd;
	private double signal;
	private double divergence;
	
	public MACDValue()
	{
		this(0., 0., 0., 0, 0.);
	}	
	
	public MACDValue(double fastEMA, double slowEMA, double macd, double signal,
			double divergence)
	{
		super();
		this.fastEMA = fastEMA;
		this.slowEMA = slowEMA;
		this.macd = macd;
		this.signal = signal;
		this.divergence = divergence;
	}

	public double getFastEMA()
	{
		return fastEMA;
	}

	public double getSlowEMA()
	{
		return slowEMA;
	}

	public double getMACD()
	{
		return macd;
	}

	public double getSignal()
	{
		return signal;
	}

	public double getDivergence()
	{
		return divergence;
	}

	public void setFastEMA(double fastEMA)
	{
		this.fastEMA = fastEMA;
	}

	public void setSlowEMA(double slowEMA)
	{
		this.slowEMA = slowEMA;
	}

	public void setMACD(double macd)
	{
		this.macd = macd;
	}

	public void setSignal(double signal)
	{
		this.signal = signal;
	}

	public void setDdivergence(double divergence)
	{
		this.divergence = divergence;
	}
	
	
}
