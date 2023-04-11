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

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class BollingerBandsPluginViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode value;
	private final ExprNode period;
	private final ExprNode multiple;

	public BollingerBandsPluginViewParameters(
			ExprNode value, ExprNode period, ExprNode multiple)
	{
		this(value, period, multiple, null);
	}
	
	public BollingerBandsPluginViewParameters(
			ExprNode value, ExprNode period, ExprNode multiple,
			ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		
		this.value = value;
		this.period = period;
		this.multiple = multiple;
	}

	public ExprNode getValue()
	{
		return value;
	}

	public ExprNode getPeriod()
	{
		return period;
	}

	public ExprNode getMultiple()
	{
		return multiple;
	}

	public static BollingerBandsPluginViewParameters make(
			ExprNode[] viewParametersExpressions, EventType parentEventType) throws ViewParameterException
	{
		if (viewParametersExpressions.length < 3)		
			throw new ViewParameterException("View requires a 3 parameters: value, period, multiple");
		
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[0].getExprEvaluator().getType()) != Double.TYPE)
			throw new ViewParameterException("View requires double-typed values in parameter 0");
				
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed period in parameter 1");
		
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed multiple in parameter 2");
				
		return new BollingerBandsPluginViewParameters(viewParametersExpressions[0], viewParametersExpressions[1], viewParametersExpressions[2],
				ViewAdditionalProps.make(viewParametersExpressions, 3, parentEventType));
	}
}
