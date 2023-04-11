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
package com.quantfabric.algo.market.dataprovider;

public class FeedNameImpl implements FeedName
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8716962216948754318L;
	
	private String name;
	
	public FeedNameImpl()
	{
		this(null);
	}
	
	public FeedNameImpl(String name)
	{
		setName(name);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FeedName)
			return this.name.equals(((FeedName)obj).getName());
		
		return false;
	}

	@Override
	public int hashCode()
	{
		return name.hashCode();
	}
	@Override
	public String toString() {

		return name;
	}
	@Override
	public long getId()
	{
		return hashCode();
	}
}
