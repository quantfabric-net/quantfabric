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

import com.quantfabric.algo.order.TradeOrder.OrderType;


public interface ReplaceOrder extends ManageAcceptedOrder
{
	int UNSPECIFIED_REPLACEMENT_PRICE = 0;
	int UNSPECIFIED_REPLACEMENT_STOP_PRICE = 0;
	double UNSPECIFIED_REPLACEMENT_SIZE = 0D;
	double UNSPECIFIED_ORDER_FILLED_SIZE = 0D;
	
	String getReplacementOrderReference();
	void setReplacementOrderReference(String replacementOrderReference);
	OrderType getReplacementOrderType();
	void setReplacementOrderType(OrderType replacementOrderType);
	double getReplacementSize();
	void setReplacementSize(double replacementSize);
	double getOrderFilledSize();
	void setOrderFilledSize(double orderFilledSize);
	int getReplacementPrice();
	void setReplacementPrice(int replacementPrice);
	int getReplacementPrice2();
	void setReplacementPrice2(int replacementPrice2);
	int getReplacementStopPrice();
	void setReplacementStopPrice(int replacementStopPrice);
	boolean isRequiredInstitutionOrderReference();
	void setRequiredInstitutionOrderReference(boolean requiredInstitutionOrderReference);
}
