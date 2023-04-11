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

import java.util.Arrays;

import com.quantfabric.algo.market.datamodel.BaseLightweightMDFeedEvent;

public class FullBook implements BaseLightweightMDFeedEvent
{
	private final long[] bidPrices;
	private final long[] askPrices;
	
	private final long[] bidVolumes;
	private final long[] askVolumes;
	
	private final long snapshotId;
	private final String productCode;
	private final long sourceTimestamp;
	
	private final String instrumentId;
	private final String symbol;
	
	private final String feedName;
	
	public FullBook()
	{
		this(0, null, null, null, null, null, 0, null, null, null);
	}
	
	public FullBook(long snapshotId, long[] bidPrices, long[] bidVolumes,
					long[] askPrices, long[] askVolumes, String productCode, 
					long sourceTimestamp, String instrumentId, String symbol, String feedName)
	{		
		this.bidPrices = bidPrices;
		this.askPrices = askPrices;
		this.bidVolumes = bidVolumes;
		this.askVolumes = askVolumes;
		
		this.snapshotId = snapshotId;
		this.productCode = productCode;
		this.sourceTimestamp = sourceTimestamp;
		
		this.instrumentId = instrumentId;
		this.symbol = symbol;
		
		this.feedName = feedName;
	}

	public long[] getBidPrices()
	{
		return bidPrices;
	}

	public long[] getAskPrices()
	{
		return askPrices;
	}

	public long[] getBidVolumes()
	{
		return bidVolumes;
	}

	public long[] getAskVolumes()
	{
		return askVolumes;
	}
	
	
	public long getSnapshotId() {
		
		return snapshotId;
	}
	
	public String getProductCode() {
		
		return productCode;
	}
	
	public long getSourceTimestamp(){
		
		return sourceTimestamp;
	}

	public String getInstrumentId() {
		return instrumentId;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getFeedName() {
		return feedName;
	}

	@Override
	public String toString()
	{
		return "FullBook(" + productCode + ") id = " + snapshotId + " [bidPrices=" + Arrays.toString(bidPrices)
				+ ", askPrices=" + Arrays.toString(askPrices) + ", bidVolumes="
				+ Arrays.toString(bidVolumes) + ", askVolumes="
				+ Arrays.toString(askVolumes) + "]";
	}
}
