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
package com.quantfabric.algo.cep.indicators.precisionhistogram;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class PrecisionHistogramPlugInView extends AbstractPlugInView<PrecisionHistogramPlugInViewParameters>
{
	private static final Logger logger = Logger.getLogger(PrecisionHistogramPlugInView.class);
	
	private final EventBean[] eventsPerStream = new EventBean[1];
	private final PrecisionHistogramCalculator pshCalculator;

	private EventBean lastEvent;
	
	public static Logger getLogger()
	{
		return logger;
	}
	
	public PrecisionHistogramPlugInView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext,
			PrecisionHistogramPlugInViewParameters viewParameters, EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
			
		this.pshCalculator = new PrecisionHistogramCalculator();
	}

	@Override
	public View cloneView()
	{
		return new PrecisionHistogramPlugInView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}

	@Override
	public void stop()
	{
		logger.info("StopCallback triggered");	
		pshCalculator.resetCurrentState();
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
			
			boolean isClosedBar = (Boolean) getViewParameters().getIsBarClosed().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext()); 
			
			pshCalculator.addPrice(barId, price, isClosedBar);		
			
			updateAdditionalPropsValue(eventsPerStream);
			
			postData(createResultBean(pshCalculator.getCurrentValue()));
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
		return new Object[] {pshCalculator.getCurrentValue()};
	}

}
