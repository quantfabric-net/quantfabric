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

public class SecondsTimeframe extends Timeframe
{
	//private Calendar calendar = getCalendar();
	
	public Interval interval(Date referenceDate) {
		
		return interval(referenceDate, 0);
	}

	/*public Interval interval(Date referenceDate, int timeOffset)
	{
		calendar.setTime(referenceDate);

		int seconds = calendar.get(Calendar.SECOND);
		int roundedSeconds = (seconds / intervalLength()) * intervalLength();

		calendar.set(Calendar.SECOND, roundedSeconds);
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.SECOND, intervalLength());
		Date end = calendar.getTime();
		return new Interval(start, end);
	}*/

	@Override
	public int getLengthInSeconds()
	{
		return intervalLength();
	}
}