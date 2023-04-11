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

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class LaguerreFilterPluginViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode barId;
	private final ExprNode gamma;
	private final ExprNode closePrice;
	private final ExprNode isBarClosed;
	
	public LaguerreFilterPluginViewParameters(ExprNode barId, ExprNode closePrice, ExprNode isBarClosed,
			ExprNode gamma, ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.barId = barId;
		this.gamma = gamma;
		this.closePrice = closePrice;
		this.isBarClosed = isBarClosed;
	}
	public LaguerreFilterPluginViewParameters(ExprNode barId, ExprNode closePrice, ExprNode isBarClosed,	ExprNode gamma)
	{
		this(barId, closePrice, isBarClosed, gamma, null);
	}
	
	public ExprNode getBarId() {
		return barId;
	}
	public ExprNode getGamma() {
		return gamma;
	}
	public ExprNode getClosePrice()
	{
		return closePrice;
	}	
	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}
	
	public static LaguerreFilterPluginViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{	
		if (viewParameters.length < 3)		
			throw new ViewParameterException("View requires a 3 parameters: barId, gamma, closePrice");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[0].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed bar id values in parameter 0");
				
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed closePrice values in parameter 1");
		
		if (!JavaClassHelper.isBoolean(viewParameters[2].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 2.");	
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[3].getExprEvaluator().getType()) != Double.TYPE)
			throw new ViewParameterException("View requires double-typed gamma values in parameter 3");
		
				return new LaguerreFilterPluginViewParameters(viewParameters[0], viewParameters[1], viewParameters[2],
						viewParameters[3],
				ViewAdditionalProps.make(viewParameters, 4, parentEventType));
	}
}
