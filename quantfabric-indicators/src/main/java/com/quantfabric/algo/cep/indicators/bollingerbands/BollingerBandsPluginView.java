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
package com.quantfabric.algo.cep.indicators.bollingerbands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;

public class BollingerBandsPluginView extends
		AbstractPlugInView<BollingerBandsPluginViewParameters>
{
	private static final Logger logger = LoggerFactory.getLogger(BollingerBandsPluginView.class);

	private final BollingerBandsCalculator calculator;

	private final EventBean[] eventsPerStream = new EventBean[1];
	private EventBean lastEvent = null;

	public BollingerBandsPluginView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext,
			BollingerBandsPluginViewParameters viewParameters,
			EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);

		int period = (Integer) viewParameters.getPeriod().getExprEvaluator()
				.evaluate(null, false, agentInstanceViewFactoryContext);
		int multiple = (Integer) viewParameters.getMultiple()
				.getExprEvaluator()
				.evaluate(null, false, agentInstanceViewFactoryContext);

		this.calculator = new BollingerBandsCalculator(period, multiple);
	}

	@Override
	public View cloneView()
	{
		return new BollingerBandsPluginView(
				getAgentInstanceViewFactoryContext(), getViewParameters(),
				getEventType());
	}

	@Override
	public void stop()
	{
		calculator.restCurrentValue();
		lastEvent = null;
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData)
	{
		if (oldData != null)
			logger.warn("Possible need to implement processing oldData");

		if (newData == null) return;

		for (EventBean event : newData)
		{
			eventsPerStream[0] = event;

			// grab parameters value
			double value = (Double) getViewParameters()
					.getValue()
					.getExprEvaluator()
					.evaluate(eventsPerStream, true,
							getAgentInstanceViewFactoryContext());

			// update calculator
			calculator.addValue(value);

			updateAdditionalPropsValue(eventsPerStream);

			postData();
		}
	}

	private void postData()
	{
		EventBean resultBean = createResultBean(calculator.getCurrentValue());
		postData(this, resultBean, lastEvent);
		lastEvent = resultBean;
	}

	@Override
	protected Object[] getCurrentViewResults()
	{
		return new Object[] { calculator.getCurrentValue() };
	}
}
