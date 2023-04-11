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
package com.quantfabric.algo.market.gate.access.product.producer;



public class OHLC {
	
	private final long open;
	private final long openSourceTimestamp;
	private final long high;
	private final long highSourceTimestamp;
	private final long low;
	private final long lowSourceTimestamp;
	private final long close;
	private final long closeSourceTimestamp;
	private final boolean closed;
	private final long closeTimestamp;
	private final long snapshotId;
	
	private final String productCode;
	
	private final int timeFrameInSeconds;
	private long barId = 0L;
	
	public OHLC() {
		this(0, 0, 0, 0, 0, 0, 0, 0, 0, false, 0, 0, null, 0L);
	}
	
	public OHLC(long snapshotId, long open, long openSourceTimestamp, long high, long highSourceTimestamp, long low,
				long lowSourceTimestamp, long close, long closeSourceTimestamp, boolean closed,
				long closeTimestamp, int timeFrameInSeconds, String productCode, long barId) {
		super();
		this.snapshotId = snapshotId;
		this.open = open;
		this.openSourceTimestamp = openSourceTimestamp;
		this.high = high;
		this.highSourceTimestamp = highSourceTimestamp;
		this.low = low;
		this.lowSourceTimestamp = lowSourceTimestamp;
		this.close = close;
		this.closeSourceTimestamp = closeSourceTimestamp;
		this.closed = closed;
		this.closeTimestamp = closeTimestamp;
		
		this.timeFrameInSeconds = timeFrameInSeconds;
		this.productCode = productCode;		
		this.barId = barId;
	}

	
	public long getOpen() {
		return open;
	}

	
	public long getOpenSourceTimestamp() {
		return openSourceTimestamp;
	}

	
	public long getHigh() {
		return high;
	}

	
	public long getHighSourceTimestamp() {
		return highSourceTimestamp;
	}

	
	public long getLow() {
		return low;
	}

	
	public long getLowSourceTimestamp() {
		return lowSourceTimestamp;
	}

	
	public long getClose() {
		return close;
	}

	
	public long getCloseSourceTimestamp() {
		return closeSourceTimestamp;
	}
	
	
	public boolean isClosed() {
		return closed;
	}

	
	public long getCloseTimestamp() {
		return closeTimestamp;
	}
	
	public long getSnapshotId() {
		return snapshotId;
	}
	
	public int getTimeFrameInSeconds() {
		return timeFrameInSeconds;
	}
	
	public String getProductCode() {
		return productCode;
	}
	
	public long getBarId() {
		return barId;
	}
	
	@Override
	public String toString() {
		
		return "OHLC(" + productCode + ") id = " + snapshotId + " (open=[" + open + "]; openSourceTimestamp=[" + openSourceTimestamp + "]; high=["
				+ high + "]; highSourceTimestamp=[" + highSourceTimestamp + "]; low=[" + low + "]; lowSourceTimestamp=[" + lowSourceTimestamp + "]; close=[" + close
				+ "]; closeSourceTimestamp=[" + closeSourceTimestamp + "]; isClosed=[" + closed + "]; closeTimestamp=[" + closeTimestamp + "]; timeFrameInSeconds=[" + timeFrameInSeconds + "])";
	}
}
