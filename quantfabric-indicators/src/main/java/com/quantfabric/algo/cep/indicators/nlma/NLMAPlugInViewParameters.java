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
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class NLMAPlugInViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode barId;
	private final ExprNode openSourceTimestamp;
	private final ExprNode price;
	private final ExprNode isBarClosed;
	private final ExprNode period;
	private final ExprNode deviation;
	private final ExprNode pctFilter;
	
	public NLMAPlugInViewParameters(ExprNode barId, ExprNode openSourceTimestamp, ExprNode price,
			ExprNode isBarClosed, ExprNode period, ExprNode deviation, ExprNode pctFilter)
	{
		this(barId, openSourceTimestamp, price, isBarClosed, period, deviation, pctFilter, null);
	}
	
	public NLMAPlugInViewParameters(ExprNode barId, ExprNode openSourceTimestamp, ExprNode price,
			ExprNode isBarClosed, ExprNode period, ExprNode deviation, ExprNode pctFilter,
			ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.barId = barId;
		this.openSourceTimestamp = openSourceTimestamp;
		this.price = price;
		this.isBarClosed = isBarClosed;
		this.period = period;
		this.deviation = deviation;
		this.pctFilter = pctFilter;
	}
	
	public ExprNode getBarId()
	{
		return barId;
	}

	public ExprNode getOpenSourceTimestamp()
	{
		return openSourceTimestamp;
	}

	public ExprNode getPrice()
	{
		return price;
	}

	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}

	public ExprNode getPeriod()
	{
		return period;
	}

	public ExprNode getDeviation()
	{
		return deviation;
	}
	
	public ExprNode getPctFilter()
	{
		return pctFilter;
	}

	public static NLMAPlugInViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{	
		if (viewParameters.length < 7)		
			throw new ViewParameterException("View requires a 7 parameters: barId, openSourceTimestamp, price, isBarClosed, period, deviation, pctFilter");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[0].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed bar id values in parameter 0");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed open bar timestamp values in parameter 1");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed price values in parameter 2");
		
		if (!JavaClassHelper.isBoolean(viewParameters[3].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 3.");	
				
		if (!JavaClassHelper.isNumericNonFP(viewParameters[4].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed period value in parameter 4.");	
				
		if (!JavaClassHelper.isFloatingPointClass(viewParameters[5].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires double-typed deviation values in parameter 5.");	
		
		if (!JavaClassHelper.isFloatingPointClass(viewParameters[6].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires double-typed dynamic filter values in parameter 6.");	
		
		return new NLMAPlugInViewParameters(viewParameters[0], viewParameters[1], viewParameters[2], viewParameters[3],
				viewParameters[4], viewParameters[5], viewParameters[6],
				ViewAdditionalProps.make(viewParameters, 7, parentEventType));
	}
}
