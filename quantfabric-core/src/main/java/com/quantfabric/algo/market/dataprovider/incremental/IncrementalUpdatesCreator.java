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
package com.quantfabric.algo.market.dataprovider.incremental;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.quantfabric.algo.market.datamodel.IncrementalUpdate;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;

public class IncrementalUpdatesCreator {
	
	private final List<Long> bidPrices = new LinkedList<>();
	private final List<Integer> bidSizes = new LinkedList<>();
	private final List<Integer> bidIds = new LinkedList<>();
	
	private final List<Long> askPrices = new LinkedList<>();
	private final List<Integer> askSizes = new LinkedList<>();
	private final List<Integer> askIds = new LinkedList<>();
	
	private final List<Integer> deletedIds = new LinkedList<>();
	
	private boolean isAskCommited;
	private boolean isBidCommited;
	
	private boolean bidPriceChanged = false;
	private boolean askPriceChanged = false;
	private boolean deletedPriceChanged = false;
	
	private boolean isNewSnapshot = false;
	
	public void setNewSnapshot(boolean isNewSnapshot) {
		
		this.isNewSnapshot = isNewSnapshot;  
	}

	public void addBidPrice(int id, long price, int size) {
		
		bidIds.add(id);
		bidPrices.add(price);
		bidSizes.add(size);
		
		bidPriceChanged = true;
	}
	
	public void addAskPrice(int id, long price, int size) {
		
		askIds.add(id);
		askPrices.add(price);
		askSizes.add(size);		
		
		askPriceChanged = true;
	}
	
	public void deletePrice(int id) {
		
		deletedIds.add(id);
		
		deletedPriceChanged = true;
	}
	
	public IncrementalUpdate commit(String productCode, long messageId, MDItemType monitoredMdItemType) {
		
		if (monitoredMdItemType == MDItemType.BID)
			isBidCommited = true;
		if (monitoredMdItemType == MDItemType.OFFER)
			isAskCommited = true;
				
		if ((isBidCommited && isAskCommited) && priceChanged()) 
			return createIncrementalUpdate(productCode, messageId);
		else 
			return null;
	}
	
	private IncrementalUpdate createIncrementalUpdate(String productCode, long messageId) {

		IncrementalUpdate update = new IncrementalUpdate(messageId, convert(bidIds), convertLong(bidPrices), convert(bidSizes), convert(askIds), convertLong(askPrices),
				convert(askSizes), convert(deletedIds), productCode);
		
		update.setNewSnapshot(isNewSnapshot);

		clear();
		return update;
	}
	
	public void clear() {
		
		bidPrices.clear();
		bidIds.clear();
		bidSizes.clear();
		
		askPrices.clear();
		askIds.clear();
		askSizes.clear();
		
		deletedIds.clear();
		
		isBidCommited = false;
		isAskCommited = false;

		bidPriceChanged = false;
		askPriceChanged = false;
		deletedPriceChanged = false;
	}
	
	private boolean priceChanged() {
		
		return (bidPriceChanged || askPriceChanged || deletedPriceChanged);
	}
	
	private int[] convert(List<Integer> list) {
		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}

	private int[] convertLong(List<Long> list) {
		return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
	}
}
