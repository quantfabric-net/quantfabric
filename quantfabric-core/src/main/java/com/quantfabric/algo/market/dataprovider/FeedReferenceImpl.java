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

public class FeedReferenceImpl implements FeedReference
{
	public static final boolean DEFAULT_ENABLE_STATUS = true;
	
	private FeedName feedName;
	private String connectionName;
	
	private boolean enable;
	
	private boolean connected = false;
	
	public boolean isEnable()
	{
		return enable;
	}
	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}
	public boolean isConnected()
	{
		return connected;
	}
	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}
	public FeedName getFeedName()
	{
		return feedName;
	}
	public void setFeedName(FeedName feedName)
	{
		this.feedName = feedName;
	}
	public String getConnectionName()
	{
		return connectionName;
	}
	public void setConnectionName(String connectionName)
	{
		this.connectionName = connectionName;
	}
	
	public FeedReferenceImpl(FeedName feedName, String connectionName)
	{
		this(feedName, connectionName, DEFAULT_ENABLE_STATUS);
	}
	
	public FeedReferenceImpl(FeedName feedName, String connectionName, boolean enable)
	{
		super();
		this.feedName = feedName;
		this.connectionName = connectionName;
		this.enable = enable;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FeedReference)
		{
			FeedReference feed = (FeedReference)obj;
			return this.feedName.equals(feed.getFeedName().getName()) && 
				this.connectionName.equals(feed.getConnectionName());
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return feedName.hashCode() + connectionName.hashCode();
	}
	@Override
	public String toString()
	{
		return "[feedName=" + feedName + ", connectionName="
				+ connectionName + ", enable=" + enable + ", connected="
				+ connected + "]";
	}	
}
