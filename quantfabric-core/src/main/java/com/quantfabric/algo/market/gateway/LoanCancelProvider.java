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
package com.quantfabric.algo.market.gateway;

import java.rmi.Remote;

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.InterruptFailed;
import com.quantfabric.algo.order.report.Interrupted;
import com.quantfabric.algo.order.report.Trade;

public interface LoanCancelProvider extends Remote
{
	void cancelLoanByTrade(TradeOrder order, Trade report);
	void cancelLoanByInterrupted(TradeOrder order, Interrupted report);
	void cancelLoanByReplaceFailed(TradeOrder order, InterruptFailed report, double failedReplaceSize);
}
