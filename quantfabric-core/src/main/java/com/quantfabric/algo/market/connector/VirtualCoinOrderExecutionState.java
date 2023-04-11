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
package com.quantfabric.algo.market.connector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.quantfabric.algo.order.TradeOrder;
import org.knowm.xchange.dto.marketdata.Trade;

public class VirtualCoinOrderExecutionState {
	
	public enum VirtualCoinExecutionState {
		
		Accepted,
		Filled,
		PartiallyFilled,
		Cancelled,
		FilledWithoutAcceptance,
		PartiallyFilledWithoutAcceptance,
		Rejected,
		Unknown
	}
	
	public static class PartiallyFilledOrder {
		
		private final String orderId;
		private final double tradeAmount;
		private final double tradePrice;
		private boolean isReported;
		private Date timestamp;
		
		public PartiallyFilledOrder(String orderId, double tradeAmount, double tradePrice, Date timestamp) {
			
			this.orderId = orderId;
			this.tradeAmount = tradeAmount;
			this.tradePrice = tradePrice;
			this.timestamp = timestamp;
		}

		public String getOrderId() {
			return orderId;
		}

		public double getTradeAmount() {
			return tradeAmount;
		}

		public double getTradePrice() {
			return tradePrice;
		}

		public boolean isReported() {
			return isReported;
		}
		
		public Date getTimestamp() {
			return timestamp;
		}

		public void setReported(boolean isReported) {
			this.isReported = isReported;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		
		@Override
		public boolean equals(Object obj) {
			
			if (obj instanceof PartiallyFilledOrder) {
				PartiallyFilledOrder comparedOrder = (PartiallyFilledOrder) obj;

				return this.orderId.equals(comparedOrder.getOrderId()) &&
						this.timestamp.equals(comparedOrder.getTimestamp()) &&
						this.tradePrice == comparedOrder.getTradePrice() &&
						new BigDecimal(this.tradeAmount * VirtualCoinConfirmationBatch.POINTS_IN_ONE).longValue() == comparedOrder.getTradeAmount();
			}

			return false;
		}
		
		@Override
		public int hashCode() {
			
			return 0;
		}
		
		@Override
		public String toString() {
			
			return "PartiallyFilledOrder: orderId [" + this.orderId + "], timestamp [" + this.timestamp + "], "
					+ "tradeAmount [" + tradeAmount + "], tradePrice [" + this.tradePrice + "], isReported [" + this.isReported + "]";
		}
	}
	
	private VirtualCoinExecutionState executionState;
	private TradeOrder tradeOrder;
	private Trade trade;
	private String marketOrderReference;
	private boolean isAccepted = false;
	private boolean isCompleted = false;
	private boolean isPartiallyFilled = false;
	private long leavesQty;
	private final List<PartiallyFilledOrder> partialFills = new ArrayList<PartiallyFilledOrder>();
	private final Object lock = new Object();
	public static final int SIZE_CORRECTOR = 100;
	
	public VirtualCoinOrderExecutionState(TradeOrder tradeOrder, String marketOrderReference, boolean isOrderRejected) {
		setTradeOrder(tradeOrder);
		setMarketOrderReference(marketOrderReference);
		if (isOrderRejected)
			setExecutionState(VirtualCoinExecutionState.Rejected);
		else
			setExecutionState(VirtualCoinExecutionState.Unknown);
		leavesQty = (long) tradeOrder.getSize() * VirtualCoinConfirmationBatch.POINTS_IN_ONE / SIZE_CORRECTOR;
	}
	
	public List<PartiallyFilledOrder> getPartialFills() {
		
		synchronized (lock) {
			if (!partialFills.isEmpty())
				return partialFills;
		}

		return null;
	}
	
	public void addPartialFills(String orderReference, long tradeAmount, double tradePrice, Date timestamp) {
		
		synchronized (lock) {
			partialFills.add(new PartiallyFilledOrder(orderReference, tradeAmount, tradePrice, timestamp));		
			leavesQty -= tradeAmount;
		}
	}

	public VirtualCoinExecutionState getExecutionState() {
		return executionState;
	}

	public void setExecutionState(VirtualCoinExecutionState executionState) {
		this.executionState = executionState;
	}

	public TradeOrder getTradeOrder() {
		return tradeOrder;
	}

	public void setTradeOrder(TradeOrder tradeOrder) {
		this.tradeOrder = tradeOrder;
	}

	public String getMarketOrderReference() {
		return marketOrderReference;
	}

	public void setMarketOrderReference(String marketOrderReference) {
		this.marketOrderReference = marketOrderReference;
	}

	public Trade getTrade() {
		return trade;
	}

	public void setTrade(Trade trade) {
		this.trade = trade;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

	public void setAccepted(boolean isPreviouslyAccepted) {
		this.isAccepted = isPreviouslyAccepted;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public boolean isPartiallyFilled() {
		return isPartiallyFilled;
	}

	public void setPartiallyFilled(boolean isPartiallyFilled) {
		this.isPartiallyFilled = isPartiallyFilled;
	}

	public long getLeavesQty() {
		return leavesQty;
	}
	
	public double getAvgPx() {

		if (!isPartiallyFilled())
			return getTrade().getPrice().doubleValue();
		else {
			synchronized (lock) {
				double VWAP = 0;
				double totalQuantity = 0;
				List<PartiallyFilledOrder> partialFills = getPartialFills();
				for (PartiallyFilledOrder partialFill : partialFills) {
					VWAP += partialFill.getTradePrice() * partialFill.getTradeAmount();
					totalQuantity += partialFill.getTradeAmount();
				}				
				if (isCompleted) {
					VWAP += getTrade().getPrice().doubleValue() * Math.abs(trade.getOriginalAmount().doubleValue() * VirtualCoinConfirmationBatch.POINTS_IN_ONE);
					totalQuantity += Math.abs(trade.getOriginalAmount().doubleValue() * VirtualCoinConfirmationBatch.POINTS_IN_ONE);
				}
				return VWAP / totalQuantity;
			}
		}
	}

	public double getQuantity() {

		if (!isPartiallyFilled())
			return getTrade().getOriginalAmount().doubleValue();
		else {
			synchronized (lock) {
				double cumulativeQuantity = 0;
				List<PartiallyFilledOrder> partialFills = getPartialFills();
				for (PartiallyFilledOrder partialFill : partialFills) {
					cumulativeQuantity += partialFill.getTradeAmount() ;
				}
				
				if (isCompleted)
					cumulativeQuantity += Math.abs(trade.getOriginalAmount().doubleValue() * VirtualCoinConfirmationBatch.POINTS_IN_ONE);
				
				return cumulativeQuantity / VirtualCoinConfirmationBatch.POINTS_IN_ONE;
			}
		}
	}
}
