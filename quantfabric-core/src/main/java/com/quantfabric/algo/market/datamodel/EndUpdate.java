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

public class EndUpdate extends MDMessageInfo
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3447647470608667402L;

	public EndUpdate()
	{
		super();
	}
	
	public EndUpdate(long timestamp, long messageId, MDMessageType messageType,
			String sourceName, long sourceTimestamp, int itemCount)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
	
	public EndUpdate(long messageId, MDMessageType messageType, 
			String sourceName, long sourceTimestamp, int itemCount)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
	
	public EndUpdate(long timestamp, long messageId, MDMessageType messageType,
			String sourceName, Date sourceTimestamp, int itemCount)
	{
		super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
	
	public EndUpdate(long messageId, MDMessageType messageType, 
			String sourceName, Date sourceTimestamp, int itemCount)
	{
		super(messageId, messageType, sourceName, sourceTimestamp, itemCount);
	}
}
