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

import java.util.Properties;

import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.dataprovider.FeedHandler;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.publisher.Publisher;


public class IncrementalUpdatesProducer implements FeedHandler {
	
	private final IncrementalUpdatesHandler askHandler;
	private final IncrementalUpdatesHandler bidHandler;
	private final IncrementalUpdatesCreator creator;
	private final MarketFeeder marketFeeder;
	private final FeedName feedName;
	
	private IncrementalUpdatesProducer(MarketFeeder marketFeeder, FeedName feedName, Publisher publisher) {
		
		this.marketFeeder = marketFeeder;
		this.feedName = feedName;
		
		creator = new IncrementalUpdatesCreator();
		
		askHandler = new IncrementalUpdatesHandler(marketFeeder, feedName, MDItemType.OFFER, publisher, creator);
		bidHandler = new IncrementalUpdatesHandler(marketFeeder, feedName, MDItemType.BID, publisher, creator);
	}

	@Override
	public void start() throws Exception {
		
		askHandler.start();
		bidHandler.start();
	}

	@Override
	public void stop() throws Exception {
		
		askHandler.stop();
		bidHandler.stop();
	}

	@Override
	public String getName() {
		
		String askHandlerName = "IncrementalUpdatesHandler " + askHandler.getMonitoredFeedName() + " " + askHandler.getMonitoredMdItemType();
		String bidHandlerName = "IncrementalUpdatesHandler " + bidHandler.getMonitoredFeedName() + " " + bidHandler.getMonitoredMdItemType();
		
		return askHandlerName + " | " + bidHandlerName;
	}

	@Override
	public MarketFeeder getMarketFeeder() {
		
		return marketFeeder;
	}

	@Override
	public FeedName getMonitoredFeedName() {

		return feedName;
	}

	@Override
	public void handleMdEvent(MDEvent event) throws FeedHandlerException {
		
		askHandler.handleMdEvent(event);
		bidHandler.handleMdEvent(event);
	}
	
	public static IncrementalUpdatesProducer createIncrementalUpdatesProducer(MarketGateway gateway, String connectionName, String feedName, Properties settings) {
		
		FeedName feed = new FeedNameImpl(feedName);
		
		MarketFeeder feeder = gateway.getMarketFeeder(connectionName);
		
		Publisher publisher = gateway.getPublishersManager(settings.getProperty("publishersManager")).getPublisher(settings.getProperty("productCode"),
				ContentType.valueOf(settings.getProperty("contentTypes")));
				
		IncrementalUpdatesProducer producer = new IncrementalUpdatesProducer(feeder, feed, publisher);
		
		return producer;
	}
}
