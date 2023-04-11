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
package com.quantfabric.algo.cep.indicators.cycleidentifier;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class CycleIdentifierPluginView extends AbstractPlugInView<CycleIdentifierPluginViewParameters>{
	
	private final CycleIdentifierCalculator ciCalculator;
	private static final Logger logger = Logger.getLogger(CycleIdentifierPluginView.class);
	private final EventBean[] eventsPerStream = new EventBean[1];
	private EventBean lastEvent;
	
	public CycleIdentifierPluginView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext, 
			CycleIdentifierPluginViewParameters viewParameters, 
			EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
		int averageBarPeriod = (Integer) viewParameters.getAverageBarPeriod().getExprEvaluator().evaluate(null, false, agentInstanceViewFactoryContext);
		int majorCycleMultiplier = (Integer) viewParameters.getMajorCycleMultiplier().getExprEvaluator().evaluate(null, false, agentInstanceViewFactoryContext);
		int averageBarMultiplier = (Integer) viewParameters.getAverageBarMultiplier().getExprEvaluator().evaluate(null, false, agentInstanceViewFactoryContext);
		int percentileBarInclude = (Integer) viewParameters.getPercentileBarInclude().getExprEvaluator().evaluate(null, false, agentInstanceViewFactoryContext);
		ciCalculator = new CycleIdentifierCalculator(averageBarPeriod, averageBarMultiplier, majorCycleMultiplier, percentileBarInclude);
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
			
			int closePrice = (Integer) getViewParameters().getClosePrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			int highPrice = (Integer) getViewParameters().getHighPrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			int lowPrice = (Integer) getViewParameters().getLowPrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			boolean isBarClosed = (Boolean) getViewParameters().getIsBarClosed().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			int majorCycleMultiplier = (Integer) getViewParameters().getMajorCycleMultiplier().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			int averageBarMultiplier = (Integer) getViewParameters().getAverageBarMultiplier().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			int percentileBarInclude = (Integer) getViewParameters().getPercentileBarInclude().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			ciCalculator.setMajorCycleMultiplier(majorCycleMultiplier);
			ciCalculator.setAverageBarMultiplier(averageBarMultiplier);
			ciCalculator.setPercentileBarInclude(percentileBarInclude);
			ciCalculator.calculate(barId, closePrice, highPrice, lowPrice, isBarClosed);
			
			updateAdditionalPropsValue(eventsPerStream);
			
			postData();
			
			ciCalculator.update();
			
		}
	}
	 private void postData() 
	 {
		 EventBean resultBean;
		 resultBean = createResultBean(ciCalculator.getCIValue());
		 postData(this, resultBean, lastEvent);
		 lastEvent = resultBean;
	 }
	@Override
	protected Object[] getCurrentViewResults()
	{
		return new Object[] {ciCalculator.getCIValue()};
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
		return new CycleIdentifierPluginView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}
}
