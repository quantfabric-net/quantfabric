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
package com.quantfabric.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtils
{
	public static Date parse(String pattern, String value) throws ParseException
	{
		Date parsed = null;
		try {
			parsed = new SimpleDateFormat(pattern).parse(value);
		} catch (Exception e) {
			return null;
		}
		return parsed;
	}
	public static Date convertFromUnixTimeStampToDate(Long timestamp) throws ParseException
	{
		return new Date(timestamp);
	}

	public static Date setDateHoursAndMinutesFromAnotherDate(Date dateDest, Date dateFrom) {
		Calendar fromCalendar = Calendar.getInstance();
		Calendar destCalendar = Calendar.getInstance();

		destCalendar.setTime(dateDest);

		if (null != dateFrom)
			fromCalendar.setTime(dateFrom);
		else
			fromCalendar.clear();

		Calendar resultCalendar = (Calendar) destCalendar.clone();

		if (null == fromCalendar.getTime()) {

			resultCalendar.set(Calendar.HOUR_OF_DAY, 0);
			resultCalendar.set(Calendar.MINUTE, 0);

		}
		else {

			resultCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.get(Calendar.HOUR_OF_DAY));
			resultCalendar.set(Calendar.MINUTE, fromCalendar.get(Calendar.MINUTE));

		}

		resultCalendar.set(Calendar.SECOND, 0);

		return resultCalendar.getTime();
	}

	public static boolean between(Date startTime, Date stopTime, Date sourceDate) {

		Calendar startTimeCalendar = Calendar.getInstance();
		Calendar stopTimeCalendar = Calendar.getInstance();
		Calendar sourceDateCalendar = Calendar.getInstance();

		startTimeCalendar.setTime(startTime);
		stopTimeCalendar.setTime(stopTime);
		sourceDateCalendar.setTime(sourceDate);

		Calendar sourceDateMidnightCalendar = (Calendar) sourceDateCalendar.clone();
		Calendar sourceDateEndOfDayCalendar = (Calendar) sourceDateCalendar.clone();

		sourceDateMidnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
		sourceDateMidnightCalendar.set(Calendar.MINUTE, 0);
		sourceDateEndOfDayCalendar.set(Calendar.HOUR_OF_DAY, 23);
		sourceDateEndOfDayCalendar.set(Calendar.MINUTE, 59);

		// check if trading within 2 calendar days over a night

		if (startTimeCalendar.get(Calendar.HOUR_OF_DAY) > stopTimeCalendar.get(Calendar.HOUR_OF_DAY)) {
			return (sourceDateCalendar.after(sourceDateMidnightCalendar) && sourceDateCalendar.before(stopTimeCalendar))
					|| (sourceDateCalendar.after(startTimeCalendar) && sourceDateCalendar.before(sourceDateEndOfDayCalendar));
		}
		else {
			return sourceDateCalendar.after(startTimeCalendar) && sourceDateCalendar.before(stopTimeCalendar);
		}
	}
}
