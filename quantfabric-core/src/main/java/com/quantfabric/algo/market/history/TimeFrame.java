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
package com.quantfabric.algo.market.history;

public enum TimeFrame {
	M1(60),
	M3(180),
	M5(300),
	M10(600),
	M15(900),
	M30(1800),
	H1(3600),
	H2(7200),
	H4(14400),
	H6(3600*6),
	D1(86400);
	
	private final int seconds;
	
	TimeFrame(int sec){
		seconds=sec;
	}
	
	public int getSeconds(){
		return seconds;
	}
	
	public static TimeFrame getTimeFrame(int seconds) {
	    for (TimeFrame tf : values()) {
	        if (tf.seconds == seconds) {
	            return tf;
	        }
	    }
	    return null;
	}
}
