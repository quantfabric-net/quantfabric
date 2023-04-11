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
package com.quantfabric.algo.market.dataprovider.incremental;

import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.IncrementalUpdate;
import com.quantfabric.algo.market.datamodel.MDDelete;
import com.quantfabric.algo.market.datamodel.MDFeedEvent;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MarketConnectionAlert;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.dataprovider.BaseMarketDataItemsHandler;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;


public class IncrementalUpdatesHandler extends BaseMarketDataItemsHandler {

	private final Publisher publisher;
	private final IncrementalUpdatesCreator producer;
	private final MDItemType monitoredMdItemType;
	private final FeedName feedName;
	
			
	public IncrementalUpdatesHandler(MarketFeeder marketFeeder, FeedName feedName, MDItemType monitoredMdItemType, Publisher publisher,
			IncrementalUpdatesCreator producer) {
		super(marketFeeder, feedName, monitoredMdItemType);
		
		this.publisher = publisher;		
		this.producer = producer;		
		this.monitoredMdItemType = monitoredMdItemType;
		this.feedName = feedName;
	}

	@Override
	public void handleMarketConnectionAlert(MarketConnectionAlert event) throws FeedHandlerException {
		
	}

	@Override
	public void handleNewSnapshot(NewSnapshot event) throws FeedHandlerException {
		
		producer.setNewSnapshot(true);
	}

	@Override
	public void handleEndUpdate(EndUpdate event, boolean isMine) throws FeedHandlerException {
		
		if (isMine) {
			
			IncrementalUpdate incrUpdate = producer.commit(feedName.getName(), event.getMessageId(), monitoredMdItemType);
			
			if (incrUpdate != null)
				try {
					publisher.publish(incrUpdate);
				}
				catch (Exception e) {
					throw new FeedHandlerException("Incremental Update is not published", e);
				}
		}
	}

	@Override
	public void handleMdItem(MDFeedEvent mdItem) throws FeedHandlerException {
		
		if (mdItem instanceof MDPrice) {

			MDPrice mdPrice = (MDPrice) mdItem;

			long price = mdPrice.getPrice();
			int size = (int) mdPrice.getSize();
			int id = Integer.parseInt(mdPrice.getMdItemId());

			if (monitoredMdItemType == MDItemType.BID) {
				producer.addBidPrice(id, price, size);

			}
			if (monitoredMdItemType == MDItemType.OFFER) {
				producer.addAskPrice(id, price, size);
			}
		}

		if (mdItem instanceof MDDelete) {

			MDDelete mdDelete = (MDDelete) mdItem;
			int id = Integer.parseInt(mdDelete.getMdItemId());
			producer.deletePrice(id);
		}
	}

	@Override
	public String getName() {
		
		return getMonitoredFeedName() + " " + getMonitoredMdItemType();
	}
}
