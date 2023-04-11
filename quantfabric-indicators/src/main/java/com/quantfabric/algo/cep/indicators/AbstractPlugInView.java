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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.ArrayEventIterator;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.util.StopCallback;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.ViewSupport;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public abstract class AbstractPlugInView<T extends AbstractPlugInViewParameters> extends ViewSupport implements CloneableView, StopCallback
{
	private final AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext;
	private final T viewParameters;
	private final EventType eventType;
	private Object[] lastAdditioanlPropsValue;
	
	protected AbstractPlugInView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext,
			T viewParameters, 
			EventType eventType)
	{
		this.agentInstanceViewFactoryContext = agentInstanceViewFactoryContext;
		this.viewParameters = viewParameters;
		this.eventType = eventType;	
		
		this.agentInstanceViewFactoryContext.getAgentInstanceContext().getTerminationCallbacks().add(this);
	}
	
	@Override
	public Iterator<EventBean> iterator()
	{
		return new ArrayEventIterator(createResultBeans());
	}

	public AgentInstanceViewFactoryChainContext getAgentInstanceViewFactoryContext()
	{
		return agentInstanceViewFactoryContext;
	}

	public EventType getEventType()
	{
		return eventType;
	}
	
	public T getViewParameters()
	{
		return viewParameters;
	}
	
	protected void updateAdditionalPropsValue(EventBean[] events)
	{ 
		if (events == null)
			return;
		
		if (getViewParameters().getAdditionalProps() != null && events.length != 0)
		{
			if (this.lastAdditioanlPropsValue == null)
			{
				this.lastAdditioanlPropsValue = new Object[viewParameters.getAdditionalProps().getAdditionalExpr().length];
			}
			for (int val = 0; val < viewParameters.getAdditionalProps().getAdditionalExpr().length; ++val)
			{
				this.lastAdditioanlPropsValue[val] = viewParameters.getAdditionalProps().getAdditionalExpr()[val]
						.evaluate(events, true, this.agentInstanceViewFactoryContext);
			}
		}
	}
	
	protected Object[] getLastAdditioanlPropsValue()
	{
		return lastAdditioanlPropsValue;
	}
	
	protected abstract Object[] getCurrentViewResults();
	
	protected EventBean[] createResultBeans()
	{		
		return createResultBeans(
					agentInstanceViewFactoryContext.getStatementContext().getEventAdapterService(),
					getEventType(),
					getCurrentViewResults(),
					viewParameters.getAdditionalProps(),
					getLastAdditioanlPropsValue());
	}
	
	protected EventBean createResultBean(Object viewResult)
	{		
		return createResultBean(
					agentInstanceViewFactoryContext.getStatementContext().getEventAdapterService(),
					getEventType(),
					viewResult,
					viewParameters.getAdditionalProps(),
					getLastAdditioanlPropsValue());
	}
	
	public static EventType createEventType(StatementContext statementContext,  Class<?> viewResultValueType)
	{
		return statementContext.getEventAdapterService().addBeanType(
				viewResultValueType.getName(), viewResultValueType, false, false, false);
	}
	
	public static EventType createEventType(
			StatementContext statementContext, Class<?> viewResultValueType,
			ViewAdditionalProps additionalProps, int streamNumber)
	{
		EventType eventType = createEventType(statementContext, viewResultValueType);
		
		if (additionalProps == null)
			return eventType;
				
		String eventTypeName = statementContext.getStatementId() + "_" + eventType.getName() + "_" + streamNumber;
		
		Map<String, Object> additionalPropsMap = new HashMap<String, Object>();
		ViewAdditionalProps.addCheckDupProperties(additionalPropsMap, additionalProps, eventType.getPropertyNames());
		
		return statementContext.getEventAdapterService().addWrapperType(eventTypeName, eventType, additionalPropsMap, true, true);
	}
		
	public static EventBean createResultBean(EventAdapterService eventAdapterService, EventType resultBeanType, Object viewResult, 
			ViewAdditionalProps additionalProps, Object[] lastAdditioanlPropsValue)
	{				
		EventBean viewResultBean = eventAdapterService.adapterForBean(viewResult);
		
		if (additionalProps != null)
		{
			Map<String, Object> additioanlPropsResult = new HashMap<String, Object>();
			additionalProps.addProperties(additioanlPropsResult, lastAdditioanlPropsValue);
			return eventAdapterService.adapterForTypedWrapper(viewResultBean, additioanlPropsResult, resultBeanType);
		}
		else
			return viewResultBean;	
	}
	
	public static EventBean[] createResultBeans(EventAdapterService eventAdapterService, EventType resultBeanType, Object[] viewResult, 
			ViewAdditionalProps additionalProps, Object[] lastAdditioanlPropsValue)
	{				
		EventBean[] viewResultBeans = new EventBean[viewResult.length];
		
		for (int i = 0; i < viewResult.length; i++)
		{
			viewResultBeans[i] = createResultBean(eventAdapterService, resultBeanType, viewResult[i],
					additionalProps, lastAdditioanlPropsValue);	
		}
		
		return viewResultBeans;
	}
		
	protected static void postData(ViewSupport viewSupport, EventBean newEvent, EventBean lastEvent)	
	{
		if (lastEvent == null)
		{
			viewSupport.updateChildren(new EventBean[] { newEvent }, null);
		}
		else
		{
			viewSupport.updateChildren(new EventBean[] { newEvent },
					new EventBean[] { lastEvent });
		}
	}
	
	protected static void postData(ViewSupport viewSupport, EventBean[] newEvents, EventBean[] oldEvents)	
	{
		viewSupport.updateChildren(newEvents, oldEvents);
	}
}
