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
package com.quantfabric.algo.market.dataprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MarketConnectionAlert;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.gateway.MarketFeeder;

public abstract class BaseMarketDataItemsHandler 
	extends BaseFeedHandler implements MarketDataHandler
{
	private static final Logger log = LoggerFactory.getLogger(BaseMarketDataItemsHandler.class);
	public static Logger getLogger()
	{
		return log;
	}
	
	private final MDItemType monitoredMdItemType;
	private long currentMonitoredMessageId = 0;
	
	public BaseMarketDataItemsHandler(
			MarketFeeder marketFeeder, 
			FeedName feedName, 
			MDItemType monitoredMdItemType)
	{		
		super(marketFeeder, feedName);
		this.monitoredMdItemType = monitoredMdItemType;
		
		getLogger().debug(String.format("%s for feed %s(%s), was created", 
				this.getClass().getSimpleName(),
				feedName.getName(), monitoredMdItemType));
	}
		
	public MDItemType getMonitoredMdItemType()
	{
		return monitoredMdItemType;
	}
	
	protected long getCurrentMonitoredMessageId()
	{
		synchronized (this)
		{
			return currentMonitoredMessageId;
		}		
	}

	protected void setCurrentMonitoredMessageId(long currentMonitoredMessageId)
	{
		synchronized (this)
		{
			this.currentMonitoredMessageId = currentMonitoredMessageId;
		}
	}

	@Override
	public void sendUpdate(Object data)
	{
		if (data instanceof MDEvent)
			try
			{
				handleMdEvent((MDEvent)data);
			}
			catch (FeedHandlerException e)
			{
				getLogger().error("can't handle MDEvent", e);
			}
		else
			getLogger().debug(String.format("obtained not supported data (%s)", data.getClass()));
	}

	@Override
	public void sendUpdate(Object[] data)
	{
		for (int i = 0; i < data.length; i++)
			sendUpdate(data[i]);
	}
	
	@Override
	public void handleMdEvent(MDEvent event) throws FeedHandlerException
	{
		if (event instanceof MDItem)
		{			
			MDItem mdItem = (MDItem)event;
		
			setCurrentMonitoredMessageId(mdItem.getMessageId());
			
			if (!mdItem.getFeedName().equals(getMonitoredFeedName().getName()))
			{
				getLogger().error(String.format("obtained MDItem (%s) with wrong FeedName (expected:%s, actual:%s)",
						mdItem.getClass().getName(), getMonitoredFeedName().getName(), mdItem.getFeedName()));
			}
			else
				if (mdItem.getMdItemType() == monitoredMdItemType)
				{
					handleMdItem(mdItem);
				}
		}	
		else
			if (event instanceof EndUpdate)
			{
				EndUpdate endUpdate = (EndUpdate) event;
				
				boolean isMine = endUpdate.getMessageId() == getCurrentMonitoredMessageId();

				handleEndUpdate((EndUpdate)event, isMine);
			}
			else
				if (event instanceof NewSnapshot)
				{
					NewSnapshot newSnapshot = (NewSnapshot)event;
					setCurrentMonitoredMessageId(newSnapshot.getMessageId());
					handleNewSnapshot(newSnapshot);
				}
				else
					if (event instanceof MarketConnectionAlert)
					{
						handleMarketConnectionAlert((MarketConnectionAlert)event);
					}
					else
						getLogger().debug(String.format("obtained not supported MDEvent (%s)", 
								event.getClass()));
	}

	/*public abstract void handleMarketConnectionAlert(MarketConnectionAlert event) throws FeedHandlerException;
	public abstract void handleNewSnapshot(NewSnapshot event) throws FeedHandlerException;
	public abstract void handleEndUpdate(EndUpdate event) throws FeedHandlerException;
	public abstract void handleMdItem(MDFeedEvent mdItem) throws FeedHandlerException;*/
}
