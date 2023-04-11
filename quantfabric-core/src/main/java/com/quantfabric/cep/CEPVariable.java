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
package com.quantfabric.cep;

import com.quantfabric.util.VariableWrapper;

public class CEPVariable<T> implements VariableWrapper<T>
{
	private final ICEPProvider cepProvider;
	private final String name;
		
	public CEPVariable(ICEPProvider cepProvider, String name, String type, T defaultValue)
	{
		super();
		this.cepProvider = cepProvider;
		this.name = name;
		
		cepProvider.addVariable(name, type, defaultValue, false);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getValue()
	{
		return (T)cepProvider.getVariableValue(getName());
	}

	@Override
	public void setValue(T value)
	{
		cepProvider.setVariableValue(getName(), value);		
	}

}
