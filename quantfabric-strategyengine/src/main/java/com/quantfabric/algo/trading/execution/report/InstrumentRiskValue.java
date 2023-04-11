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
package com.quantfabric.algo.trading.execution.report;

public class InstrumentRiskValue implements ExecutionReport
{
	public String instrumentId;
	public double riskValue;
	
	public InstrumentRiskValue()
	{
		this(null, 0);
	}
	
	public InstrumentRiskValue(String instrumentId, double riskValue)
	{
		super();
		this.instrumentId = instrumentId;
		this.riskValue = riskValue;
	}
	
	public String getInstrumentId()
	{
		return instrumentId;
	}
	public void setInstrumentId(String instrumentId)
	{
		this.instrumentId = instrumentId;
	}
	public double getRiskValue()
	{
		return riskValue;
	}
	public void setRiskValue(double riskValue)
	{
		this.riskValue = riskValue;
	}

	@Override
	public String toString()
	{
		return "InstrumentRiskValue : instrumentId=" + instrumentId + "; riskValue=" + riskValue
				+ ";";
	}
}
