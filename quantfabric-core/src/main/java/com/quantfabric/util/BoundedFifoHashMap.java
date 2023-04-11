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

import java.util.LinkedHashMap;

public class BoundedFifoHashMap<K, V> extends LinkedHashMap<K, V>
{
	private static final long serialVersionUID = -8045959058221032224L;

	private int maxSize;
	
	public BoundedFifoHashMap(int maxSize)
	{
		super(maxSize);
		this.maxSize = maxSize;
	}
	
	public int getMaxSize()
	{
		return maxSize;
	}

	public void setMaxSize(int maxSize)
	{
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> paramEntry)
	{
		return (size() > maxSize);
	}
}
