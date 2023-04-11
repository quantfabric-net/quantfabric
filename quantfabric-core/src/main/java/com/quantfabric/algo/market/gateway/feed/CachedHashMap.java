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
package com.quantfabric.algo.market.gateway.feed;

import java.util.HashMap;
import java.util.Map;

public abstract class CachedHashMap<K, V, CK> extends HashMap<K, V>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5418058809179961940L;
	
	private final HashMap<CK, V> valuesByCachedKey= new HashMap<CK, V>();
	
	protected abstract CK getCachedKey(V value);
	
	protected V getByCashKey(CK key)
	{				
		return valuesByCachedKey.get(key);
	}

	private void addValueByCachedKey(V value)
	{
		valuesByCachedKey.put(getCachedKey(value),value);
	}
	
	@Override
	public V put(K key, V value)
	{
		addValueByCachedKey(value);
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (Map.Entry<? extends K, ? extends V> feedByFeedName : m.entrySet())
			addValueByCachedKey(feedByFeedName.getValue());
		super.putAll(m);
	}

	@Override
	public V remove(Object key)
	{
		V value = super.remove(key);
		if (value != null)
			valuesByCachedKey.remove(getCachedKey(value));	
		return value;
	}

	@Override
	public void clear()
	{
		valuesByCachedKey.clear();
		super.clear();
	}
}
