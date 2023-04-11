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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.EPStatementHandleCallback;
import com.espertech.esper.core.service.ExtensionServicesContext;
import com.espertech.esper.schedule.ScheduleHandleCallback;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esper.view.View;
import com.quantfabric.algo.cep.indicators.AbstractPlugInView;
import com.quantfabric.algo.market.datamodel.OHLCValue;
import com.quantfabric.util.timeframes.Interval;
import com.quantfabric.util.timeframes.Timeframe;
import com.quantfabric.util.timeframes.TimeframeFactory;

public class OHLCPlugInView extends AbstractPlugInView<OHLCPlugInViewParameters>
{
	private static final Logger logger = Logger.getLogger(OHLCPlugInView.class);
	public static Logger getLogger()
	{
		return logger;
	}
	
	private final static int LATE_EVENT_SLACK_MILLS = 30000;
	
	private final EventBean[] eventsPerStream = new EventBean[1];
	
	private OHLCValue currentOhlcValue;
	private final Timeframe timeframe;
	private Interval currentTimeframeInterval;
	
	private EventBean lastEvent = null;	
	
	private final ScheduleSlot scheduleSlot;
	private EPStatementHandleCallback closeByTimeoutCallbackHandle = null;
	
	public OHLCPlugInView(
			AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext, 
			OHLCPlugInViewParameters viewParameters, 
			EventType eventType)
	{
		super(agentInstanceViewFactoryContext, viewParameters, eventType);
					
		this.timeframe = TimeframeFactory.getTimeframe((String)viewParameters.getTimeFrame().getExprEvaluator().evaluate(
				null, false, agentInstanceViewFactoryContext));
		
		this.scheduleSlot = agentInstanceViewFactoryContext.getStatementContext().getScheduleBucket().allocateSlot();
	}

	@SuppressWarnings("unused")
	private void initInternalState(long timestamp)	
	{		
		initInternalState(new Date(timestamp));
	}
	
	private void initInternalState(Date timestamp)	
	{		
		this.currentOhlcValue = new OHLCValue(this.timeframe.getLengthInSeconds());
		this.currentTimeframeInterval = this.timeframe.interval(timestamp);
	}
	
	@Override
	public void update(EventBean[] newData, EventBean[] oldData)
	{
		if (oldData != null)
			logger.warn("Possible need to implement processing oldData");
		
		if (newData == null)
			return;

		resetCallback();
		
		for (EventBean event : newData)
		{
			logger.debug(String.valueOf(newData.length));
			
			eventsPerStream[0] = event;
			Long timestamp = (Long) getViewParameters().getTimestamps().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			Integer price = (Integer) getViewParameters().getPrices().getExprEvaluator().evaluate(
					eventsPerStream, true, getAgentInstanceViewFactoryContext());
			
			if (timestamp <= 0 || price <= 0)
				continue;
			
			Date timestampAsDate = new Date(timestamp);
			
			logger.debug(new SimpleDateFormat("HH:mm:ss.SSS").format(timestampAsDate));
			
			if (currentOhlcValue == null)
				initInternalState(timestampAsDate);
			else
				//check for to close currentOhlcValue, by next time frame started
				if (timestampAsDate.compareTo(this.currentTimeframeInterval.getEnd()) >= 0)
				{
					currentOhlcValue.close(false);
					postData(createResultBean(currentOhlcValue.clone()));
					initInternalState(timestampAsDate);
				}
			
			try
			{				
				currentOhlcValue.update(timestamp, price);
			}
			catch (Exception e)
			{
				logger.error("Update internal state of View failed.", e);
			}			

			updateAdditionalPropsValue(eventsPerStream);
			
			EventBean resultBean = createResultBean(currentOhlcValue.clone());			
			postData(resultBean);
						
		}
		
		//schedule callback for to close currentOhlcValue by timeout
		scheduleCallback();
	}

	private void scheduleCallback()
	{
		if (closeByTimeoutCallbackHandle != null)
			resetCallback();
		
		logger.debug("End of frame - " + new SimpleDateFormat("HH:mm:ss").format(this.currentTimeframeInterval.getEnd()));
		
		long scheduleAfterMSec = (this.currentTimeframeInterval.getEnd().getTime() - this.currentOhlcValue.getCloseSourceTimestamp())
				+ LATE_EVENT_SLACK_MILLS; // leave some time for late comers
				
		ScheduleHandleCallback callback = 
			new ScheduleHandleCallback() 
		 	{
		        public void scheduledTrigger(ExtensionServicesContext extensionServicesContext)
		        {
					currentOhlcValue.close(true);
					postData(createResultBean(currentOhlcValue.clone()));
					currentOhlcValue = null;
		        }
		    };
		
		closeByTimeoutCallbackHandle = new EPStatementHandleCallback(
				getAgentInstanceViewFactoryContext().getEpStatementAgentInstanceHandle(), callback);
	        
		getAgentInstanceViewFactoryContext().getStatementContext().getSchedulingService().add(
				scheduleAfterMSec, closeByTimeoutCallbackHandle, scheduleSlot);
		
		logger.info("Callback scheduled after " + scheduleAfterMSec + " for " + this.currentTimeframeInterval.length());
	}
	
	private void resetCallback()
	{
		if (closeByTimeoutCallbackHandle != null)
		{
			getAgentInstanceViewFactoryContext().getStatementContext().getSchedulingService().remove(
					closeByTimeoutCallbackHandle, scheduleSlot);
			logger.info("Callback reset");
			closeByTimeoutCallbackHandle = null;
		}
	}
	
	public void postData(EventBean resultBean)	
	{
		postData(this, resultBean, lastEvent);
		lastEvent = resultBean;
	}
		
	@Override
	public View cloneView()
	{
		return new OHLCPlugInView(getAgentInstanceViewFactoryContext(), getViewParameters(), getEventType());
	}

	@Override
	public void stop()
	{
		logger.info("StopCallback triggered");
		resetCallback();
		currentOhlcValue = null;		
	}
	
	@Override
	protected Object[] getCurrentViewResults()
	{
		return new Object[]{currentOhlcValue};
	}
}
