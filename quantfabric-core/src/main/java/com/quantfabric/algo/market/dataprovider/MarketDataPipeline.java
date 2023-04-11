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
package com.quantfabric.algo.market.dataprovider;

import java.util.List;
import java.util.Set;

import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.messaging.NamedMapSubscriber;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.messaging.Subscriber;
import com.quantfabric.util.Startable;

public interface MarketDataPipeline extends Publisher<NamedMapSubscriber<Object>,QueryDataViewRequest,Object>,
								 Subscriber<Object>, Startable
{
	String getName();
	boolean isStarted();

	boolean isSingleThreadModel();
	void setSingleThreadModel(boolean singleThreadModel) ;
	boolean addFeedListener(FeedReference feed);
	boolean removeFeedListener(FeedReference feed);
	void addDataViews(List<DataView> views) throws Exception;
	Set<FeedReference> getListeningFeeds();
	FeedReference getListeningFeed(String feedName);
	MarketDataFeed getMarketDataFeed(String feedName);

	PipelineService getPipelineService(String serviceName);
}
