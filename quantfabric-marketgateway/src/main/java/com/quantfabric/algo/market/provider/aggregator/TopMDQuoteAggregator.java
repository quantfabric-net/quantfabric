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
package com.quantfabric.algo.market.provider.aggregator;

import java.util.Properties;

import com.quantfabric.algo.market.datamodel.MDDealableQuote;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;

public class TopMDQuoteAggregator extends BaseMarketViewAggregator 
{	
	private boolean bidEndUpdate = false;
	private boolean offerEndUpdate = false;
	private boolean tradeEndUpdate = false;
	
	MDDealableQuote currentQuote = new MDDealableQuote();
	MDDealableQuote lastQuoteSnapshot = new MDDealableQuote();
	MDPrice currentBidTop;
	MDPrice currentOfferTop;
	
	private boolean quotePrePopulated = false;
	
	public TopMDQuoteAggregator(String name, Properties properties)
	{
		super(name, properties);
	}

	@Override   
	public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
			throws OrderBookSnapshotListenerException
	{
		synchronized (this)
		{
			MDPrice topPrice = orderBookSnapshot.getOrderBookView().getTop();
			MDTrade mdTrade = orderBookSnapshot.getOrderBookView().getTrade();
						
			if (mdTrade != null) {
				currentQuote.setTrade(mdTrade);
			}
			
			if (!quotePrePopulated && topPrice != null)
			{
				quotePrePopulated = true;
				currentQuote.pupulate(topPrice);
			}
			
			if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.BID_BOOK)
			{				
				if (topPrice == null || !topPrice.equals(currentBidTop))
				{
					currentQuote.populateBidSide(topPrice);
					currentQuote.setSourceTimestamp(orderBookSnapshot.getOrderBookView().getSourceTimestamp());
					currentBidTop = topPrice;
				}
			}
			else if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.OFFER_BOOK)
			{
				if (topPrice == null || !topPrice.equals(currentOfferTop))
				{
					currentQuote.populateOfferSide(topPrice);
					currentQuote.setSourceTimestamp(orderBookSnapshot.getOrderBookView().getSourceTimestamp());
					currentOfferTop = topPrice;
				}
			}
		}
	}
	
	private void createLastQuoteSnapshot(long snapshotId)
	{			
		try
		{
			this.lastQuoteSnapshot = currentQuote.clone();
			this.lastQuoteSnapshot.setSnapshotId(snapshotId);	
			
			this.currentQuote.resetUpdateStatus();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}	
	}
	
	public MDDealableQuote getTopQuote()
	{
		return this.lastQuoteSnapshot;
	}

	@Override
	public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified)
	{
		synchronized (this)
		{
			if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK)
				bidEndUpdate = true;
			else if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK)
				offerEndUpdate = true;
			else
				tradeEndUpdate = true;
			
			if (bidEndUpdate && offerEndUpdate)
			{		
				bidEndUpdate = false;
				offerEndUpdate = false;
				
				tradeEndUpdate = false;
				
				createLastQuoteSnapshot(updateId);
				
				publish(getTopQuote());		
			}
		}
	}

	@Override
	public void processNoUpdate(long snapshotId)
	{
		
	}
}
