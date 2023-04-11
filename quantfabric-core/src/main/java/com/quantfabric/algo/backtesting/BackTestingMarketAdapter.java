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
package com.quantfabric.algo.backtesting;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.quantfabric.algo.backtesting.eventbus.BackTestingEventAdapter;
import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.algo.backtesting.player.MDPTask;
import com.quantfabric.algo.backtesting.player.MarketDataPlayer;
import com.quantfabric.algo.backtesting.player.MarketDataPlayer.ContentListener;
import com.quantfabric.algo.backtesting.player.MarketDataPlayer.MarketDataPlayerException;
import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.gateway.BaseMarketAdapter;
import com.quantfabric.algo.runtime.QuantfabricRuntime;

public class BackTestingMarketAdapter extends BaseMarketAdapter
{
	private final AtomicInteger messageId = new AtomicInteger(0);
	private final AtomicInteger executionId = new AtomicInteger(0);
	
	private final MarketDataPlayer player;
		
	public BackTestingMarketAdapter()
	{
		this(new MarketDataPlayer(), new BackTestingCommandFactory());
	}
	
	public BackTestingMarketAdapter(BackTestingCommandFactory factory) {
		
		this(new MarketDataPlayer(), factory);
	}
	
	public BackTestingMarketAdapter(MarketDataCacheProvider storageProvider, List<MDPTask> playlist)
	{
		this(new MarketDataPlayer(storageProvider, playlist), new BackTestingCommandFactory());
	}
	
	public BackTestingMarketAdapter(MarketDataCacheProvider storageProvider)
	{
		this(new MarketDataPlayer(storageProvider), null);
	}
	
	public BackTestingMarketAdapter(MarketDataPlayer player, BackTestingCommandFactory factory)
	{
		super(factory);
		this.player = player;
					
		QuantfabricRuntime.getGlobalBackTestingEventBus().attachListener(
				new BackTestingEventAdapter() 
				{			
					@Override
					public void playBackTestingMarketData(BackTestingEvent event)
					{
						try
						{					
							if (BackTestingMarketAdapter.this.player.getStorageProvider() != null)
							{
								if (!doPlaying)
									BackTestingMarketAdapter.this.logon();
								else
									getLogger().error("Can't play. Still playing.");
							}
							
						}
						catch (Exception e)
						{
							getLogger().error("Play failed.", e);
						}						
					}
					
					@Override
					public void stopBackTestingMarketData(BackTestingEvent event)
					{
						try
						{				
							if (BackTestingMarketAdapter.this.player.getStorageProvider() != null)
							{
								if (doPlaying)
									BackTestingMarketAdapter.this.logout();
								else
									getLogger().error("Can't stop. Is not playing.");
							}
							
						}
						catch (Exception e)
						{
							getLogger().error("Stop failed.", e);
						}						
					}	
				});
		
		ContentListener contentListener = new ContentListener() {
			
			@Override
			public void onPlay(MDEvent event)
			{
				try
				{
					publish(event);
				}
				catch (PublisherException e)
				{
					getLogger().error("Publishing error", e);
				}				
			}

			@Override
			public void newLatencyOffset(long latencyOffset)
			{
				try
				{
					setMarketLatencyOffset(latencyOffset);
				}
				catch (PublisherException e)
				{
					getLogger().error("Update latency offset error", e);
				}				
			}

			@Override
			public void disconnected()
			{
				doPlaying = false;
				invokeLogonListenersByLogout();				
			}

			@Override
			public void connected()
			{
				doPlaying = true;
				invokeLogonListenersByLoggedIn();				
			}
		};
		
		this.player.addContentListener(contentListener);
	}
	
	public int getNextMessageId()
	{
		return messageId.incrementAndGet();
	}
	
	public String getNextExecutionId()
	{
		return String.valueOf(executionId.incrementAndGet());
	}
	
	public String getInstitutionOrderReference(String clientOrderReference)
	{
		return clientOrderReference;
	}
	
	public void setMarketDataCacheProvider(MarketDataCacheProvider storageProvider)
	{
		getPlayer().setStorageProvider(storageProvider);
	}
	
	@Override
	public void logon() throws MarketAdapterException
	{		
		try
		{
			player.play();
		}
		catch (MarketDataPlayerException e)
		{
			throw new MarketAdapterException(e);
		}
		doPlaying = true;
	}

	@Override
	public void logout()
	{
		player.stop();	
		doPlaying = false;
	}

	private boolean doPlaying = false;
	
	@Override
	public AdapterStatus getStatus()
	{
		return doPlaying ? AdapterStatus.CONNECTED : AdapterStatus.DISCONNECTED;
	}

	@Override
	public String getVenueName()
	{
		return "BackTesting";
	}

	public MarketDataPlayer getPlayer()
	{
		return player;
	}

	@Override
	public void setPassword(String password) throws MarketAdapterException
	{
		throw new MarketAdapterException("Provider doesn't use password property ");	
	}

	@Override
	public String getIdentifier()
	{
		return getVenueName();
	}
}
