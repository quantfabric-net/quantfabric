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

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.event.map.MapEventBean;
import com.espertech.esper.event.map.MapEventType;

public abstract class EsperEventBeanUtils
{	
	public static Object getFieldValue(EventBean bean, String name)
	{	
		Object value = null;
		
		if (bean.getClass() == MapEventBean.class)
			value = getFieldValue((MapEventBean)bean, name);
		else
			value = bean.get(name);
									
		if (value != null && value.getClass().isEnum())
			value = value.toString();
		
		return value;
	}
	
	@SuppressWarnings("rawtypes")
	public static Object getFieldValue(MapEventBean bean, String name)
	{
		int delimIndex = name.indexOf("%");
		String sub_name = delimIndex != -1 ? name.substring(0, delimIndex) : name;
		String field_name = delimIndex != -1 ? name.substring(delimIndex + 1) : name;
		
		Object objBean = bean.get(sub_name);
		
		if (objBean instanceof EventBean)
		{
			EventBean eventBean = (EventBean)objBean;
			return getFieldValue(eventBean, field_name);
		}
		if (objBean instanceof Map)
		{
			return ((Map)objBean).get(field_name);
		}			
		else
			return objBean;
	}
		
	public static Map<String, Class<?>> getProperties(EventType eventType)
	{
		HashMap<String, Class<?>> properties = new HashMap<String, Class<?>>(); 
				
		if (eventType.getClass() == MapEventType.class)
		{
			MapEventType mapEventType = (MapEventType)eventType;
			
			for (Map.Entry<String, Object> eventTypeObjectEntry : 
				mapEventType.getTypes().entrySet())
			{				
				if (eventTypeObjectEntry.getValue().getClass() == Class.class)
				{
					Class<?> classValue = (Class<?>)eventTypeObjectEntry.getValue();	
					properties.put(eventTypeObjectEntry.getKey(), 
								classValue.isEnum() ? String.class : classValue);
				}
				
				if (eventTypeObjectEntry.getValue() instanceof EventType)
				{
					Object objEventType = eventTypeObjectEntry.getValue();
					
					Map<String, Class<?>> sub_properties = 
						getProperties((EventType)objEventType);
					for (Map.Entry<String, Class<?>> sub_property : sub_properties.entrySet())
						properties.put(eventTypeObjectEntry.getKey() + "%" + sub_property.getKey(),
								sub_property.getValue());
				}
				
			}			
		}
		else
		{		
			String[] propertyNames = eventType.getPropertyNames();
			
			for (int i = 0; i < propertyNames.length; i++)
			{
				String propertyName = propertyNames[i];
				Class<?> propertyType = eventType.getPropertyType(propertyNames[i]);
				
				if (propertyType.isEnum())
					propertyType = String.class;
				
				properties.put(propertyName, propertyType);
			}
		}
		return properties;			
	}
}
