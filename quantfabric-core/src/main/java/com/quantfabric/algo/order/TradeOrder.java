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
package com.quantfabric.algo.order;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.quantfabric.algo.instrument.Instrument;

public class TradeOrder implements Cloneable, Serializable
{
	private static final long serialVersionUID = -4765849414064097089L;

	private static final AtomicLong id = new AtomicLong(0);
	
	public static long generateID()
	{
		return id.getAndIncrement();
	}
	
	public static String generateReference()
	{
		return String.valueOf(generateID());
	}
	
	public static String generateReference(String strategyOrderReference)
	{
		return generateReference() + "/" + strategyOrderReference;
	}
		
	public enum OrderSide
	{
		BUY,
		SELL,
	}
	
	public enum StopSides
	{
		DEFAULT,
		BID,
		OFFER
	}
	
	public enum TimeInForceMode
	{
		DAY,
		QUANTFABRIC_DAY,
		GOOD_TILL_CANCEL,
		IMMEDIATE_OR_CANCEL,
		FILL_OR_KILL,
		GOOD_TILL_DATE,
		GOOD_TILL_SECONDS,
	}
	
	public enum OrderType
	{
		MARKET,
		STOP_LOSS,
		STOP_LIMIT,
		LIMIT,
		QUANTFABRIC_LIMIT,
		PEGGED,
		TRAILING_STOP,
		FOREX_MARKET,
		FOREX_LIMIT,
		ONE_CANCELS_THE_OTHER,
		THRESHOLD,
		IF_DONE,
		IF_DONE_OCO,
		ICEBERG,
		MARKET_LIMIT,
		BTC_MARGIN_LIMIT,
		BTC_EXCHANGE_LIMIT,
		TAKE_PROFIT,
		LIMIT_MAKER,
		STOP_LOSS_LIMIT,
		TAKE_PROFIT_LIMIT
	}
	
	public enum ExecutionInstructions
	{
		ALL_OR_NONE,
		MARKET_PEG,
		PRIMARY_PEG
	}
	
	private Instrument instrument = null;
	private String instrumentId = null;
	private long price;
	private long price2;
	private long stopPrice;
	private StopSides stopSide = StopSides.DEFAULT;	
	private double minSize = UNSPECIFIED_MIN_SIZE; 
	private double size;
	private OrderSide orderSide;
	private TimeInForceMode timeInForceMode;
	
	private String orderReference; 
	private String complexOrderReference;
	private int complexOrderLegId;
	private long signalSourceTimestamp;
	
	private OrderType orderType;
	private boolean allowPartialFilling;
	private int expireSeconds;
	private Date expireDate;
	private OCOSettings ocoSettings;
	private IFDSettings ifdSettings;
	private PeggedSettings peggedSettings;
	private int trailBy;
	private double maxShow = UNSPECIFIED_MAX_SHOW;
	
	private ExecutionInstructions[] executionInstructions = null;
	
	public static final int UNSPECIFIED_MIN_SIZE = -1;
	public static final int UNSPECIFIED_MAX_SHOW = -1;
	public static final int UNSPECIFIED_MAX_SLIPPAGE = -1;
	public static final int UNSPECIFIED_INITIAL_TRIGGER_RATE = -1;
	
	private int maxSlippage = UNSPECIFIED_MAX_SLIPPAGE;
	private int initialTriggerRate = UNSPECIFIED_INITIAL_TRIGGER_RATE;
	
	public TradeOrder()
	{
		setOrderReference(generateReference());
	}
	
	public TradeOrder(String orderReference)
	{
		setOrderReference(orderReference);
	}
		
	public String getComplexOrderReference()
	{
		return complexOrderReference;
	}

	public void setComplexOrderReference(String complexOrderReference)
	{
		this.complexOrderReference = complexOrderReference;
	}
	
	public IFDSettings getIfdSettings()
	{
		return ifdSettings;
	}

	public void setIfdSettings(IFDSettings ifdSettings)
	{
		this.ifdSettings = ifdSettings;
	}

