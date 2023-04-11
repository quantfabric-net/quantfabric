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
package com.quantfabric.algo.cep.indicators.timeframer;

public class TimeFramerValue {
	
	private final int timeFrameInSeconds;
	private final int value;
	private boolean isBarClosed;
	
	public int getTimeFrameInSeconds()
	{
		return timeFrameInSeconds;
	}
	public int getValue()
	{
		return value;
	}
	public TimeFramerValue(int timeFrameInSeconds, int value)
	{
		this.timeFrameInSeconds = timeFrameInSeconds;
		this.value = value;
	}
	@Override
	public String toString()
	{
		return " timeFrame=" + timeFrameInSeconds + "; value=" + value+";";
	}
	public boolean isBarClosed() {
		return isBarClosed;
	}
}
