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
package com.quantfabric.algo.backtesting.player.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.quantfabric.algo.market.dataprovider.FeedName;

public class FeedsTrack extends TrackInfo
{	
	public static class Range
	{	
		private static Date maxDate()
		{
			Calendar c = Calendar.getInstance(); 
			c.set(
                c.getActualMaximum(Calendar.YEAR), 
                c.getActualMaximum(Calendar.MONTH), 
                c.getActualMaximum(Calendar.DAY_OF_MONTH), 
                c.getActualMaximum(Calendar.HOUR), 
                c.getActualMaximum(Calendar.MINUTE), 
                c.getActualMaximum(Calendar.SECOND)
		    );
	        c.set(Calendar.MILLISECOND, c.getActualMaximum(Calendar.MILLISECOND));
	        return c.getTime();
		}
		
		private static Date minDate()
		{
			Calendar c = Calendar.getInstance(); 
			c.set(
	                c.getActualMinimum(Calendar.YEAR), 
	                c.getActualMinimum(Calendar.MONTH), 
	                c.getActualMinimum(Calendar.DAY_OF_MONTH), 
	                c.getActualMinimum(Calendar.HOUR), 
	                c.getActualMinimum(Calendar.MINUTE), 
	                c.getActualMinimum(Calendar.SECOND)
	            );

	        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
	        return c.getTime();
		}
		
		public static final Date MAX_BOUND = maxDate();
		public static final Date MIN_BOUND = minDate();
		public static final Range ALL = new Range(MIN_BOUND, MAX_BOUND); 
		
		private Date from;
		private Date to;
		
		public Range(Date from, Date to)
		{
			super();
			this.from = from;
			this.to = to;
		}

		public Date getFrom()
		{
			return from;
		}

		public void setFrom(Date from)
		{
			this.from = from;
		}

		public Date getTo()
		{
			return to;
		}

		public void setTo(Date to)
		{
			this.to = to;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Range)
			{
				Range rangeObj = (Range)obj;
				return rangeObj.from == this.from &&
						rangeObj.to == this.to;
			}
			
			return false;
		}

		@Override
		public int hashCode() {
			return from.hashCode() + to.hashCode();
		}
	}

	private Range range = Range.ALL;
	private int contextId;
	private final Collection<FeedName> feeds;
	
	public FeedsTrack(int trackNumber, int contextId, Collection<FeedName> feeds, Range range)
	{
		super(trackNumber);
		this.contextId = contextId;
		this.range = range;
		this.feeds = feeds;
	}
	
	public FeedsTrack(int contextId, Collection<FeedName> feeds, Range range)
	{
		this(DEFAULT_TRACK_NUMBER, contextId, feeds, range);
	}
	
	public FeedsTrack(int trackNumber, int contextId, Collection<FeedName> feeds)
	{
		this(trackNumber, contextId, feeds, Range.ALL);
	}
	
	public FeedsTrack(int trackNumber, int contextId, FeedName feed)
	{
		this(trackNumber, contextId, feed, Range.ALL);
	}

	public FeedsTrack(int trackNumber, int contextId, FeedName feed, Range range)
	{
		this(trackNumber, contextId, encapsulateFeed(feed), Range.ALL);
	}
	
	public FeedsTrack(int trackNumber, int contextId, Range range)
	{
		this(trackNumber, contextId, new ArrayList<FeedName>(), range);
	}
	
	public int getContextId()
	{
		return contextId;
	}

	public void setContextId(int contextId)
	{
		this.contextId = contextId;
	}
	
	public Range getRange()
	{
		return range;
	}

	public void setRange(Range range)
	{
		this.range = range;
	}
	
	private static Collection<FeedName> encapsulateFeed(FeedName feed)
	{
		Collection<FeedName> feeds = new ArrayList<FeedName>();
		feeds.add(feed);
		return feeds; 
	}
	
	public FeedName[] getFeeds()
	{
		return feeds.toArray(new FeedName[0]);
	}
}
