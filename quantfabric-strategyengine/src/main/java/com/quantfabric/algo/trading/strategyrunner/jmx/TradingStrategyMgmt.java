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
package com.quantfabric.algo.trading.strategyrunner.jmx;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;
import com.quantfabric.algo.trading.strategy.DataSinkInfo;
import com.quantfabric.algo.trading.strategy.ExecutionPoint;
import com.quantfabric.algo.trading.strategy.StrategyEpStatement;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting;
import com.quantfabric.algo.trading.strategyrunner.jmx.mbean.TradingStrategyMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingStrategyMgmt implements TradingStrategyMXBean
{

	private static final Logger logger = LoggerFactory.getLogger(TradingStrategyMgmt.class);
	private final TradingStrategy theStrategy;

	public TradingStrategyMgmt(TradingStrategy strategy)
	{
		theStrategy = strategy;
	}

	@Override
	public String getName()
	{
		return theStrategy.getName();
	}

	@Override
	public String getType()
	{
		return theStrategy.getType();
	}

	@Override
	public void start() throws Exception
	{
		theStrategy.start();

	}

	@Override
	public void stop() throws Exception
	{
		theStrategy.stop();

	}

	@Override
	public String getDescription()
	{
		return theStrategy.getDescription();
	}

	@Override
	public void setDescription(String desc)
	{
		theStrategy.setDescription(desc);

	}

	@Override
	public Map<String, StrategySetting> getSettings()
	{
		return theStrategy.getSettings();
	}

	@Override
	public Map<String, String> getSettingValues()
	{
		return theStrategy.getSettingValues();
	}

	@Override
	public void setSettingValue(String name, String value)
	{
		try
		{
			theStrategy.setSettingValue(name, value);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public Set<ExecutionPoint> getExecutionEndPoints()
	{
		return theStrategy.getExecutionEndPoints();
	}

	@Override
	public Set<DataSinkInfo> getDataSinks()
	{
		return new HashSet<>(theStrategy.getDataSinks());
	}

	@Override
	public Set<StrategyEpStatement> getStrategyStatements()
	{
		return theStrategy.getStrategyStatements();
	}

	@Override
	public boolean isEnabled()
	{
		return theStrategy.isEnabled();
	}

	@Override
	public void setEnabled(boolean isEnabled)
	{
		theStrategy.setEnabled(isEnabled);
	}

	@Override
	public boolean isRunning()
	{
		return theStrategy.isRunning();
	}

	@Override
	public boolean isPlugged()
	{
		return theStrategy.isPlugged();
	}
	
	@Override
	public boolean isExecutionAllowed()
	{
		return theStrategy.isExecutionAllowed();
	}
	
	@Override
	public void setExecutionAllowed(boolean isExecutionAllowed)
	{
		theStrategy.setExecutionAllowed(isExecutionAllowed);		
	}

	@Override
	public int getStrategyDataStreamPort()
	{
		return theStrategy.getStrategyDataStreamPort();
	}

	@Override
	public void submitMarketOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size,TimeInForceMode timeInForce,int expireSec)
	{
		theStrategy.submitMarketOrder(source, executionPoint, side,
				instrumentId, size, timeInForce, expireSec);
	}

	@Override
	public void submitLimitOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size, int price,TimeInForceMode timeInForce,int expireSec)
	{
		theStrategy.submitLimitOrder(source, executionPoint, side,
				instrumentId, size, price, timeInForce, expireSec);

	}

	@Override
	public void cancelOrder(String source, String executionPoint,
			String originalOrderReference)
	{
		theStrategy.cancelOrder(source, executionPoint, originalOrderReference);
	}

	@Override
	public void replaceOrder(String source, String executionPoint,
			String originalOrderReference, String newOrderReference, int size,
			int price)
	{
		theStrategy.replaceOrder(source, executionPoint,
				originalOrderReference, newOrderReference, size, price);
	}

	@Override
	public void submitOCOOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size,
			OCOSettings ocoSettings)
	{
		theStrategy.submitOCOOrder(source, executionPoint, side, instrumentId,
				size, ocoSettings);
	}

	@Override
	public void submitStopLimitOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size, int price,
			StopSides stopSide, int stopPrice,TimeInForceMode timeInForce,int expireSec)
	{
		theStrategy.submitStopLimitOrder(source, executionPoint, side,
				instrumentId, size, price, stopSide, stopPrice,timeInForce, expireSec);
	}

	@Override
	public void submitStopLossOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size, StopSides stopSide,
			int stopPrice,TimeInForceMode timeInForce,int expireSec)
	{
		theStrategy.submitStopLossOrder(source, executionPoint, side,
				instrumentId, size, stopSide, stopPrice, timeInForce, expireSec);
	}

	@Override
	public void submitTrailingStopOrder(String source, String executionPoint,
			OrderSide side, String instrumentId, int size, int price,
			StopSides stopSide, int stopPrice, int trailBy, int maxSlippage,
			int initialTriggerRate,TimeInForceMode timeInForce,int expireSec)
	{
		theStrategy.submitTrailingStopOrder(source, executionPoint, side,
				instrumentId, size, price, stopSide, stopPrice, trailBy,
				maxSlippage, initialTriggerRate,timeInForce, expireSec);
	}

	@Override
	public String getSettingsLayoutDefinition()
	{
		return theStrategy.getSettingsLayoutDefinition();
	}

	@Override
	public boolean saveSettingsLayoutDefinitionToFile(String path)
	{
		try (FileWriter fw = new FileWriter(path))
		{
			fw.write(getSettingsLayoutDefinition());
		}
		catch (Exception e)
		{
			logger.error(e.toString());
			return false;
		}
		return true;
	}
}
