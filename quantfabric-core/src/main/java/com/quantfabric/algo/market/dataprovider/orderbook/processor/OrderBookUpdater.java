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
package com.quantfabric.algo.market.dataprovider.orderbook.processor;

import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.OrderBookStorageWriter;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.OrderBookStorageWriter.OrderBookStorageWriterException;

class OrderBookUpdater
{					
	private final OrderBookStorageWriter orderBookStorage;
	
	public OrderBookUpdater(OrderBookStorageWriter orderBookStorage)
	{
		this.orderBookStorage = orderBookStorage;
	}
	
	public void newTrade(MDTrade mdTrade) throws OrderBookStorageWriterException {
		
		orderBookStorage.addTrade(mdTrade);
	}
				
	public void newPrice(MDPrice mdPrice) throws OrderBookStorageWriterException
	{			
		orderBookStorage.addPrice(mdPrice);
	}
	
	public void deletePrice(MDDelete mdDelete) throws OrderBookStorageWriterException
	{
		orderBookStorage.deletePrice(mdDelete);
	}
	
	public void endUpdate(EndUpdate endUpdate, boolean isMine) throws OrderBookStorageWriterException
	{	
		if (isMine)
			orderBookStorage.commit(endUpdate);
		else
			orderBookStorage.noUpdates(endUpdate);
	}
	
	public void newSnapshot(NewSnapshot newSnapshot) throws OrderBookStorageWriterException
	{
		orderBookStorage.clear();
	}
	
	public void sourceIsBroken() throws OrderBookStorageWriterException
	{
		orderBookStorage.clear();
		orderBookStorage.commit(-1, System.currentTimeMillis());
	}
} 