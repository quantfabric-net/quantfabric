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
package com.quantfabric.algo.market.gateway.feed;

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.dataprovider.FeedName;

public abstract class AbstractFeed implements Feed
{
	public static final int DEFAULT_FEED_GROUP_ID = 0;
	public static final int FEED_ID_NOT_SET = 0;

	private String instrumentId;
	private Instrument instrument = null;
	private InstrumentProvider instrumentProvider;

	private boolean cachingInstrument;
	private FeedName feedName;	
	private boolean saveData = false;
	private int feedGroupId = DEFAULT_FEED_GROUP_ID;
	
	public AbstractFeed(FeedName feedName)
	{
		super();
		this.feedName = feedName;
	}
	
	public AbstractFeed(FeedName feedName, int feedGroupId)
	{
		super();
		this.feedName = feedName;
		this.feedGroupId = feedGroupId;
	}

	public AbstractFeed(FeedName feedName, InstrumentProvider instrumentProvider, String instrumentId, boolean cachingInstrument) {
		super();
		this.feedName = feedName;
		this.instrumentProvider = instrumentProvider;
		this.instrumentId = instrumentId;
		this.cachingInstrument = cachingInstrument;
	}
	
	public boolean isSaveData()
	{
		return saveData;
	}

	public void setSaveData(boolean doSaveData)
	{
		this.saveData = doSaveData;
	}
	
	public FeedName getFeedName()
	{
		return feedName;
	}
	
	public int getFeedId()
	{
		return hashCode();
	}
	
	public int getFeedGroupId()
	{
		return feedGroupId;
	}

	public void setFeedGroupId(int feedGroupId)
	{
		this.feedGroupId = feedGroupId;
	}

	@Override
	public String getInstrumentId() {
		return instrumentId;
	}

	@Override
	public Instrument getInstrument() {
		return instrument == null ? lookupInstrument(instrumentId) : instrument;
	}
	@Override
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}
	@Override
	public void setInstrumentId(String instrumentId) {
		this.instrumentId = instrumentId;
	}

	public InstrumentProvider getInstrumentProvider() {
		return instrumentProvider;
	}

	public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
		this.instrumentProvider = instrumentProvider;
	}

	public boolean isCachingInstrument() {
		return cachingInstrument;
	}

	public void setCachingInstrument(boolean cachingInstrument) {
		this.cachingInstrument = cachingInstrument;
	}

	public void setFeedName(FeedName feedName) {
		this.feedName = feedName;
	}

	private Instrument lookupInstrument(String instrumentId)
	{
		Instrument i = instrumentProvider.getInstrument(instrumentId);

		if (cachingInstrument)
			this.instrument = i;

		return i;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Feed)
			return feedName.equals(((Feed)obj).getFeedName());
		
		return false;
	}

	@Override
	public int hashCode()
	{
		return feedName.hashCode();
	}
}
