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

import java.util.Date;

public class MDDelete extends MDItem
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1749367976304185140L;

	public MDDelete()
	{
		super();
	}
	
	public MDDelete(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType, 
			String deletedItemId, String symbol, int feedId)
	{
		super(timestamp, messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, 
				deletedItemId, symbol, feedId);
	}

	public MDDelete(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String symbol, int feedId)
	{
		super(timestamp, messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
	}

	public MDDelete(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType, 
			String deletedItemId, String symbol, int feedId)
	{
		super(timestamp, messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, deletedItemId, symbol, feedId);
	}

	public MDDelete(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, int itemCount, int itemIndex, MDItemType mdItemType, 
			String symbol, int feedId)
	{
		super(timestamp, messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
	}

	public MDDelete(long messageId, String sourceName, Date sourceTimestamp,
			int itemCount, int itemIndex, 
			MDItemType mdItemType, String deletedItemId, String symbol, int feedId)
	{
		super(messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, deletedItemId, symbol, feedId);
	}

	public MDDelete(long messageId, String sourceName, Date sourceTimestamp,
			int itemCount, int itemIndex, MDItemType mdItemType, String symbol, int feedId)
	{
		super(messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
	}

	public MDDelete(long messageId, String sourceName, long sourceTimestamp,
			int itemCount, int itemIndex, 
			MDItemType mdItemType, String deletedItemId, String symbol, int feedId)
	{
		super(messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, deletedItemId, symbol, feedId);
	}

	public MDDelete(long messageId, String sourceName, long sourceTimestamp,
			int itemCount, int itemIndex, 
			MDItemType mdItemType, String symbol, int feedId)
	{
		super(messageId, MDMessageType.INCREMENTAL_REFRESH,
				sourceName, sourceTimestamp, itemCount, itemIndex,
				mdItemType, symbol, feedId);
	}	
}
