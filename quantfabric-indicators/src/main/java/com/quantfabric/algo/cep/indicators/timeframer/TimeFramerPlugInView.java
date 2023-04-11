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
package com.quantfabric.algo.cep.indicators.timeframer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class TimeFramerPlugInView extends AbstractPlugInView<TimeFramerPlugInViewParameters>{
	private static final Logger logger = LoggerFactory.getLogger(TimeFramerPlugInView.class);
	
	public static Logger getLogger()
	{
		return logger;
	}
	private TimeFramerValue currentTimeFramerValue;
	private final EventBean[] eventsPerStream = new EventBean[1];
	private EventBean lastEvent = null;	
	
	public TimeFramerPlugInView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext, 
			TimeFramerPlugInViewParameters viewParameters, 
			EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData)
	{
		if (newData == null)
			return;
		int value = 0;
		for (EventBean event : newData)
		{
			eventsPerStream[0] = event;
			Integer timeFrame = (Integer) getViewParameters().getTimeFrame().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			Integer lastBid = (Integer) getViewParameters().getLastBid().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			Integer candleOpenPrice = (Integer) getViewParameters().getCandleOpenPrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			Boolean isBarClosed = (Boolean) getViewParameters().getIsBarClosed().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			if(!isBarClosed)
			{
				if(candleOpenPrice < lastBid)
					value = 1;
				else if(candleOpenPrice > lastBid)
					value = -1;
				TimeFramerValue tfValue = new TimeFramerValue(timeFrame, value);
				currentTimeFramerValue = tfValue;
			}
				updateAdditionalPropsValue(eventsPerStream);
				
				if(null == currentTimeFramerValue)
					currentTimeFramerValue = new TimeFramerValue(timeFrame , 0);
				EventBean resultBean = createResultBean(currentTimeFramerValue);			
				
				postData(resultBean);
		}
	}

	public void postData(EventBean resultBean)
	{
		postData(this, resultBean, lastEvent);
		lastEvent = resultBean;
	}
	
	public static void postData(ViewSupport viewSupport, EventBean newEvent, EventBean lastEvent)	
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
	
	public static void postData(ViewSupport viewSupport, EventBean[] newEvents, EventBean[] oldEvents)	
	{
		viewSupport.updateChildren(newEvents, oldEvents);
	}
	
	@Override
	protected Object[] getCurrentViewResults()
	{
		return new Object[]{currentTimeFramerValue};
	}

	@Override
	public View cloneView()
	{
		return new TimeFramerPlugInView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}
	@Override
	public void stop()
	{
		currentTimeFramerValue = null;
	}
}
