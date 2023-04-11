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

import java.util.HashMap;
import java.util.Map;

public class TimeframeFactory
{

	private static final Map<String, Class<? extends Timeframe>> timeframesMap;
	static
	{
		timeframesMap = new HashMap<String, Class<? extends Timeframe>>();
		timeframesMap.put("seconds", SecondsTimeframe.class);
		timeframesMap.put("second", SecondsTimeframe.class);
		timeframesMap.put("sec", SecondsTimeframe.class);
		timeframesMap.put("s", SecondsTimeframe.class);
		timeframesMap.put("minutes", MinutesTimeframe.class);
		timeframesMap.put("minute", MinutesTimeframe.class);
		timeframesMap.put("min", MinutesTimeframe.class);
		timeframesMap.put("m", MinutesTimeframe.class);
		timeframesMap.put("hours", HoursTimeframe.class);
		timeframesMap.put("hour", HoursTimeframe.class);
		timeframesMap.put("h", HoursTimeframe.class);
		timeframesMap.put("days", DaysTimeframe.class);
		timeframesMap.put("day", DaysTimeframe.class);
		timeframesMap.put("d", DaysTimeframe.class);
		timeframesMap.put("weeks", DaysTimeframe.class);
		timeframesMap.put("week", DaysTimeframe.class);
		timeframesMap.put("w", DaysTimeframe.class);
	}

	public static Timeframe getTimeframe(String timeframePattern)
	{
		Class<? extends Timeframe> timeframeClass = findTimeframeClass(timeframePattern);
		int intervalLength = findIntervalLength(timeframePattern);
		try
		{
			return timeframeClass.newInstance().withIntervalLength(
					intervalLength);
		}
		catch (Exception e)
		{
			throw new TimeframeException(
					"Timeframe must have a default public constructor", e);
		}
	}

	private static Class<? extends Timeframe> findTimeframeClass(
			String timeframePattern)
	{
		String timeframeType = findTimeframeType(timeframePattern);

		Class<? extends Timeframe> timeframeClass = timeframesMap
				.get(timeframeType);
		if (timeframeClass == null) { throw new TimeframeException(
				"Timeframe of type '" + timeframeType
						+ "' doesn't exist. Available types are "
						+ timeframesMap.keySet()); }
		return timeframeClass;
	}

	private static int findIntervalLength(String timeframePattern)
	{
		String timeframeLengthStr = timeframePattern.toLowerCase().trim().split("\\s+")[0];
		return Integer.valueOf(timeframeLengthStr);
	}

	private static String findTimeframeType(String timeframePattern)
	{
		return String.valueOf(timeframePattern.toLowerCase().trim().split("\\s+")[1]);
	}
}