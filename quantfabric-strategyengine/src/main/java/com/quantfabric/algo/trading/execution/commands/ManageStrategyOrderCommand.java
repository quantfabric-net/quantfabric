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
package com.quantfabric.algo.trading.execution.commands;

import com.quantfabric.algo.commands.Command;

public abstract class ManageStrategyOrderCommand implements Command
{
	private String originalOrderReference; 
	
	private boolean indetifiedByComplexAttributes;
	private String complexOrderReference;
	private int complexOrderLegId;
	
	public ManageStrategyOrderCommand()
	{
		this(null);
	}
	
	public ManageStrategyOrderCommand(String originalOrderReference)
	{
		setOriginalOrderReference(originalOrderReference);
		this.indetifiedByComplexAttributes = false;
	}
	
	public ManageStrategyOrderCommand(String complexOrderReference,	int complexOrderLegId)
	{
		this.indetifiedByComplexAttributes = true;
		this.complexOrderReference = complexOrderReference;
		this.complexOrderLegId = complexOrderLegId;
	}
	
	public String getOriginalOrderReference()
	{
		return originalOrderReference;
	}

	public void setOriginalOrderReference(String originalOrderReference)
	{
		this.originalOrderReference = originalOrderReference;
	}	
	
	public boolean isIndetifiedByComplexAttributes()
	{
		return indetifiedByComplexAttributes;
	}

	public void setIndetifiedByComplexAttributes(
			boolean indetifiedByComplexAttributes)
	{
		this.indetifiedByComplexAttributes = indetifiedByComplexAttributes;
	}

	public String getComplexOrderReference()
	{
		return complexOrderReference;
	}

	public void setComplexOrderReference(String complexOrderReference)
	{
		this.complexOrderReference = complexOrderReference;
	}

	public int getComplexOrderLegId()
	{
		return complexOrderLegId;
	}

	public void setComplexOrderLegId(int complexOrderLegId)
	{
		this.complexOrderLegId = complexOrderLegId;
	}
}
