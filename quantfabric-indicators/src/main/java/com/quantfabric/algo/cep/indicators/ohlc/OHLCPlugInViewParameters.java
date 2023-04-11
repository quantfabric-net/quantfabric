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
package com.quantfabric.algo.cep.indicators.ohlc;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class OHLCPlugInViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode timeFrame;
	private final ExprNode timestamps;
	private final ExprNode prices;
	
	public OHLCPlugInViewParameters(ExprNode timeFrame, ExprNode timestamps,
			ExprNode prices)
	{
		this(timeFrame, timestamps, prices, null);
	}
	
	public OHLCPlugInViewParameters(ExprNode timeFrame, ExprNode timestamps,
			ExprNode prices, ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.timeFrame = timeFrame;
		this.timestamps = timestamps;
		this.prices = prices;
	}
	
	public ExprNode getTimeFrame()
	{
		return timeFrame;
	}

	public ExprNode getTimestamps()
	{
		return timestamps;
	}

	public ExprNode getPrices()
	{
		return prices;
	}

	public static OHLCPlugInViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{
		if (viewParameters.length < 3)		
			throw new ViewParameterException("View requires a three parameters: time frame, the expression returning " + 
											 "timestamps and expression supplying OHLC data points");
		
		if (!viewParameters[0].isConstantResult())
			throw new ViewParameterException("First parameter (Time frame size) must be a constant");
		
		if (viewParameters[0].getExprEvaluator().getType() != String.class)
			throw new ViewParameterException("View requires constant string representing time frame value, in parameter 0. Format: N { second | seconds | sec | s | minute | minutes | min | m | hour | hours | h | day | days | d | week | weeks | w }");	
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed timestamp values in parameter 1");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed price values in parameter 2");
	       
		return new OHLCPlugInViewParameters(viewParameters[0], viewParameters[1], viewParameters[2], 
				ViewAdditionalProps.make(viewParameters, 3, parentEventType));		
	}
}
