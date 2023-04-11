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
package com.quantfabric.algo.cep.indicators.stochrsi;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class StochRSIPlugInViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode barId;
	private final ExprNode price;
	private final ExprNode isBarClosed;
	private final ExprNode rPeriod;
	private final ExprNode kPeriod;
	private final ExprNode dPeriod;
	private final ExprNode slowing;
	
	public StochRSIPlugInViewParameters(ExprNode barId, ExprNode price,
			ExprNode isBarClosed, ExprNode rPeriod, ExprNode kPeriod, ExprNode dPeriod, ExprNode slowing)
	{
		this(barId, price, isBarClosed, rPeriod, kPeriod, dPeriod, slowing, null);
	}	
	
	public StochRSIPlugInViewParameters(ExprNode barId, ExprNode price,
			ExprNode isBarClosed, ExprNode rPeriod, ExprNode kPeriod, ExprNode dPeriod, ExprNode slowing, 
			ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		
		this.barId = barId;
		this.price = price;
		this.isBarClosed = isBarClosed;
		this.rPeriod = rPeriod;
		this.kPeriod = kPeriod;
		this.dPeriod = dPeriod;
		this.slowing = slowing;
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

	public ExprNode getrPeriod()
	{
		return rPeriod;
	}

	public ExprNode getkPeriod()
	{
		return kPeriod;
	}

	public ExprNode getdPeriod()
	{
		return dPeriod;
	}

	public ExprNode getSlowing()
	{
		return slowing;
	}

	public static StochRSIPlugInViewParameters make(
			ExprNode[] viewParametersExpressions, EventType parentEventType) throws ViewParameterException
	{
		if (viewParametersExpressions.length < 8)		
			throw new ViewParameterException("View requires a 7 parameters: barId, price, isBarClosed, rPeriod, kPeriod, dPerdiod, slowing");
		
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[0].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed bar id values in parameter 0");
				
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed price values in parameter 1");
		
		if (!JavaClassHelper.isBoolean(viewParametersExpressions[2].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 2.");	
		
		if (!viewParametersExpressions[3].isConstantResult())
			throw new ViewParameterException("3th parameter (R preriod) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[3].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant R period value in parameter 3.");	
		
		if (!viewParametersExpressions[4].isConstantResult())
			throw new ViewParameterException("4th parameter (K preriod) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[4].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant K period value in parameter 4.");	
		
		if (!viewParametersExpressions[5].isConstantResult())
			throw new ViewParameterException("5th parameter (D preriod) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[5].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant D period value in parameter 5.");	
		
		if (!viewParametersExpressions[6].isConstantResult())
			throw new ViewParameterException("6th parameter (Slowing) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[6].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant slowing value in parameter 6.");	

		
		return new StochRSIPlugInViewParameters(viewParametersExpressions[0], viewParametersExpressions[1], viewParametersExpressions[2],
				viewParametersExpressions[3], viewParametersExpressions[4], viewParametersExpressions[5], viewParametersExpressions[6],
				ViewAdditionalProps.make(viewParametersExpressions, 7, parentEventType));
	}
}
