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
package com.quantfabric.persistence.esper;

import com.espertech.esper.client.EventBean;
import com.quantfabric.persistence.ConsoleStorageProvider;
import com.quantfabric.persistence.DataAdapter;

public class EsperConsoleStorageProvider extends ConsoleStorageProvider
{
	DataAdapter dataAdapter = new DataAdapter()
	{
		public Object adapt(Object object) throws AdaptationException
		{
			if (object instanceof EventBean)
			{
				EventBean eventBean = (EventBean)object;
				StringBuilder buf = new StringBuilder();
		        buf.append(" Type=" + eventBean.getEventType().getName());//.getEventType().getUnderlyingType().getSimpleName());
		        for (String name : eventBean.getEventType().getPropertyNames())
		        {
		            buf.append(' ');
		            buf.append(name);
		            buf.append("=");
		            buf.append(eventBean.get(name));
		        }
		        return buf.toString();
			}
			throw new AdaptationException("Not support type " + object.getClass().getName());
		}
		
	};
	
	public DataAdapter getDataApdapter()
	{
		return dataAdapter;
	}

	public void dispose() throws StoragingException
	{
		super.dispose();	
	}

	@Override
	public void erase() throws StoragingException
	{
	}
}
