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
import com.quantfabric.algo.market.datamodel.MDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.datamodel.MarketConnectionAlert;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.datamodel.StatusChanged.MarketConnectionStatuses;
import com.quantfabric.algo.market.dataprovider.BaseMarketDataItemsHandler;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.OrderBookStorageWriter;
import com.quantfabric.algo.market.dataprovider.orderbook.storage.OrderBookStorageWriter.OrderBookStorageWriterException;
import com.quantfabric.algo.market.gateway.MarketFeeder;

public class OrderBookProcessor extends BaseMarketDataItemsHandler
{
	private final OrderBookUpdater orderBookUpdater;
	private final String name;
	
	public OrderBookProcessor(MarketFeeder marketFeeder, OrderBookStorageWriter orderBookStorage)
	{		
		super(marketFeeder, orderBookStorage.getFeedName(), 
				getMonitoredMdItemType(orderBookStorage.getOrderBookType()));

		this.name = String.format("OrderBookProcessor-%s-%s", 
				orderBookStorage.getFeedName().getName(), orderBookStorage.getOrderBookType());
		
		this.orderBookUpdater = 
			new OrderBookUpdater(orderBookStorage);
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void handleMarketConnectionAlert(MarketConnectionAlert event) throws FeedHandlerException
	{
		if (event instanceof StatusChanged)	
		{
			StatusChanged statusChangedEvent = (StatusChanged)event;
			if (statusChangedEvent.getMarketConnectionStatus() == 
					MarketConnectionStatuses.DISCONNECTED) try
			{
				orderBookUpdater.sourceIsBroken();
			}
			catch (OrderBookStorageWriterException e)
			{
				throw new FeedHandlerException(e);
			}
		}
	}

	@Override
	public void handleNewSnapshot(NewSnapshot event) throws FeedHandlerException
	{
		try
		{
			orderBookUpdater.newSnapshot(event);
		}
		catch (OrderBookStorageWriterException e)
		{
			throw new FeedHandlerException(e);
		}		
	}

	@Override
	public void handleEndUpdate(EndUpdate event, boolean isMine) throws FeedHandlerException
	{	
		try
		{
			orderBookUpdater.endUpdate(event, isMine);
		}
		catch (OrderBookStorageWriterException e)
		{
			throw new FeedHandlerException(e);
		}	
	}

	@Override
	public void handleMdItem(MDFeedEvent mdItem) throws FeedHandlerException
	{
		if (mdItem instanceof MDTrade) {
			try	{
				orderBookUpdater.newTrade((MDTrade) mdItem);
			}
			catch (OrderBookStorageWriterException e) {
				
				throw new FeedHandlerException(e);
			}
		}		
		else if (mdItem instanceof MDPrice)
			try
			{
				orderBookUpdater.newPrice((MDPrice)mdItem);
			}
			catch (OrderBookStorageWriterException e)
			{
				throw new FeedHandlerException(e);
			}
		else
			if (mdItem instanceof MDDelete) 
				try
				{
					orderBookUpdater.deletePrice((MDDelete)mdItem);
				}
				catch (OrderBookStorageWriterException e)
				{
					throw new FeedHandlerException(e);
				}		
	}	
	
	private static MDItemType getMonitoredMdItemType(OrderBookInfo.OrderBookTypes orderBookType)
	{
		switch (orderBookType)
		{
			case BID_BOOK:
				return MDItemType.BID;
			case OFFER_BOOK:
				return MDItemType.OFFER;
			case TRADE:
				return MDItemType.TRADE;
			default : return MDItemType.UNKNOWN;
		}
	}
}
