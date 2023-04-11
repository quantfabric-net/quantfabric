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
package com.quantfabric.algo.cep.indicators.cycleidentifier;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class CycleIdentifierPluginViewParameters extends AbstractPlugInViewParameters
{
	private final ExprNode averageBarPeriod;
	private final ExprNode averageBarMultiplier;
	private final ExprNode majorCycleMultiplier;
	private final ExprNode percentileBarInclude;
	private final ExprNode barId;
	private final ExprNode closePrice;
	private final ExprNode highPrice;
	private final ExprNode lowPrice;
	private final ExprNode isBarClosed;
	
	public CycleIdentifierPluginViewParameters(ExprNode averageBarPeriod, ExprNode averageBarMultiplier, ExprNode majorCycleMultiplier, 
			ExprNode percentileBarInclude, ExprNode barId, ExprNode closePrice, ExprNode highPrice,
			ExprNode isBarClosed, ExprNode lowPrice)
	{
		this(averageBarPeriod, averageBarMultiplier, majorCycleMultiplier, percentileBarInclude, barId, closePrice, highPrice, lowPrice, isBarClosed, null);
	}
	
	public CycleIdentifierPluginViewParameters(ExprNode averageBarPeriod, ExprNode averageBarMultiplier, ExprNode majorCycleMultiplier,
			ExprNode percentileBarInclude, ExprNode barId, ExprNode closePrice,ExprNode highPrice,ExprNode lowPrice, 
			ExprNode isBarClosed, ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.averageBarPeriod = averageBarPeriod;
		this.averageBarMultiplier = averageBarMultiplier;
		this.majorCycleMultiplier = majorCycleMultiplier;
		this.percentileBarInclude = percentileBarInclude;
		this.barId = barId;
		this.closePrice = closePrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.isBarClosed = isBarClosed;
	}
	public ExprNode getAverageBarPeriod() 
	{
		return averageBarPeriod;
	}
	public ExprNode getMajorCycleMultiplier() 
	{
		return majorCycleMultiplier;
	}
	public ExprNode getBarId()
	{
		return barId;
	}
	public ExprNode getClosePrice()
	{
		return closePrice;
	}
	public ExprNode getHighPrice()
	{
		return highPrice;
	}
	public ExprNode getLowPrice()
	{
		return lowPrice;
	}
	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}
	public ExprNode getAverageBarMultiplier() 
	{
		return averageBarMultiplier;
	}
	public ExprNode getPercentileBarInclude() 
	{
		return percentileBarInclude;
	}
	public static CycleIdentifierPluginViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{	
		if (viewParameters.length < 9)
			throw new ViewParameterException("View requires a 9 parameters: averageBarPeriod, averageBarMultiplier, majorCycleMultiplier, " +
					"percentileBarInclude, barId, closePrice, higiPrice, lowPrice, barClosed");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[0].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires int-typed averageBarPeriod values in parameter 1");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires int-typed averageBarMultiplier values in parameter 2");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires int-typed majorCycleMultiplier values in parameter 3");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires int-typed percentileBarInclude values in parameter 4");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[4].getExprEvaluator().getType()) != Long.TYPE)
			throw new ViewParameterException("View requires long-typed bar id values in parameter 5");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[5].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed closePrice values in parameter 6");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[6].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed highPrice values in parameter 7");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[7].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed lowPrice values in parameter 8");
		
		if (!JavaClassHelper.isBoolean(viewParameters[8].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires boolean-typed bar is closed values in parameter 9.");
				
		return new CycleIdentifierPluginViewParameters(viewParameters[0], viewParameters[1], viewParameters[2], viewParameters[3],
				viewParameters[4], viewParameters[5], viewParameters[6], viewParameters[7], viewParameters[8], ViewAdditionalProps.make(viewParameters, 9, parentEventType));
	}
}
