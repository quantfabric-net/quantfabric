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

public class SpreadCorrector implements MarketDataProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(SpreadCorrector.class);

	private OrderBookSnapshot lastBidUpdate;
	private OrderBookSnapshot lastOfferUpdate;
	
	private OrderBookSnapshot lastCorrectedBidUpdate;
	private OrderBookSnapshot lastCorrectedOfferUpdate;

	private OrderBookInfo bidOrderBookInfo;
	private long bidUpdateId;
	private boolean isBidBookModified;

	private OrderBookInfo offerOrderBookInfo;
	private long offerUpdateId;
	private boolean isOfferBookModified;

	private boolean bidEndUpdate = false;
	private boolean offerEndUpdate = false;

	private long previousBid;
	private long previousOffer;
	
	private final MarketDataProcessor consumer;
	
	private final int spreadThreshold;
	private final int synthSpread;

	private boolean isPricesCorrected = false;
	
	public SpreadCorrector(int spreadThreshold, int synthSpread, MarketDataProcessor consumer)
	{
		this.spreadThreshold = spreadThreshold;
		this.synthSpread = synthSpread;
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
		isPricesCorrected = false;
		
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
				if (!testSpread(bid, offer))
				{
					if (previousBid != 0 && previousOffer != 0)
					{
						long bidChange = previousBid - bid.getPrice();
						long offerChange = previousOffer - offer.getPrice();

						long absBidChange = Math.abs(bidChange);
						long absOfferChange = Math.abs(offerChange);

						long bidSynth = bid.getPrice();
						long offerSynth = offer.getPrice();
						
						if (Math.min(absBidChange, absOfferChange) == absBidChange)
						{
							offerSynth = bid.getPrice() + synthSpread;
						}
						else
						{
							bidSynth = offer.getPrice() - synthSpread;
						}
						
						logger.debug(String.format("Prices (intrumentId=%s) corrected by spread threshold (%d) with synthetic spread (%d). Prices: %s->%s / %s->%s)",
							bid.getInstrumentId(), spreadThreshold, synthSpread, bid.getPrice(), bidSynth, offer.getPrice(), offerSynth));
							
						//clone OrderBookSnapshot to make safe changes 
						lastCorrectedBidUpdate = lastBidUpdate.clone();
						lastCorrectedOfferUpdate = lastOfferUpdate.clone();
						
						//Update TOP OF BOOK with synthetic values
						lastCorrectedBidUpdate.getOrderBookView().getTop().setPrice(bidSynth);
						lastCorrectedOfferUpdate.getOrderBookView().getTop().setPrice(offerSynth);
						
						isPricesCorrected = true;
					}
				}
				previousBid = bid.getPrice();
				previousOffer = offer.getPrice();
				
				try
				{
					consumer.processNewSnapshot(getLastBidUpdate());
					consumer.processNewSnapshot(getLastOfferUpdate());
				}
				catch (OrderBookSnapshotListenerException e)
				{
					logger.error(
							"Error during publishing corrected marketdata",
							e);
				}

				consumer.processEndUpdate(bidOrderBookInfo, bidUpdateId,
						isBidBookModified);
				consumer.processEndUpdate(offerOrderBookInfo,
						offerUpdateId, isOfferBookModified);
					
			}
			bidEndUpdate = false;
			offerEndUpdate = false;
		}
	}

	private OrderBookSnapshot getLastOfferUpdate()
	{
		return isPricesCorrected ? lastCorrectedOfferUpdate : lastOfferUpdate;
	}

	private OrderBookSnapshot getLastBidUpdate()
	{
		return isPricesCorrected ? lastCorrectedBidUpdate : lastBidUpdate;
	}

	private boolean testSpread(MDPrice bid, MDPrice offer)
	{
		long spreadValue = Math.abs(bid.getPrice() - offer.getPrice());
		return spreadValue <= spreadThreshold;
	}
}
