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

import java.util.Date;

public class HoursTimeframe extends Timeframe
{
	//private Calendar calendar = getCalendar();
	
	public Interval interval(Date referenceDate) {
		
		return interval(referenceDate, 0);
	}

	/*public Interval interval(Date referenceDate, int timeOffset)
	{
		calendar.setTime(referenceDate);

		//int hours = calendar.get(Calendar.HOUR_OF_DAY);
		//int roundedHours = (hours / intervalLength()) * intervalLength();
		
		long startDt = (referenceDate.getTime()/(getLengthInSeconds() * 1000) * (getLengthInSeconds() * 1000));
		
		startDt += timeOffset * 1000;
		if (referenceDate.getTime() < startDt)		
			startDt = startDt - (getLengthInSeconds() * 1000);
		
		Date startDate = new Date(startDt);
		Date endDate = new Date(startDt + (getLengthInSeconds() * 1000));
		//System.out.println(">> " + startDate.toGMTString() + " - " + endDate.toGMTString());

		/*calendar.set(Calendar.HOUR_OF_DAY, roundedHours + timeOffset);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.HOUR, intervalLength());
		Date end = calendar.getTime();
		return new Interval(start, end);*/
		//return new Interval(startDate, endDate);
	//}

	@Override
	public int getLengthInSeconds()
	{
		return intervalLength() * 3600;
	}

}