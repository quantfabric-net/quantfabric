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
package com.quantfabric.net.stream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class Event
{	
	private long creationTime;
	private long sendingTime;
	private String source;
	private Object eventBean;
		
	public Event(){}
	
	public Event(String source, Object eventBean)
	{
		super();
		this.creationTime = GregorianCalendar.getInstance().getTime().getTime();
		this.source = source;
		this.eventBean = eventBean;
	}
	
	public String getSource()
	{
		return source;
	}
	public void setSource(String source)
	{
		this.source = source;
	}
	public Object getEventBean()
	{
		return eventBean;
	}
	public void setEventBean(Object eventBean)
	{
		this.eventBean = eventBean;
	}

	public long getCreationTime()
	{
		return creationTime;
	}

	public long getSendingTime()
	{
		return sendingTime;
	}

	public void setCreationTime(long creationTime)
	{
		this.creationTime = creationTime;
	}

	public void setSendingTime(long sendingTime)
	{
		this.sendingTime = sendingTime;
	}



	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z(Z)");
	
	@Override
	public String toString()
	{
		return " creationTime=" + timeFormat.format(creationTime) + "; sendingTime=" + timeFormat.format(sendingTime)
				+ "; source=" + source + "; eventBean=" + eventBean + ";";
	}
}
