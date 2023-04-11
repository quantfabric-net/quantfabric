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
package com.quantfabric.algo.trading.execution.report;

import java.util.Date;

import com.quantfabric.algo.order.TradeOrder.OrderSide;

public class TradeReport implements ExecutionReport {

	private String tradeReportID;
	private boolean previouslyReported;
	private String symbol;
	private double lastQty;
	private double lastPx;
	private long tradeDate;
	private Date transactTime;
	private String execId;
	private long settlDate;
	private int partyRole;
	private int noSides;
	private int noPartyIds;
	private char partyIdSource;

	private OrderSide side;
	private String orderId;

	private String partyId;
	
	public TradeReport() {
		super();
		noSides = 1;
		noPartyIds = 1;
		partyIdSource = 'D';
		partyRole = 1;
	}

	public int getNoSides() {
		return noSides;
	}

	public int getNoPartyIds() {
		return noPartyIds;
	}

	public int getPartyRole() {
		return partyRole;
	}

	public char getPartyIdSource() {
		return partyIdSource;
	}

	public String getTradeReportID() {
		return tradeReportID;
	}

	public boolean isPreviouslyReported() {
		return previouslyReported;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getLastQty() {
		return lastQty;
	}

	public double getLastPx() {
		return lastPx;
	}

	public long getTradeDate() {
		return tradeDate;
	}

	public Date getTransactTime() {
		return transactTime;
	}

	public String getExecId() {
		return execId;
	}

	public long getSettlDate() {
		return settlDate;
	}

	public OrderSide getSide() {
		return side;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getPartyId() {
		return partyId;
	}

	
	public void setTradeReportID(String tradeReportID) {
		this.tradeReportID = tradeReportID;
	}
	
	public void setPreviouslyReported(boolean previouslyReported) {
		this.previouslyReported = previouslyReported;
	}
	
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public void setLastQty(double lastQty) {
		this.lastQty = lastQty;
	}
	
	public void setLastPx(double lastPx) {
		this.lastPx = lastPx;
	}
	
	public void setTradeDate(long tradeDate) {
		this.tradeDate = tradeDate;
	}
	
	public void setTransactTime(Date transactTime) {
		this.transactTime = transactTime;
	}
	
	public void setExecId(String execId) {
		this.execId = execId;
	}
	
	
	public void setNoSides(int noSides) {
		this.noSides = noSides;
	}

	
	public void setNoPartyIds(int noPartyIds) {
		this.noPartyIds = noPartyIds;
	}

	
	public void setPartyIdSource(char partyIdSource) {
		this.partyIdSource = partyIdSource;
	}

	public void setSettlDate(long settlDate) {
		this.settlDate = settlDate;
	}
	
	public void setSide(OrderSide side) {
		this.side = side;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	
	public void setPartyRole(int partyRole) {
		this.partyRole = partyRole;
	}
}
