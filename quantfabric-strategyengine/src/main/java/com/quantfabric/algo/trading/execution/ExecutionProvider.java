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
package com.quantfabric.algo.trading.execution;

import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.trading.execution.commands.CancelStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ConfirmTradeStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.RejectTradeStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ReplaceStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.report.TradeReport;
import com.quantfabric.algo.trading.execution.tradeMonitor.TradeMonitor;
import com.quantfabric.util.Startable;

public interface ExecutionProvider extends Startable
{
	void update(String executionPoint, TradeOrder order);
	void update(String executionPoint, CancelStrategyOrderCommand command);
	void update(String executionPoint, ReplaceStrategyOrderCommand command);
	void update(String executionPoint, ConfirmTradeStrategyOrderCommand command);
	void update(String executionPoint, RejectTradeStrategyOrderCommand command);
	void update(TradeReport tradeReport);
	TradeMonitor getTradeMonitor();
	void sendToStrategyDataStream(Object data);
	void sendToExecutionDataStream(Object data);
}
