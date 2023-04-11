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

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class PrecisionHistogramPlugInViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode barId;
	private final ExprNode price;
	private final ExprNode isBarClosed;
	
	public PrecisionHistogramPlugInViewParameters(ExprNode barId, ExprNode price, ExprNode isBarClosed)
	{
		this(barId, price, isBarClosed, null);
	}
	
	public PrecisionHistogramPlugInViewParameters(ExprNode barId, ExprNode price,
			ExprNode isBarClosed, ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.barId = barId;
		this.price = price;
		this.isBarClosed = isBarClosed;
	}
	
	public ExprNode getBarId()
	{
		return barId;
	}

	public ExprNode getPrice()
	{
		return price;
	}

	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}

	public static PrecisionHistogramPlugInViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{	
		if (viewParameters.length < 3)		
			throw new ViewParameterException("View requires a 3 parameters: barId, price, isBarClosed");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[0].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed bar id values in parameter 0");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed price values in parameter 2");
		
		if (!JavaClassHelper.isBoolean(viewParameters[2].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 3.");	
				
		return new PrecisionHistogramPlugInViewParameters(viewParameters[0], viewParameters[1], viewParameters[2],
				ViewAdditionalProps.make(viewParameters, 3, parentEventType));
	}
}
