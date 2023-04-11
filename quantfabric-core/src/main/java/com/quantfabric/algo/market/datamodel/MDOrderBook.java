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

import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookView;

public class MDOrderBook extends BaseMDFeedEvent implements Cloneable
{
	private static final long serialVersionUID = 4454408658940570419L;
	
	private long snapshotId;
	private long sourceTimestamp;
	private OrderBookView bids;
	private OrderBookView offers;
	
	public MDOrderBook()
	{
		super();
	}
	public MDOrderBook(MDFeedEvent event)
	{
		super(event);
	}
	
	protected MDOrderBook(MDOrderBook book)
	{
		super(book);
		this.snapshotId = book.snapshotId;
		this.sourceTimestamp = book.sourceTimestamp;
		this.bids =	book.bids.clone();
		this.offers = book.offers.clone();
	}
	
	public OrderBookView getBids()
	{
		return bids;
	}
	public void setBids(OrderBookView bids)
	{
		this.bids = bids;
	}
	public OrderBookView getOffers()
	{
		return offers;
	}
	public void setOffers(OrderBookView offers)
	{
		this.offers = offers;
	}
	public long getSnapshotId()
	{
		return snapshotId;
	}
	public void setSnapshotId(long snapshotId)
	{
		this.snapshotId = snapshotId;
	}
	
	public long getSourceTimestamp()
	{
		return sourceTimestamp;
	}
	public void setSourceTimestamp(long sourceTimestamp)
	{
		this.sourceTimestamp = sourceTimestamp;
	}
	
	@Override
	public MDOrderBook clone() 
	{
		return new MDOrderBook(this);
	}
}
