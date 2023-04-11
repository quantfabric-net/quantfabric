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
package com.quantfabric.util;

import java.util.Properties;

public class PropertiesViewer
{
	public static class NotSpecifiedProperty extends Exception
	{
		private static final long serialVersionUID = 1734988180785535415L;

		public NotSpecifiedProperty(String propertyName)
		{
			super(String.format("Property \"%s\" does not specified." , propertyName));
		}
	}
	
	public static String getProperty(
			Properties properties,
			String propertyName) throws NotSpecifiedProperty
	{
		if (properties.containsKey(propertyName))
			return properties.getProperty(propertyName);
		else
			throw new NotSpecifiedProperty(propertyName);
			
	}
	
	public static String getProperty(
			Properties properties, 
			String propertyName, 
			String defaultValue)
	{
		return properties.getProperty(propertyName, defaultValue);
	}
	
	public static String[] getMultiProperty(Properties properties, 
			String propertyName, Character serparator) throws NotSpecifiedProperty
	{
		String value = getProperty(properties, propertyName);		
		return parseMultiString(value, serparator);
	}
	
	private static String[] parseMultiString(String value, Character serparator)
	{
		String[] values = value.trim().split(serparator.toString());
		
		for (int i = 0; i < values.length; i++)
			values[i] = values[i].trim();
		
		return values;
	}
	
	public static String[] getMultiProperty(Properties properties, 
			String propertyName, Character serparator, String defaultValue)
	{
		try
		{
			return getMultiProperty(properties, propertyName, serparator);
		}
		catch (NotSpecifiedProperty e)
		{
			return parseMultiString(defaultValue, serparator);
		}
	}
}
