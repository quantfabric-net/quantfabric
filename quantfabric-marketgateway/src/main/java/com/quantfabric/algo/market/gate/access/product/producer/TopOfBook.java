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


public class TopOfBook
{	
	private final long bidPrice;
	private final int bidValue;
	
	private final long askPrice;
	private final int askValue;
		
	private final long snapshotId;
	private final String productCode;
	
	private final long sourceTimestamp;
		
	public TopOfBook()
	{
		this(0, 0, 0, 0, 0, null, 0);
	}

	public TopOfBook(long snapshotId, long bidPrice, int bidValue, long askPrice, int askValue, String productCode, long sourceTimestamp)
	{
		super();		
		this.bidPrice = bidPrice;
		this.bidValue = bidValue;
		this.askPrice = askPrice;
		this.askValue = askValue;
		
		this.snapshotId = snapshotId;
		this.productCode = productCode;
		
		this.sourceTimestamp = sourceTimestamp;		
	}

	public long getBidPrice()
	{
		return bidPrice;
	}

	public int getBidValue()
	{
		return bidValue;
	}

	public long getAskPrice()
	{
		return askPrice;
	}

	public int getAskValue()
	{
		return askValue;
	}
	
	public long getSnapshotId() {
		
		return snapshotId;
	}
	
	public String getProductCode() {
		
		return productCode;
	}
	
	public long getSourceTimestamp() {
		
		return sourceTimestamp;
	}	

	@Override
	public String toString()
	{
		return "TopOfBook(" + productCode + ") id = " + snapshotId + " [" + bidPrice + "(" + bidValue
				+ ") / " + askPrice + "(" + askValue
				+ ")]";
	}
}
