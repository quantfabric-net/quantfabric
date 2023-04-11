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
package com.quantfabric.algo.market.datamodel;

public class ComplexAccumulatedOHLC extends BaseMDFeedEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9009485817265018281L;
	private long snapshotId;
	private OHLCUpdate genericOHLCUpdate = null;
	private OHLCUpdate tradeOHLCUpdate = null;
	private boolean closed;
	private boolean closedByTimeout;
	private long closedTimestamp;
	
	
	public ComplexAccumulatedOHLC() {
		
		super();
	}
	
	public ComplexAccumulatedOHLC(long snapshotId) {
		
		super();
		this.snapshotId = snapshotId;
	}
	
	public ComplexAccumulatedOHLC(MDFeedEvent event, long snapshotId) {
		
		super(event);
		this.snapshotId = snapshotId;
	}
	
	public ComplexAccumulatedOHLC(String symbol, int feedId, int feedGroupId, String feedName, String instrumentId, int pointsInOne, 
			long snapshotId, OHLCUpdate genericOHLCUpdate, OHLCUpdate tradeOHLCUpdate, boolean closed, boolean closedByTimeout, long closedTimestamp) {
		
		super();
		
		setFeedInfo(symbol, feedId, feedGroupId, feedName, instrumentId, pointsInOne);
		
		this.snapshotId = snapshotId;
		this.genericOHLCUpdate = genericOHLCUpdate;
		this.tradeOHLCUpdate = tradeOHLCUpdate;
		this.closed = closed;
		this.closedByTimeout = closedByTimeout;
		this.closedTimestamp = closedTimestamp;
	}
	public void update(Object event) {
		
		OHLCUpdate ohlcUpdate = (OHLCUpdate) event;
		
		setFeedInfo(ohlcUpdate);
		
		if (ohlcUpdate.getOHLC().getTradeCount() > 0)
			setTradeOHLCUpdate(ohlcUpdate);
		else
			setGenericOHLCUpdate(ohlcUpdate);
	}

	public long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(long snapshotId) {
		this.snapshotId = snapshotId;
	}

	public OHLCUpdate getGenericOHLCUpdate() {
		return genericOHLCUpdate;
	}

	public void setGenericOHLCUpdate(OHLCUpdate genericOHLCUpdate) {
		this.genericOHLCUpdate = genericOHLCUpdate;
	}

	public OHLCUpdate getTradeOHLCUpdate() {
		return tradeOHLCUpdate;
	}

	public void setTradeOHLCUpdate(OHLCUpdate tradeOHLCUpdate) {
		this.tradeOHLCUpdate = tradeOHLCUpdate;
	}
	
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public long getClosedTimestamp() {
		return closedTimestamp;
	}

	public void setClosedTimestamp(long closedTimestamp) {
		this.closedTimestamp = closedTimestamp;
	}
	
	public boolean isClosedByTimeout() {
		return closedByTimeout;
	}

	public void setClosedByTimeout(boolean closedByTimeout) {
		this.closedByTimeout = closedByTimeout;
	}

	public void close(boolean closedByTimeout) {
		
		closed = true;
		this.closedByTimeout = closedByTimeout;
		closedTimestamp = System.currentTimeMillis();
	}
	
	private void setFeedInfo(String symbol, int feedId, int feedGroupId, String feedName, String instrumentId, int pointsInOne) {
		
		setSymbol(symbol);
		setFeedId(feedId);
		setFeedGroupId(feedGroupId);
		setFeedName(feedName);
		setInstrumentId(instrumentId);
		setPointsInOne(pointsInOne);
	}
	
	private void setFeedInfo(OHLCUpdate event) {
		
		setSymbol(event.getSymbol());
		setFeedId(event.getFeedId());
		setFeedGroupId(event.getFeedGroupId());
		setFeedName(event.getFeedName());
		setInstrumentId(event.getInstrumentId());
		setPointsInOne(event.getPointsInOne());
	}

	@Override
	public ComplexAccumulatedOHLC clone() {
		
		return new ComplexAccumulatedOHLC(getSymbol(), getFeedId(), getFeedGroupId(), getFeedName(), getInstrumentId(), getPointsInOne(),
				snapshotId, genericOHLCUpdate, tradeOHLCUpdate, closed, closedByTimeout, closedTimestamp);
	}
	
	@Override
	public String toString() {
		
		return "ComplexAccumulatedOHLC: isClosed=[" + closed + "], isClosedByTimeout=[" + closedByTimeout + "], closeTimestamp=[" + closedTimestamp + "], "
				+ "genericOHLCUpdate=[" + genericOHLCUpdate + "], tradeOHLCUpdate=[" + tradeOHLCUpdate + "]";
	}
}
