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
package com.quantfabric.algo.market.gateway.commands;

import com.quantfabric.algo.order.TradeOrder;

public class BaseManageOrderTrade extends BaseManageAcceptedOrder implements ManageOrderTrade
{
	private final String tradeExecutionId;
			
	public BaseManageOrderTrade()
	{
		super();
		this.tradeExecutionId = null;
	}

	public BaseManageOrderTrade(TradeOrder order,
			String institutionOrderReference, String tradeExecutionId)
	{
		super(order, institutionOrderReference);
		this.tradeExecutionId = tradeExecutionId;
	}

	public BaseManageOrderTrade(TradeOrder order, String tradeExecutionId)
	{
		super(order);
		this.tradeExecutionId = tradeExecutionId;
	}

	@Override
	public String getTradeExecutionId()
	{
		return tradeExecutionId;
	}
}
