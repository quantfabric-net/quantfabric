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
package com.quantfabric.algo.backtesting.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.player.MDPTask.RepeatMode;
import com.quantfabric.algo.backtesting.player.MDPTask.StartNotifyListener;
import com.quantfabric.algo.backtesting.player.events.StopPlayingEvent;
import com.quantfabric.algo.backtesting.player.track.FeedsTrack;
import com.quantfabric.algo.backtesting.player.track.FeedsTrack.Range;
import com.quantfabric.algo.backtesting.player.track.TrackInfo;
import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider.MarketDataCacheProviderException;
import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.NewSnapshot;

public class MarketDataPlayer
{	
	private final static Logger log = LoggerFactory.getLogger(MarketDataPlayer.class);
	
	public static class MarketDataPlayerException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1407471718793636842L;

		public MarketDataPlayerException()
		{
			super();
		}

		public MarketDataPlayerException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public MarketDataPlayerException(String message)
		{
			super(message);
		}

		public MarketDataPlayerException(Throwable cause)
		{
			super(cause);
		}
	}
	
	public enum PlayerState
	{
		STOPPED,
		INITIALIZING,
		INITIALIZED,
		PLAYING,
		FINISHED,
		STOPPING
	}
	
	public interface ContentListener
	{
		void onPlay(MDEvent event);
		void newLatencyOffset(long latencyOffset);
		void disconnected();
		void connected();
	}
	
	private final List<ContentListener> contentListeners = new ArrayList<ContentListener>();
	private MarketDataCacheProvider storageProvider;
	private List<MDPTask> playlist; 
	

	
	private volatile PlayerState playerState = PlayerState.STOPPED; 
	
	public PlayerState getPlayerState()
	{
		synchronized (playerState)
		{
			return playerState;
		}
	}
	
	private void setPlayerState(PlayerState playerState)
	{
		synchronized (playerState)
		{
			this.playerState = playerState;
		}
	}
	
	public MarketDataPlayer(MarketDataCacheProvider storageProvider, List<MDPTask> playlist)
	{
		this.storageProvider = storageProvider;
		this.playlist = playlist;
	}
	
	public MarketDataPlayer(MarketDataCacheProvider storageProvider)
	{
		this(storageProvider, new ArrayList<MDPTask>());
	}
	
	public MarketDataPlayer()
	{
		this(null, new ArrayList<MDPTask>());
	}

	public MarketDataCacheProvider getStorageProvider()
	{
		return storageProvider;
	}

	public boolean addContentListener(ContentListener listener)
	{
		synchronized (contentListeners)
		{
			return contentListeners.add(listener);
		}
	}
	
	public boolean removeContentListener(ContentListener listener)
	{
		synchronized (contentListeners)
		{
			return contentListeners.remove(listener);	
		}
	}
	
	private void invokeContentListeners(MDEvent event)
	{
		synchronized (contentListeners)
		{		
			for (ContentListener listener : contentListeners)
				listener.onPlay(event);
		}
	}
	
	private void notifyLatencyOffset(long latencyOffset)
	{
		synchronized (contentListeners)
		{
			for (ContentListener listener : contentListeners)
				listener.newLatencyOffset(latencyOffset);
		}
	}
	
	private void notifyDisconnected()
	{
		/*log.info("10 seconds pause before disconnect event");		
		try
		{						
			Thread.sleep(10000);		
		}
		catch (InterruptedException e)
		{
			log.error("pause process before disconnect event was iterrupted", e);
		}*/
		
		synchronized (contentListeners)
		{
			for (ContentListener listener : contentListeners)
				listener.disconnected();
		}
		
	/*	log.info("10 seconds pause after disconnect event");		
		try
		{						
			Thread.sleep(10000);		
		}
		catch (InterruptedException e)
		{
			log.error("pause process after disconnect event was iterrupted", e);
		}*/
	}
	
	private void notifyConnected()
	{
		/*log.info("10 seconds pause before connect event");		
		try
		{						
			Thread.sleep(10000);		
		}
		catch (InterruptedException e)
		{
			log.error("pause process before connect event was iterrupted", e);
		}*/
		
		synchronized (contentListeners)
		{
			for (ContentListener listener : contentListeners)
				listener.connected();		
		}
		
		/*log.info("10 seconds pause after connect event");
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			log.error("pause process after connect event was iterrupted", e);
		}*/

	}
	
	public void setStorageProvider(MarketDataCacheProvider storageProvider)
	{
		this.storageProvider = storageProvider;
	}
	public List<MDPTask> getPlaylist()
	{
		return playlist;
	}
	public void setPlaylist(List<MDPTask> playlist)
	{
		this.playlist = playlist;
	}
	/*public void addTaskToPlayList(MDPTask task) throws MarketDataPlayerException
	{
		this.playlist.add(task);
		initTask(task);
	}*/
	
	public void clearPlayList()
	{
		this.playlist.clear();
	}
	
	/*public void removeTaskFromPlayList(MDPTask task)
	{
		this.playlist.remove(task);
	}*/
		
	private class ContentPlayer extends Thread
	{		
		private final RepeatMode repaeatMode;
		private final boolean executeDelay;
		private final Iterator<MDItem> contentIterator;
		private final int trackNumber;
		
		@SuppressWarnings("unused")
		public ContentPlayer(RepeatMode repeatMode, boolean executeDelay,
				Collection<MDItem> content, int trackNumber, String suffixName)
		{
			this(repeatMode, executeDelay, content.iterator(), trackNumber, suffixName);
		}
		
		public ContentPlayer(RepeatMode repeatMode, boolean executeDelay,
				Iterator<MDItem> contentIterator, int trackNumber, String suffixName)
		{
			super("ContentPlayer-" + suffixName);
			this.contentIterator = contentIterator;
			this.repaeatMode = repeatMode;
			this.executeDelay = executeDelay;
			this.trackNumber = trackNumber;
			this.setDaemon(true);
		}
		
		private boolean doTerminate = false;
		
		public boolean doTerminate()
		{
			if (doTerminate)
				return false;
			
			doTerminate = true;
			return true;
		}
		
		private Iterator<MDItem> getContentIterator()
		{
			return contentIterator;
		}
		
		@Override
		public void run() 
		{
			if (doTerminate)
				return;
				
			log.info("ContentPlayer started");
			setPlayerState(PlayerState.PLAYING);
			notifyConnected();
			
			Iterator<MDItem> contenetIterator = getContentIterator();
						
			MDItem backItem = null;
						
			while (backItem == null)
			{
				if (contenetIterator.hasNext())
				{
					MDItem item = contenetIterator.next();
							
					if (item.getMdItemType() != MDItemType.BID && 
						item.getMdItemType() != MDItemType.OFFER)
					{
						publish(item);
						continue;
					}
					
					if (item.getMessageType() == MDMessageType.SNAPSHOT)
						publishNewSnapshot(item);
					
					notifyLatencyOffset(item.getTimestamp() - item.getSourceTimestamp());				
					
					publish(item);
					backItem = item;
				}
				else
					return;
			}
			
			while (!doTerminate && contenetIterator.hasNext())
			{
				MDItem item = contenetIterator.next();
				
				if (item.getMdItemType() != MDItemType.BID && 
					item.getMdItemType() != MDItemType.OFFER)
				{
					publish(item);
					continue;
				}
				
				/*if (item.getMessageId() < backItem.getMessageId())
				{					
					notifyDisconnected();
					notifyConnected();
				}*/
				
				//long delay = item.getTimestamp() - backItem.getTimestamp();
				
				//correctionTime(item);
				
				boolean needCheckForNewSnapshot = false; 
				if (backItem.getMessageId() != item.getMessageId())
				{
					publishEndUpdate(backItem);
					needCheckForNewSnapshot = true;
				}
				
				if (executeDelay)
					try 
					{										
						TimeUnit.MILLISECONDS.sleep(4); 
						//sleep(delay);
					} 
					catch (Exception e)
					{
						log.error("ContentPlayer error during delay executing", e);
					}
				
				if (needCheckForNewSnapshot && item.getMessageType() == MDMessageType.SNAPSHOT)
					publishNewSnapshot(item);
				
				publish(item);
				backItem = item;
				
				if (doTerminate || !contenetIterator.hasNext())
					publishEndUpdate(backItem);
			}
			
			if (repaeatMode == RepeatMode.LOOP && !doTerminate)
			{
				for (Map.Entry<MDPTask, ContentPlayer> entry : getContentPlayers().entrySet())
					if (entry.getValue() == this)
					{
						log.info("Going to replay last track");
						try
						{							
							startNotifyListener.onStart(entry.getKey());
							return;
						}
						catch (MarketDataPlayerException e)
						{
							log.error("Can't replay", e);
						}
					}
				
			}
						
			notifyDisconnected();
			publish(new StopPlayingEvent(trackNumber));	
			
			setPlayerState(PlayerState.FINISHED);
			log.info("ContentPlayer was stopped");	
		}
		
		private void publishEndUpdate(MDItem lastItem)
		{
			EndUpdate eu = new EndUpdate(
					lastItem.getTimestamp(),
					lastItem.getMessageId(),
					lastItem.getMessageType(),
					lastItem.getSourceName(),
					lastItem.getSourceTimestamp(),
					lastItem.getItemCount());
						
			publish(eu);
		}
		
		private void publishNewSnapshot(MDItem currentItem)
		{
			publish(new NewSnapshot(
					currentItem.getTimestamp(),
					currentItem.getMessageId(), 
					currentItem.getSourceName(), 
					currentItem.getSourceTimestamp(), 
					currentItem.getItemCount(), 
					currentItem.getSymbol(), 
					currentItem.getFeedId(), 
					currentItem.getFeedName()));
		}
	}
	
	/*private void correctionTime(MDItem item)
	{
		long localTime = item.getTimestamp();
		long sourceTime = item.getSourceTimestamp();
		
		long nowTime = Calendar.getInstance().getTimeInMillis();
		
		long diff = nowTime - localTime;
		
		sourceTime += diff;
		
		//item.setLocalTime(nowTime);
		//item.setSourceTime(sourceTime);
	}*/
	
	private void publish(MDEvent event)
	{		
		invokeContentListeners(event);
	}
	
	private final Map<MDPTask, ContentPlayer> contentPlayers = new HashMap<MDPTask, ContentPlayer>();
	
	protected Map<MDPTask, ContentPlayer> getContentPlayers()
	{
		return contentPlayers;
	}
	
	private final StartNotifyListener startNotifyListener = new StartNotifyListener()
	{			
		@Override
		public void onStart(MDPTask task) throws MarketDataPlayerException
		{	
			try
			{
				Iterator<MDItem> contentIterator = null;
				
				if (task.getTrack() instanceof FeedsTrack)
				{								
					FeedsTrack track = (FeedsTrack)task.getTrack();
					
					log.info(String.format("Initializing to play feeds track (ContextID = %s)", track.getContextId()));
							
					if (track.getRange() == Range.ALL)
						contentIterator = getStorageProvider().deferredLoad(
								track.getContextId(), 
								track.getFeeds());
					else
						contentIterator = getStorageProvider().deferredLoad(
							track.getContextId(),
							track.getFeeds(), 
							track.getRange().getFrom(),
							track.getRange().getTo());
				}
				
				if (contentIterator != null)
				{
					//if (task.isWaitEndPreviousTrack())
					//	notifyConnected();
					
					log.info(String.format("Going to play track " + task.getTrack().getTrackNumber()));
					playContent(task, contentIterator);
				}
			}
			catch (MarketDataCacheProviderException e)
			{
				throw new MarketDataPlayerException(e);
			}
		}
				
		private void playContent(MDPTask task, Iterator<MDItem> contentIterator)
		{
			if (getPlayerState() != PlayerState.INITIALIZED)
			{
				if (getPlayerState() == PlayerState.STOPPING)
				{
					log.info("MarketDataPlayer is stopping. Interrupt playContenet.");
					return;
				}
				log.error("MarketDataPlayer not initialized. Interrupt playContenet");
			}
			
			if (!contentIterator.hasNext())
			{
				log.warn("Content for playing is empty");
				notifyDisconnected();
				publish(new StopPlayingEvent(task.getTrack().getTrackNumber()));
				return;
			}
			
			ContentPlayer contentPlayer = new ContentPlayer(
					task.getRepeatMode(),
					task.getExecuteDelay(),
					contentIterator, 
					task.getTrack().getTrackNumber(),
					String.valueOf(getContentPlayers().size()));
			
			getContentPlayers().put(task, contentPlayer);
			
			contentPlayer.start();
		}
	};
	
	private void initTask(MDPTask task, int playAfterTrack) throws MarketDataPlayerException
	{
		if (task.addStartNotifyListener(startNotifyListener));
			task.setupAlarm(playAfterTrack, this);
	}
	
	private void initTask(MDPTask task) throws MarketDataPlayerException
	{
		if (task.addStartNotifyListener(startNotifyListener));
			task.setupAlarm();
	}
	
	public void play() throws MarketDataPlayerException
	{
		if (getPlayerState() != PlayerState.STOPPED &&  getPlayerState() != PlayerState.FINISHED)
			throw new MarketDataPlayerException("Can't do play because player does not stopped.");
		
		setPlayerState(PlayerState.INITIALIZING);
		
		if (getStorageProvider() == null)
			log.warn("Called Play but not set MarketDataStorageProvider");
		
		Collections.sort(playlist, new Comparator<MDPTask>()
				{
					@Override
					public int compare(MDPTask o1, MDPTask o2)
					{
						return ((Integer)o1.getTrack().getTrackNumber()).compareTo(o2.getTrack().getTrackNumber());
					}
				});		
		
		int prevTrackNumber = TrackInfo.DEFAULT_TRACK_NUMBER;
		for (MDPTask task : playlist)
		{
			if (prevTrackNumber != TrackInfo.DEFAULT_TRACK_NUMBER)
				initTask(task, prevTrackNumber);
			else
				initTask(task);
			
			prevTrackNumber = task.getTrack().getTrackNumber();
		}
		
		setPlayerState(PlayerState.INITIALIZED);
	}
	
	public void stop()
	{			
		if (getPlayerState() == PlayerState.STOPPING || getPlayerState() == PlayerState.STOPPED)
			return;
			
		setPlayerState(PlayerState.STOPPING);
		
		for (MDPTask task : playlist)
		{			
			task.cancelAlarm();		
			
			ContentPlayer cp = getContentPlayers().get(task);
			if (cp != null)
			{
				if (cp.doTerminate())
					try
					{
						cp.join();
					}
					catch (InterruptedException e)
					{
						log.error("Error during termination of contentPlayer", e);
					}
			}
			
			task.removeStartNotifyListener(startNotifyListener);			
		}
		getContentPlayers().clear();
		
		setPlayerState(PlayerState.STOPPED);
	}
}
