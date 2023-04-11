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
package com.quantfabric.algo.cep.indicators.timeframer;

import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.JavaClassHelper;
import com.espertech.esper.view.ViewParameterException;
import com.quantfabric.algo.cep.indicators.AbstractPlugInViewParameters;
import com.quantfabric.algo.cep.indicators.util.ViewAdditionalProps;

public class TimeFramerPlugInViewParameters extends AbstractPlugInViewParameters{
	private final ExprNode timeFrame;
	private final ExprNode lastBid;
	private final ExprNode candleOpenPrice;
	private final ExprNode isBarClosed;
	
	public TimeFramerPlugInViewParameters(ExprNode timeFrame, ExprNode lastBid, ExprNode candleOpenPrice, ExprNode isBarClosed)
	{
		this(timeFrame, lastBid, candleOpenPrice, isBarClosed, null);
	}
	
	public TimeFramerPlugInViewParameters(ExprNode timeFrame, ExprNode lastBid, ExprNode candleOpenPrice, ExprNode isBarClosed, 
			ViewAdditionalProps additionalProps)
	{
		super(additionalProps);
		this.timeFrame = timeFrame;
		this.lastBid = lastBid;
		this.candleOpenPrice = candleOpenPrice;
		this.isBarClosed = isBarClosed;
	}
	
	public ExprNode getTimeFrame()
	{
		return timeFrame;
	}

	public ExprNode getLastBid()
	{
		return lastBid;
	}
	public ExprNode getCandleOpenPrice()
	{
		return candleOpenPrice;
	}
	public ExprNode getIsBarClosed()
	{
		return isBarClosed;
	}

	public static TimeFramerPlugInViewParameters make(ExprNode[] viewParameters, EventType parentEventType) throws ViewParameterException	
	{
		 if (viewParameters.length < 4)		
			throw new ViewParameterException("View requires a four parameters: time frame, last known market Bid, candle open price and isBarClosed");
		
		if (!JavaClassHelper.isNumericNonFP(viewParameters[0].getExprEvaluator().getType()))
			throw new ViewParameterException("View requires integer-typed time frame value in parameter 1");	
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[1].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed last bid value in parameter 2");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[2].getExprEvaluator().getType()) != Integer.TYPE)
			throw new ViewParameterException("View requires integer-typed candle open price in parameter 3");
		
		if (JavaClassHelper.getPrimitiveType(viewParameters[3].getExprEvaluator().getType()) != Boolean.TYPE)
			throw new ViewParameterException("View requires boolean-typed bar is closed in parameter 4");
	       
		return new TimeFramerPlugInViewParameters(viewParameters[0], viewParameters[1], viewParameters[2], viewParameters[3], 
				ViewAdditionalProps.make(viewParameters, 4, parentEventType));
	}
}
