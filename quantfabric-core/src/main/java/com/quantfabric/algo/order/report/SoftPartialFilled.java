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
package com.quantfabric.algo.order.report;

import java.util.Date;

public class SoftPartialFilled extends Trade
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5249907762965532738L;

	public SoftPartialFilled()
	{
		super();
	}

	public SoftPartialFilled(long timestamp, long messageId, String sourceName,
			Date sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, int price,
			double quantity)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID, price,
				quantity);
	}

	public SoftPartialFilled(long timestamp, long messageId, String sourceName,
			long sourceTimestamp, String institutionOrderReference,
			String localOrderReference, String executionID, int price,
			double quantity)
	{
		super(timestamp, messageId, sourceName, sourceTimestamp,
				institutionOrderReference, localOrderReference, executionID, price,
				quantity);
	}

	public SoftPartialFilled(long messageId, String sourceName, Date sourceTimestamp,
			String institutionOrderReference, String localOrderReference,
			String executionID, int price, double quantity)
	{
		super(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionID, price, quantity);
	}

	public SoftPartialFilled(long messageId, String sourceName, long sourceTimestamp,
			String institutionOrderReference, String localOrderReference,
			String executionID, int price, double quantity)
	{
		super(messageId, sourceName, sourceTimestamp, institutionOrderReference,
				localOrderReference, executionID, price, quantity);
	}

	@Override
	public OrderStatus getOrderStatus()
	{
		return OrderStatus.ConfirmPartialFillPending;
	}
}