	public OCOSettings getOcoSettings()
	{
		return ocoSettings;
	}
	public void setOcoSettings(OCOSettings ocoSettings)
	{
		this.ocoSettings = ocoSettings;
	}
	public boolean getAllowPartialFilling()
	{
		return allowPartialFilling;
	}
	public void setAllowPartialFilling(boolean allowPartialFilling)
	{
		this.allowPartialFilling = allowPartialFilling;
	}
	public OrderType getOrderType()
	{
		return orderType;
	}
	public void setOrderType(OrderType orderType)
	{
		this.orderType = orderType;
	}
	public String getOrderReference()
	{
		return orderReference;
	}
	public void setOrderReference(String orderReference)
	{
		this.orderReference = orderReference;
	}
	public OrderSide getOrderSide()
	{
		return orderSide;
	}
	public void setOrderSide(OrderSide orderSide)
	{
		this.orderSide = orderSide; 
	}
	public TimeInForceMode getTimeInForceMode()
	{
		return timeInForceMode;
	}
	public void setTimeInForceMode(TimeInForceMode timeInForceMode)
	{
		this.timeInForceMode = timeInForceMode;
	}	
	public Instrument getInstrument()
	{
		return instrument;
	}
	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
	}
	public long getPrice()
	{
		return price;
	}
	public void setPrice(long price)
	{
		this.price = price;
	}	
	public long getPrice2()
	{
		return price2;
	}
	public void setPrice2(long price2)
	{
		this.price2 = price2;
	}	
	public long getStopPrice()
	{
		return stopPrice;
	}

	public void setStopPrice(long stopPrice)
	{
		this.stopPrice = stopPrice;
	}

	public double getSize()
	{
		return size;
	}
	public void setSize(double size)
	{
		this.size = size;
	}
	public void setExpireSeconds(int expireSeconds)
	{
		this.expireSeconds = expireSeconds;
	}
	public int getExpireSeconds()
	{
		return expireSeconds;
	}

	public String getInstrumentId()
	{
		return instrumentId;
	}

	public void setInstrumentId(String instrumentId)
	{
		this.instrumentId = instrumentId;
	}

	public StopSides getStopSide()
	{
		return stopSide;
	}

	public void setStopSide(StopSides stopSide)
	{
		this.stopSide = stopSide;
	}

	public PeggedSettings getPeggedSettings()
	{
		return peggedSettings;
	}

	public void setPeggedSettings(PeggedSettings peggedSettings)
	{
		this.peggedSettings = peggedSettings;
	}

	public int getTrailBy()
	{
		return trailBy;
	}

	public void setTrailBy(int trailBy)
	{
		this.trailBy = trailBy;
	}

	public int getMaxSlippage()
	{
		return maxSlippage;
	}

	public void setMaxSlippage(int maxSlippage)
	{
		this.maxSlippage = maxSlippage;
	}

	public int getInitialTriggerRate()
	{
		return initialTriggerRate;
	}

	public void setInitialTriggerRate(int initialTriggerRate)
	{
		this.initialTriggerRate = initialTriggerRate;
	}
	
	public int getComplexOrderLegId()
	{
		return complexOrderLegId;
	}

	public void setComplexOrderLegId(int complexOrderLegId)
	{
		this.complexOrderLegId = complexOrderLegId;
	}
	
	public long getSignalSourceTimestamp()
	{
		return signalSourceTimestamp;
	}

	public void setSignalSourceTimestamp(long signalSourceTimeStamp)
	{
		this.signalSourceTimestamp = signalSourceTimeStamp;
	}
	
	public double getMaxShow()
	{
		return maxShow;
	}

	public void setMaxShow(double maxShow)
	{
		this.maxShow = maxShow;
	}
	
	public ExecutionInstructions[] getExecutionInstructions()
	{
		return executionInstructions;
	}

	public void setExecutionInstructions(
			ExecutionInstructions[] executionInstructions)
	{
		this.executionInstructions = executionInstructions;
	}
		
	public Date getExpireDate()
	{
		return expireDate;
	}

	public void setExpireDate(Date expireDate)
	{
		this.expireDate = expireDate;
	}
	
	public double getMinSize()
	{
		return minSize;
	}

	public void setMinSize(double minSize)
	{
		this.minSize = minSize;
	}

	@Override
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	@Override
	public String toString() {
		return "TradeOrder [instrument=" + instrument + ", instrumentId=" + instrumentId + ", price=" + price + ", price2=" + price2 + ", stopPrice="
				+ stopPrice + ", stopSide=" + stopSide + ", minSize=" + minSize + ", size=" + size + ", orderSide=" + orderSide + ", timeInForceMode="
				+ timeInForceMode + ", orderReference=" + orderReference + ", complexOrderReference=" + complexOrderReference + ", complexOrderLegId="
				+ complexOrderLegId + ", signalSourceTimestamp=" + signalSourceTimestamp + ", orderType=" + orderType + ", allowPartialFilling="
				+ allowPartialFilling + ", expireSeconds=" + expireSeconds + ", expireDate=" + expireDate + ", ocoSettings=" + ocoSettings + ", ifdSettings="
				+ ifdSettings + ", peggedSettings=" + peggedSettings + ", trailBy=" + trailBy + ", maxShow=" + maxShow + ", executionInstructions="
				+ Arrays.toString(executionInstructions) + ", maxSlippage=" + maxSlippage + ", initialTriggerRate=" + initialTriggerRate + "]";
	}
}
