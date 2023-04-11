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

import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.util.Named;

public interface OrderBookSnapshotListener extends Named
{
	class OrderBookSnapshotListenerException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7265415750812590906L;

		public OrderBookSnapshotListenerException()
		{
			super();
		}
		public OrderBookSnapshotListenerException(String message,
				Throwable cause)
		{
			super(message, cause);
		}
		public OrderBookSnapshotListenerException(String message)
		{
			super(message);
		}
		public OrderBookSnapshotListenerException(Throwable cause)
		{
			super(cause);
		}
	}
	
	void onNewSnapshot(OrderBookSnapshot orderBookSnapshot) 
		throws OrderBookSnapshotListenerException;
	
	void onEndUpdate(OrderBookInfo orderBookInfo, long updateId, boolean isBookModified);
	
	void onNoUpdate(long snapshotId);
}