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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeframeTest
{

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException	{
		Timeframe timeframe_4H = TimeframeFactory.getTimeframe("2 h");
		
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd kk:mm:ss");
		dateFormat.setCalendar(calendar);
		
		Date refDate = dateFormat.parse("2012.11.26 17:00:00");
		
		Interval interval = timeframe_4H.interval(refDate, 3600);
		
		System.out.println(dateFormat.format(interval.getStart()) + " - " + dateFormat.format(interval.getEnd()));
	}

}
