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
package com.quantfabric.algo.backtesting.player.events;

import com.quantfabric.algo.backtesting.player.track.TrackInfo;
import com.quantfabric.algo.market.datamodel.MDEvent;

public class StopPlayingEvent extends MDEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4728870796306947482L;
	private int trackNumber;
	
	public StopPlayingEvent(int trackNumber)
	{
		super();
		this.trackNumber = trackNumber;
	}
	
	public StopPlayingEvent()
	{
		this(TrackInfo.DEFAULT_TRACK_NUMBER);
	}

	public int getTrackNumber()
	{
		return trackNumber;
	}

	public void setTrackNumber(int trackNumber)
	{
		this.trackNumber = trackNumber;
	}
}
