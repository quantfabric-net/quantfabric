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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.player.MarketDataPlayer.ContentListener;
import com.quantfabric.algo.backtesting.player.MarketDataPlayer.MarketDataPlayerException;
import com.quantfabric.algo.backtesting.player.events.StopPlayingEvent;
import com.quantfabric.algo.backtesting.player.track.TrackInfo;
import com.quantfabric.algo.market.datamodel.MDEvent;


public class MDPTask
{
	public interface StartNotifyListener
	{
		void onStart(MDPTask task) throws MarketDataPlayerException;
	}
	
	public static final Date START_IMMEDIATELY = null;
	public static final long DEFAULT_DELAY_BEFORE_START = 5000;
	public static final boolean DEFAULT_EXECUTE_DELAY = true;
	
	public enum RepeatMode
	{
		ONCE,
		LOOP
	}

	private static final Logger log = LoggerFactory.getLogger(MDPTask.class);
	
	private Date startTime;
	private RepeatMode repeatMode;
	private TrackInfo track;
	private long delayBeforeStart;
	private boolean executeDelay;
	private boolean isWaitEndPreviousTrack;
	
	private final List<StartNotifyListener> startNotifyListeners = new ArrayList<StartNotifyListener>();
	
	public MDPTask(TrackInfo track, RepeatMode repeatMode, Date startTime, long delayBeforeStart)
	{
		setTrack(track);
		setRepeatMode(repeatMode);
		setStartTime(startTime);
		setDelayBeforeStart(delayBeforeStart);
		setExecuteDelay(DEFAULT_EXECUTE_DELAY);
	}	
	
	public MDPTask(TrackInfo track, RepeatMode repeatMode)
	{
		this(track, repeatMode, START_IMMEDIATELY, DEFAULT_DELAY_BEFORE_START);
	}

	public MDPTask(TrackInfo track, Date startTime)
	{
		this(track, RepeatMode.ONCE, startTime, DEFAULT_DELAY_BEFORE_START);
	}

	public MDPTask(TrackInfo track)
	{
		this(track, RepeatMode.ONCE);
	}

	private void invokeAllStartNotifyListeners() throws MarketDataPlayerException
	{
		synchronized (startNotifyListeners)
		{	
			for (StartNotifyListener listener : startNotifyListeners)
				listener.onStart(this);
		}
	}
	
	public boolean isWaitEndPreviousTrack()
	{
		return isWaitEndPreviousTrack;
	}

	private Timer alarm = null;
	
	public void setupAlarm(final int waitTrackNumber, MarketDataPlayer mdp)
	{
		this.isWaitEndPreviousTrack = true;
		mdp.addContentListener(new ContentListener() 
		{
			
			@Override
			public void onPlay(MDEvent event)
			{
				if (event instanceof StopPlayingEvent)
				{
					StopPlayingEvent stopEvent = (StopPlayingEvent)event;
					if (stopEvent.getTrackNumber() == waitTrackNumber) try
					{
						log.info(String.format("Track %d going to start", getTrack().getTrackNumber()));
						setupAlarm();
					}
					catch (MarketDataPlayerException e)
					{
						log.error(e.getMessage());
					}
				}
			}
			
			@Override
			public void newLatencyOffset(long latencyOffset){}			
			@Override
			public void disconnected(){}			
			@Override
			public void connected(){}
		});
	}
	
	public void setupAlarm() throws MarketDataPlayerException
	{		
		this.alarm = new Timer();
		TimerTask alarmTask = new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{					
					log.info("!!!invokeAllStartNotifyListeners!!!");
					invokeAllStartNotifyListeners();
				}
				catch (MarketDataPlayerException e)
				{
					log.error(e.getMessage());
				}		
			}
		};		
		//cancelAlarm();
					
		if (getStartTime() == START_IMMEDIATELY)
		{
			alarm.schedule(alarmTask, getDelayBeforeStart());
			log.info(String.format("Track %d will be start after %d sec.", getTrack().getTrackNumber(), getDelayBeforeStart() / 1000)); 
		}
		else
		{
			alarm.schedule(alarmTask, getStartTime());
			log.info(String.format("Track %d will be start at %s", getTrack().getTrackNumber() , getStartTime()));
		}
	}
	
	public void cancelAlarm()
	{
		if (alarm != null)
		{
			log.info("!!!CANCEL ALRAM!!!");
			alarm.cancel();
		}
	}
	
	public boolean addStartNotifyListener(StartNotifyListener listener)
	{		
		synchronized (startNotifyListeners)
		{	
			return startNotifyListeners.add(listener);
		}
	}
	
	public boolean removeStartNotifyListener(StartNotifyListener listener)
	{
		synchronized (startNotifyListeners)
		{	
			log.info("!!!removeStartNotifyListener!!! (" + listener.toString() + ")");
			return startNotifyListeners.remove(listener);
		}
	}
	
	public Date getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	public RepeatMode getRepeatMode()
	{
		return repeatMode;
	}

	public void setRepeatMode(RepeatMode repeatMode)
	{
		this.repeatMode = repeatMode;
	}

	public TrackInfo getTrack()
	{
		return track;
	}

	public void setTrack(TrackInfo track)
	{
		this.track = track;
	}
	
	public long getDelayBeforeStart()
	{
		return delayBeforeStart;
	}

	public void setDelayBeforeStart(long delayBeforeStart)
	{
		this.delayBeforeStart = delayBeforeStart;
	}

	public void setExecuteDelay(boolean executeDelay)
	{
		this.executeDelay = executeDelay;		
	}
	
	public boolean getExecuteDelay()
	{
		return executeDelay;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MDPTask)
		{
			MDPTask taskObj = (MDPTask)obj;
			return taskObj.repeatMode == this.repeatMode &&
					taskObj.startTime == this.startTime &&
					taskObj.track.equals(this.track);
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return track.hashCode();
	}	
}
