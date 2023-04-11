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

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPluginViewFactory;

public class NLMAPlugInViewFactory extends AbstractPluginViewFactory<NLMAPlugInViewParameters>
{

	public NLMAPlugInViewFactory()
	{
		super("Quantfabric NLMA View", NLMAValue.class);
	}

	@Override
	public View makeView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext)
	{
		return new NLMAPlugInView(agentInstanceViewFactoryContext, getViewParameters(), getEventType());
	}

	@Override
	protected NLMAPlugInViewParameters makeViewParameters(
			ExprNode[] viewParametersExpressions, EventType parentEventType)
			throws ViewParameterException
	{		
		return NLMAPlugInViewParameters.make(viewParametersExpressions, parentEventType);
	}

}
