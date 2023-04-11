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
package com.quantfabric.util.timeframes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class Timeframe
{
	private int intervalLength;

	public abstract Interval interval(Date referenceDate);
	
	public Interval interval(Date referenceDate, int timeOffset) {
		
		long startDt = (referenceDate.getTime() / (getLengthInSeconds() * 1000L) * (getLengthInSeconds() * 1000L));

		startDt += timeOffset * 1000L;
		if (referenceDate.getTime() < startDt)
			startDt = startDt - (getLengthInSeconds() * 1000L);

		Date startDate = new Date(startDt);
		Date endDate = new Date(startDt + (getLengthInSeconds() * 1000L));

		return new Interval(startDate, endDate);
	}

	public abstract int getLengthInSeconds();
	
	protected int intervalLength()
	{
		return intervalLength;
	}
	
	Timeframe withIntervalLength(int intervalLength)
	{
		this.intervalLength = intervalLength;
		return this;
	}
	
	protected static Calendar getCalendar()
	{
		return GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
	}

}
