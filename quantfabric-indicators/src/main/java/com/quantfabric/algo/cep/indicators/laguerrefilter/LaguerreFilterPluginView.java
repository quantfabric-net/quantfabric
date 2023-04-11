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
package com.quantfabric.algo.cep.indicators.laguerrefilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class LaguerreFilterPluginView extends AbstractPlugInView<LaguerreFilterPluginViewParameters>{
	
	private static final Logger logger = LoggerFactory.getLogger(LaguerreFilterPluginView.class);
	private final EventBean[] eventsPerStream = new EventBean[1];
	private EventBean lastEvent;
	private final LaguerreFilterCalculator lfCalculator = new LaguerreFilterCalculator();
	
	public LaguerreFilterPluginView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext, 
			LaguerreFilterPluginViewParameters viewParameters, 
			EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
	}
	
	public static Logger getLogger()
	{
		return logger;
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData)
	{
		if (oldData != null)
			logger.warn("Possible need to implement processing oldData");
		
		if (newData == null)
			return;
		for (EventBean event : newData)
		{
			eventsPerStream[0] = event;
			
			long barId = (Long) getViewParameters().getBarId().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			double gamma = (Double) getViewParameters().getGamma().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			int closePrice = (Integer) getViewParameters().getClosePrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			boolean isClosedBar = (Boolean) getViewParameters().getIsBarClosed().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext()); 
			
			lfCalculator.setGamma(gamma);
			lfCalculator.calculate(barId, closePrice, isClosedBar);
			
			updateAdditionalPropsValue(eventsPerStream);
			
			postData(createResultBean(lfCalculator.getCurrentValue()));			
		}
	}
	private void postData(EventBean resultBean)	
	{
		postData(this, resultBean, lastEvent);
		lastEvent = resultBean;
	}
	@Override
	protected Object[] getCurrentViewResults()
	{
		return new Object[] {lfCalculator.getClass()};
	}
	@Override
	public void stop()
	{
		logger.info("StopCallback triggered");
		lastEvent = null;
	}
	@Override
	public View cloneView()
	{
		return new LaguerreFilterPluginView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}
}
