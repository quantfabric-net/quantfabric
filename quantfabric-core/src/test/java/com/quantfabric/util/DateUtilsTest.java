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
//package com.quantfabric.util;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.Calendar;
//import java.util.Date;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//
//public class DateUtilsTest {
//
//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//	}
//
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test
//	public void testSetDateHoursAndMinutesFromAnotherDate() {
//
//		Date fromDate = new Date(1383289132824L);
//		Date destDate = new Date(1383235022524L);
//
//		Calendar fromCalendar = Calendar.getInstance();
//		Calendar destCalendar = Calendar.getInstance();
//		Calendar resultCalendar = Calendar.getInstance();
//
//		fromCalendar.setTime(fromDate);
//		destCalendar.setTime(destDate);
//
//		Date newDate = DateUtils.setDateHoursAndMinutesFromAnotherDate(destDate, fromDate);
//
//		resultCalendar.setTime(newDate);
//
//		assertEquals(resultCalendar.get(Calendar.YEAR), destCalendar.get(Calendar.YEAR));
//		assertEquals(resultCalendar.get(Calendar.MONTH), destCalendar.get(Calendar.MONTH));
//		assertEquals(resultCalendar.get(Calendar.DAY_OF_MONTH), destCalendar.get(Calendar.DAY_OF_MONTH));
//		assertEquals(resultCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.HOUR_OF_DAY));
//		assertEquals(resultCalendar.get(Calendar.MINUTE), fromCalendar.get(Calendar.MINUTE));
//
//		newDate = DateUtils.setDateHoursAndMinutesFromAnotherDate(new Date(Long.MIN_VALUE), fromDate);
//
//		assertEquals(resultCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.HOUR_OF_DAY));
//		assertEquals(resultCalendar.get(Calendar.MINUTE), fromCalendar.get(Calendar.MINUTE));
//
//		newDate = DateUtils.setDateHoursAndMinutesFromAnotherDate(new Date(Long.MAX_VALUE), fromDate);
//
//		assertEquals(resultCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.HOUR_OF_DAY));
//		assertEquals(resultCalendar.get(Calendar.MINUTE), fromCalendar.get(Calendar.MINUTE));
//
//		newDate = DateUtils.setDateHoursAndMinutesFromAnotherDate(destDate, null);
//		resultCalendar.setTime(newDate);
//
//		assertEquals(0, resultCalendar.get(Calendar.HOUR_OF_DAY));
//		assertEquals(0, resultCalendar.get(Calendar.MINUTE));
//
//	}
//
//	@Test
//	public void testBetween() {
//
//		Date stopTime = new Date(1383289132824L);
//		Date startTime = new Date(1383235022524L);
//		Date sourceDate = new Date();
//
//		boolean check;
//
//		check = DateUtils.between(startTime, stopTime, sourceDate);
//		assertEquals(check, true);
//
//		check = DateUtils.between(stopTime, startTime, sourceDate);
//		assertEquals(check, false);
//	}
//
//}
