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
package com.quantfabric.algo.market.dataprovider.orderbook.storage;

import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;

public interface OrderBookStorageWriter extends OrderBookInfo
{
	class OrderBookStorageWriterException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -491248074189397700L;

		public OrderBookStorageWriterException()
		{
			super();
		}
		public OrderBookStorageWriterException(String message, Throwable cause)
		{
			super(message, cause);
		}
		public OrderBookStorageWriterException(String message)
		{
			super(message);
		}
		public OrderBookStorageWriterException(Throwable cause)
		{
			super(cause);
		}
	}
	
	void addPrice(MDPrice mdPrice);
	void addTrade(MDTrade mdTrade);
	void deletePrice(MDDelete mdDelete);
	void clear();
	void commit(long snapshotId, long sourceTimestamp) throws OrderBookStorageWriterException;
	void commit(EndUpdate endUpdate) throws OrderBookStorageWriterException;
	void noUpdates(EndUpdate endUpdate) throws OrderBookStorageWriterException;
}