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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;


public class ArrayWrapper extends PropertyWrapper {
	
	public ArrayWrapper(String name, Class<?> valueType, PropertyWrapper parent) {
		super(name, valueType, parent);
	}	
	
	public Object getValue(Object rootObject) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
	{	
		if (rootObject == null)
			return null;
		
		StringBuilder builder = new StringBuilder("[");
		int length = Array.getLength(rootObject);
		
		for (int i = 0; i < length; i++) {
			builder.append(Array.get(rootObject, i).toString());
			if (i < length-1)
				builder.append(", ");
		}

		builder.append("]");
		
		return builder.toString();
	}
}
