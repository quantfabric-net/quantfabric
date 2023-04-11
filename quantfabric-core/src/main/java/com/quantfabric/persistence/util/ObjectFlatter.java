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
package com.quantfabric.persistence.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ObjectFlatter
{
	public static boolean typeIsPersitable(Collection<Class<?>> persistableClasses, Class<?> clazz)
	{
        return clazz.isPrimitive() || persistableClasses.contains(clazz);
	}
	
	public static Collection<PropertyWrapper> getFlatStructure(Class<?> clazz, Collection<Class<?>> persistableClasses) 
	{
		return getFlatStructure(clazz, null, persistableClasses);
	}
	
	public static Collection<PropertyWrapper> getFlatStructure(
			Class<?> clazz, PropertyWrapper parent, Collection<Class<?>> persistableClasses) 
	{
		List<PropertyWrapper> classFields = new LinkedList<PropertyWrapper>();
		
		/*if (clazz.isAssignableFrom(Map.class))
		{
			PropertyWrapper propertyWrapper = new MapWrapper("Map", String.class, parent);			
			classFields.add(propertyWrapper);
		}
		else*/		
			if (clazz.isArray()) {
	
				PropertyWrapper propertyWrapper = new ArrayWrapper("Array", String.class, parent);
				
				classFields.add(propertyWrapper);
				
			} else {
			
			Method[] methods = clazz.getMethods();
	
			for (int i=0; i<methods.length; i++)
				if (!methods[i].getName().equals("getClass"))
				{
					String fieldName = null;
					Class<?> fieldType = null;
					
					if (methods[i].getName().startsWith("get")) 
					{
						fieldName = methods[i].getName().substring(3);
						fieldType = methods[i].getReturnType();
					}
						if (methods[i].getName().startsWith("is")) 
						{
							fieldName = methods[i].getName();
							fieldType = methods[i].getReturnType();
						}
					
					if (fieldName != null)
					{
						if (fieldType.isEnum())
							fieldType = String.class;
						
						if (parent != null)
							fieldName = parent.getName() + "." + fieldName;
						
						PropertyWrapper propertyWrapper = 
							new GetterWrapper(fieldName, fieldType, methods[i], parent);
						
						if (!typeIsPersitable(persistableClasses, fieldType))
						{
							classFields.addAll(getFlatStructure(fieldType, propertyWrapper, persistableClasses));
						}
						else
							classFields.add(propertyWrapper);
					}
				}
			}
		
		return classFields;		
	}
}
