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
package com.quantfabric.algo.cep.indicators;

import java.util.List;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;

public abstract class AbstractPluginViewFactory<T extends AbstractPlugInViewParameters> extends ViewFactorySupport
{
	private ViewFactoryContext viewFactoryContext;
	private int streamNumber;
	private List<ExprNode> viewParametersExpressions;
		
	private T viewParameters;
	private EventType eventType;
	private final String viewName;
	private final Class<?> viewResultValueType;
			
	protected AbstractPluginViewFactory(String viewName,
			Class<?> viewResultValueType)
	{
		super();
		this.viewName = viewName;
		this.viewResultValueType = viewResultValueType;
	}

	@Override
	public void setViewParameters(ViewFactoryContext viewFactoryContext,
			List<ExprNode> viewParameters) throws ViewParameterException
	{
		this.viewFactoryContext = viewFactoryContext;	
		this.viewParametersExpressions = viewParameters;
		this.streamNumber = viewFactoryContext.getStreamNum();		
	}

	@Override
	public void attach(EventType parentEventType,
			StatementContext statementContext,
			ViewFactory optionalParentFactory,
			List<ViewFactory> parentViewFactories)
			throws ViewParameterException
	{
		ExprNode[] validated = ViewFactorySupport.validate("Quantfabric OHLC view", parentEventType, statementContext, 
				this.viewParametersExpressions, true);		
		
		this.viewParameters =
				makeViewParameters(validated, parentEventType); 	
	
		this.eventType = AbstractPlugInView.createEventType(viewFactoryContext.getStatementContext(), getViewResultValueType(),	
				viewParameters.getAdditionalProps(), streamNumber);
	}
	
	@Override
	public EventType getEventType()
	{
		return this.eventType;
	}

	protected abstract T makeViewParameters(
			ExprNode[] viewParametersExpressions, EventType parentEventType) throws ViewParameterException;
	
	
	public T getViewParameters()
	{
		return viewParameters;
	}

	public Class<?> getViewResultValueType()
	{
		return viewResultValueType;
	}	
	
	public String getViewName()
	{
		return viewName;
	}
}
