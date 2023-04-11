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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;


public class GetterWrapper extends PropertyWrapper {
	
	private final Method getter;

	public GetterWrapper(String name, Class<?> valueType, Method getter, PropertyWrapper parent) {
		super(name, valueType, parent);
		
		this.getter = getter;
	}
	
	public Method getGetter() {
		return getter;
	}
	
	public Object getValue(Object rootObject) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
	{	
		PropertyWrapper rootPropertyWrapper = getParent();
		
		Stack<Method> gettersStack = new Stack<Method>();
		
		while (rootPropertyWrapper != null)
		{
			gettersStack.add(getGetter());
			rootPropertyWrapper = rootPropertyWrapper.getParent();						
		}
		
		Object parentObject = rootObject;
								
		while (!gettersStack.empty())
		{
			if (parentObject != null)
				parentObject = gettersStack.pop().invoke(parentObject);
			else 
				return null;
		}
		
		if (parentObject != null)
		{
			Object value = getGetter().invoke(parentObject);
								
			if (value != null && value.getClass().isEnum())
				value = value.toString();
			
			return value;
		}
		else 
			return null;
	}
}
