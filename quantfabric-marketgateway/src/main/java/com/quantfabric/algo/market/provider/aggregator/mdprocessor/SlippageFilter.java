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
package com.quantfabric.algo.market.provider.aggregator.mdprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener.OrderBookSnapshotListenerException;

public class SlippageFilter implements MarketDataProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(SlippageFilter.class);

	private OrderBookSnapshot lastBidUpdate;
	private OrderBookSnapshot lastOfferUpdate;

	private OrderBookInfo bidOrderBookInfo;
	private long bidUpdateId;
	private boolean isBidBookModified;

	private OrderBookInfo offerOrderBookInfo;
	private long offerUpdateId;
	private boolean isOfferBookModified;

	private boolean bidEndUpdate = false;
	private boolean offerEndUpdate = false;

	private final MarketDataProcessor consumer;
	private final long slippageFilter;

	public SlippageFilter(long slippageFilter, MarketDataProcessor consumer)
	{
		this.slippageFilter = slippageFilter;
		this.consumer = consumer;
	}

	@Override
	public void processNoUpdate(long snapshotId)
	{
		consumer.processNoUpdate(snapshotId);
	}

	@Override
	public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
			throws OrderBookSnapshotListenerException
	{
		if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.BID_BOOK)
			lastBidUpdate = orderBookSnapshot;
		else if (orderBookSnapshot.getOrderBookView().getOrderBookType() == OrderBookTypes.OFFER_BOOK)
			lastOfferUpdate = orderBookSnapshot;

	}

	@Override
	public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
			boolean isBookModified)
	{
		if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK)
		{
			bidOrderBookInfo = orderBookInfo;
			bidUpdateId = updateId;
			isBidBookModified = isBookModified;
			bidEndUpdate = true;
		}
		else if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK)
		{
			offerOrderBookInfo = orderBookInfo;
			offerUpdateId = updateId;
			isOfferBookModified = isBookModified;
			offerEndUpdate = true;
		}

		if (bidEndUpdate && offerEndUpdate)
		{
			MDPrice bid = lastBidUpdate.getOrderBookView().getTop();
			MDPrice offer = lastOfferUpdate.getOrderBookView().getTop();

			if (bid != null && offer != null)
			{
				if (testSlippage(bid, offer))
				{
					try
					{
						consumer.processNewSnapshot(lastBidUpdate);
						consumer.processNewSnapshot(lastOfferUpdate);
					}
					catch (OrderBookSnapshotListenerException e)
					{
						logger.error(
								"Error during publishing filtered marketdata",
								e);
					}

					consumer.processEndUpdate(bidOrderBookInfo, bidUpdateId,
							isBidBookModified);
					consumer.processEndUpdate(offerOrderBookInfo,
							offerUpdateId, isOfferBookModified);
				}
				else
					logger.info(String
							.format("Prices is rejected by Slippage filter (%d). Prices: %s, %s",
									slippageFilter, "[" + bid.getInstrumentId()
											+ " " + bid.getPrice() + "]", "["
											+ offer.getInstrumentId() + " "
											+ offer.getPrice() + "]"));
			}
			bidEndUpdate = false;
			offerEndUpdate = false;
		}
	}

	private boolean testSlippage(MDPrice bid, MDPrice offer)
	{
		long slippageValue = Math.abs(bid.getPrice() - offer.getPrice());
		return slippageValue <= slippageFilter;
	}
}
