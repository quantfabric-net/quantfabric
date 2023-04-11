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
package com.quantfabric.algo.market.datamodel;

import java.util.Arrays;


public class IncrementalUpdate {
	
	private final int[] bidIds;
	private final int[] bidPrices;
	private final int[] bidVolumes;
	private final int[] askIds;
	private final int[] askPrices;
	private final int[] askVolumes;
	private final int[] deletedIds;
	
	private final long snapshotId;
	private final String productCode;
	
	private boolean isNewSnapshot = false;
	
	public IncrementalUpdate() {
		this(0, null, null, null, null, null, null, null, null);
	}
	
	public IncrementalUpdate(long snapshotId, int[] bidIds, int[] bidPrices, int[] bidVolumes,
			int[] askIds, int[] askPrices, int[] askVolumes, int[] deletedIds, String productCode) {
		
		this.snapshotId = snapshotId;
		this.bidIds = bidIds;
		this.bidPrices = bidPrices;
		this.bidVolumes = bidVolumes;
		this.askIds = askIds;
		this.askPrices = askPrices;
		this.askVolumes = askVolumes;
		this.deletedIds = deletedIds;		
		this.productCode = productCode;
	}

	
	public int[] getBidIds() {
		return bidIds;
	}

	
	public int[] getBidPrices() {
		return bidPrices;
	}

	
	public int[] getBidVolumes() {
		return bidVolumes;
	}

	
	public int[] getAskIds() {
		return askIds;
	}

	
	public int[] getAskPrices() {
		return askPrices;
	}

	
	public int[] getAskVolumes() {
		return askVolumes;
	}

	
	public int[] getDeletedIds() {
		return deletedIds;
	}

	
	public boolean isNewSnapshot() {
		return isNewSnapshot;
	}
		
	public long getSnapshotId() {
		
		return  snapshotId;
	}
	
	public String getProductCode() {
		
		return productCode;
	}
	
	public void setNewSnapshot(boolean isNewSnapshot) {
		this.isNewSnapshot = isNewSnapshot;
	}	

	@Override
	public String toString() {
		return "IncrementalUpdate(" + productCode + ") id = " + snapshotId + " [bidPrices=" + Arrays.toString(bidPrices) + ", askPrices=" + Arrays.toString(askPrices) + ", bidVolumes="
				+ Arrays.toString(bidVolumes) + ", askVolumes=" + Arrays.toString(askVolumes) + ", bidIds=" + Arrays.toString(bidIds) + ", askIds= "
				+ Arrays.toString(askIds) + ", deletedIds=" + Arrays.toString(deletedIds) + ", isNewSnapshot=" + isNewSnapshot +"]";
	}
}
