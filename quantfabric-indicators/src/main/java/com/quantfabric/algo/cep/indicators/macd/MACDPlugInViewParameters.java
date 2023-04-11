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
package com.quantfabric.algo.cep.indicators.macd;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class MACDPlugInViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode price;
	private final ExprNode isBarClosed;
	private final ExprNode fastEMAPeriod;
	private final ExprNode slowEMAPeriod;
	private final ExprNode signalEMAPeriod;
	
	public MACDPlugInViewParameters(ExprNode price,	ExprNode isBarClosed, 
			ExprNode fastEMAPeriod, ExprNode slowEMAPeriod, ExprNode signalEMAPeriod)
	{
		this(price, isBarClosed, fastEMAPeriod, slowEMAPeriod, signalEMAPeriod, null);
	}
	
	public MACDPlugInViewParameters(ExprNode price,	ExprNode isBarClosed, 
			ExprNode fastEMAPeriod, ExprNode slowEMAPeriod, ExprNode signalEMAPeriod, ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.price = price;
		this.isBarClosed = isBarClosed;
		this.fastEMAPeriod = fastEMAPeriod;
		this.slowEMAPeriod = slowEMAPeriod;
		this.signalEMAPeriod = signalEMAPeriod; 
	}

	public ExprNode getPrice()
	{
		return price;
	}

	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}

	public ExprNode getFastEMAPeriod()
	{
		return fastEMAPeriod;
	}

	public ExprNode getSlowEMAPeriod()
	{
		return slowEMAPeriod;
	}

	public ExprNode getSignalEMAPeriod()
	{
		return signalEMAPeriod;
	}

	public static MACDPlugInViewParameters make(
			ExprNode[] viewParametersExpressions, EventType parentEventType) throws ViewParameterException
	{
		if (viewParametersExpressions.length < 5)		
			throw new ViewParameterException("View requires a 5 parameters: price, isBarClosed, fastEMAPeriod, slowEMAPeriod, signalEMAPeriod");
						
		if (JavaClassHelper.getPrimitiveType(viewParametersExpressions[0].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed price values in parameter 1");
		
		if (!JavaClassHelper.isBoolean(viewParametersExpressions[1].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 2.");	
		
		if (!viewParametersExpressions[2].isConstantResult())
			throw new ViewParameterException("3th parameter (Fast EMA period) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[2].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant Fast EMA period value in parameter 3.");	
		
		if (!viewParametersExpressions[3].isConstantResult())
			throw new ViewParameterException("4th parameter (Slow EMA period) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[3].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant Slow EMA period value in parameter 4.");	
		
		if (!viewParametersExpressions[4].isConstantResult())
			throw new ViewParameterException("5th parameter (Signal EMA period) must be a constant");
		
		if (!JavaClassHelper.isNumericNonFP(viewParametersExpressions[4].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed constant Signal EMA period value in parameter 5.");	
			
		return new MACDPlugInViewParameters(viewParametersExpressions[0], viewParametersExpressions[1], viewParametersExpressions[2],
				viewParametersExpressions[3], viewParametersExpressions[4],
				ViewAdditionalProps.make(viewParametersExpressions, 5, parentEventType));
	}
	
}
