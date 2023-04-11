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
package com.quantfabric.algo.backtesting;

import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.commands.CommandExecutor;
import com.quantfabric.algo.commands.ConcreteCommand;
import com.quantfabric.algo.market.gateway.commands.SubmitOrderCommand;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.Accepted;
import com.quantfabric.algo.order.report.Filled;
import com.quantfabric.messaging.Publisher.PublisherException;

public class BackTestingSubmitOrderCommand extends SubmitOrderCommand implements ConcreteCommand
{
	private static final Logger logger = LoggerFactory.getLogger(BackTestingSubmitOrderCommand.class);
	public BackTestingSubmitOrderCommand(TradeOrder order)
	{
		super(order);
	}

	@Override
	public void execute(CommandExecutor commandExecuter)
	{
		BackTestingMarketAdapter adapter = (BackTestingMarketAdapter)commandExecuter;
		
		Accepted acceptedReport = new Accepted(adapter.getNextMessageId(), 
						adapter.getVenueName(), 
						GregorianCalendar.getInstance().getTime(), 
						adapter.getInstitutionOrderReference(getOrder().getOrderReference()), 
						getOrder().getOrderReference(), 
						adapter.getNextExecutionId());
		
		try
		{
			adapter.publish(acceptedReport);
		}
		catch (PublisherException e)
		{
			logger.error("Can't publish Accepted report", e);
		}
		
		Filled filledReport = 
				new Filled(adapter.getNextMessageId(), 
						adapter.getVenueName(), 
						GregorianCalendar.getInstance().getTime(), 
						adapter.getInstitutionOrderReference(getOrder().getOrderReference()), 
						getOrder().getOrderReference(), 
						adapter.getNextExecutionId(), 
						getOrder().getPrice(), 
						getOrder().getSize());
		
		try
		{
			adapter.publish(filledReport);
		}
		catch (PublisherException e)
		{
			logger.error("Can't publish Filled report", e);
		}
	}

	@Override
	public String getDescription()
	{
		return "BackTestingSubmitOrderCommand";
	}

}
