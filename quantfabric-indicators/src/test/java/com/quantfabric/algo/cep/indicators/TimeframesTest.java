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
package com.quantfabric.algo.cep.indicators;

import java.text.ParseException;
import java.util.Date;

import com.quantfabric.util.timeframes.Interval;
import com.quantfabric.util.timeframes.Timeframe;
import com.quantfabric.util.timeframes.TimeframeFactory;

public class TimeframesTest
{

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException
	{
		Timeframe tf = TimeframeFactory.getTimeframe("4 h");
		
		Date date = new Date(1341964799000L); //new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2012-11-07 00:59:59"); 
		
		Interval interval = tf.interval(date);
		
		System.out.println(date);
		System.out.println(interval);
	}

}
