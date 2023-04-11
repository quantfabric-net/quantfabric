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

public class LatencyOffset extends MDEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6068324452821777836L;
	private long latencyOffset;
	
	public LatencyOffset()
	{	
		super();
	}
	
	public LatencyOffset(long latencyOffset)
	{
		super();
		setLatencyOffset(latencyOffset);
	}

	public LatencyOffset(Date timestamp, long latencyOffset)
	{
		super(timestamp);
		setLatencyOffset(latencyOffset);
	}

	public LatencyOffset(long timestamp, long latencyOffset)
	{
		super(timestamp);
		setLatencyOffset(latencyOffset);
	}

	public long getLatencyOffset()
	{
		return latencyOffset;
	}

	public void setLatencyOffset(long latencyOffset)
	{
		this.latencyOffset = latencyOffset;
	}	
}
