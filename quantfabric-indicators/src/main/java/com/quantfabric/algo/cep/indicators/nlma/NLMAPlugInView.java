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
package com.quantfabric.algo.cep.indicators.nlma;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class NLMAPlugInView extends AbstractPlugInView<NLMAPlugInViewParameters>
{
	private static final Logger logger = Logger.getLogger(NLMAPlugInView.class);
	
	private final EventBean[] eventsPerStream = new EventBean[1];
	private final NLMACalculator nlmaCalculator;

	private EventBean lastEvent;
	
	public static Logger getLogger()
	{
		return logger;
	}
	
	public NLMAPlugInView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext,
			NLMAPlugInViewParameters viewParameters, EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
			
		int period = (Integer) viewParameters.getPeriod().getExprEvaluator().evaluate(null, false, agentInstanceViewFactoryContext);
		
		this.nlmaCalculator = new NLMACalculator(period);
	}

	@Override
	public View cloneView()
	{
		return new NLMAPlugInView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}

	@Override
	public void stop()
	{
		logger.info("StopCallback triggered");	
		nlmaCalculator.resetCurrentState();
		lastEvent = null;		
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
			
			int price = (Integer) getViewParameters().getPrice().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			long barId = (Long) getViewParameters().getBarId().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			long openSourceTimestamp = (Long) getViewParameters().getOpenSourceTimestamp().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			boolean isClosedBar = (Boolean) getViewParameters().getIsBarClosed().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext()); 
			
			//dynamic parameters
			double deviation = (Double) getViewParameters().getDeviation().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			double pctFilter = (Double) getViewParameters().getPctFilter().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			int period = (Integer) getViewParameters().getPeriod().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			if (period < 2)
				logger.error("NLMA period must be gretter than 2");
			else			
				nlmaCalculator.setPeriod(period);
			
			nlmaCalculator.setDeviation(deviation);
			nlmaCalculator.setPctFilter(pctFilter);			
			
			nlmaCalculator.addPrice(barId, price, openSourceTimestamp, isClosedBar);		
			
			updateAdditionalPropsValue(eventsPerStream);
			
			postData(createResultBean(nlmaCalculator.getCurrentValue()));
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
		return new Object[] {nlmaCalculator.getCurrentValue()};
	}

}
