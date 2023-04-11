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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DaysTimeframe extends Timeframe
{
	//private Calendar calendar = getCalendar();
	
	public Interval interval(Date referenceDate) {
		
		return interval(referenceDate, 0);
	}

	/*public Interval interval(Date referenceDate, int timeOffset)
	{		
		calendar.setTime(referenceDate);
		int days = calendar.get(Calendar.DAY_OF_YEAR);
		int roundedDays = (days / intervalLength()) * intervalLength();

		calendar.set(Calendar.DAY_OF_YEAR, roundedDays);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, intervalLength());
		Date end = calendar.getTime();
		return new Interval(start, end);
	}*/

	@Override
	public int getLengthInSeconds()
	{
		return intervalLength() * 3600 * 24;
	}

	public static void main(String[] args) throws ParseException
	{
		DateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date currDate = dateFormat.parse("05.31.2012 17:05:19 GMT");
		System.out.println(dateFormat.format(currDate));
		
		Timeframe dtf = TimeframeFactory.getTimeframe("1 h");
		
		//dtf.withIntervalLength(3);
		
		Interval interval = dtf.interval(currDate); 
		System.out.println(dateFormat.format(interval.getStart()));
		System.out.println(dateFormat.format(interval.getEnd()));
		
		
		
		
	}
}
