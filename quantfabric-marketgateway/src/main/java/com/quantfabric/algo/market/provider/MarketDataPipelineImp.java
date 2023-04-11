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
package com.quantfabric.algo.market.provider;


import java.util.*;

import com.quantfabric.algo.market.dataprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.Configuration;
import com.quantfabric.algo.market.gateway.MarketFeeder;
import com.quantfabric.algo.market.gateway.MarketGateway;
import com.quantfabric.algo.market.gateway.OrderBookSnapshotsProvider;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublishersManager;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.cep.CEPProvider;
import com.quantfabric.cep.ICEPProvider;
import com.quantfabric.messaging.BasePublisher;
import com.quantfabric.messaging.NamedMapSubscriber;
import com.quantfabric.messaging.NativeSubscriberBuffer;
import com.quantfabric.messaging.SubscriberBuffer;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;

public class MarketDataPipelineImp extends BasePublisher<NamedMapSubscriber<Object>, QueryDataViewRequest,Object>
									implements MarketDataPipeline
{
	private static final Logger log = LoggerFactory.getLogger(MarketDataPipelineImp.class);
	public static Logger getLogger()
	{
		return log;
	}
	
	private final MarketGateway owner;
	private ICEPProvider cep;
	private final PipelineHandlersManager handlersManager;
	private final DataViewManager dataViewManager = new DataViewManager();
	private final String name;
	private final HashSet<FeedReference> feeds = new HashSet<>();
	private boolean singleThreadModel;
	private NativeSubscriberBuffer singleInputChannel;
	private final Map<FeedReference, SubscriberBuffer<Object>> inputChannels = new HashMap<>();
	private final Map<String, PipelineService> pipelineServices = new HashMap<>();
	private boolean started;

	public MarketDataPipelineImp(
			MarketGateway marketGateway, 
			String pipelineName, 
			Configuration cepConfig,
			Collection<PersistingUpdateListenerConfig> persisterConfigs) 	{
		super();
		this.owner = marketGateway;
		this.name = pipelineName;

		initCep(cepConfig, persisterConfigs);
		handlersManager = new PipelineHandlersManager(cep);
		dataViewManager.setStatementProvider(handlersManager);
	}

	private void initCep(
			Configuration cepConfig,
			Collection<PersistingUpdateListenerConfig> persisterConfigs)
	{
		String uri = "MDP_"+ name;

		this.cep = CEPProvider.getCEPProvider(uri, cepConfig, persisterConfigs);

		cep.addNamedEventTypes("com.quantfabric.algo.market.datamodel");
	}

	//region Interface Startable
	@Override
	public void start()
	{

		for (FeedReference feed : feeds)
		{
			if (feed.isEnable())
			{
				MarketFeeder marketFeeder = getMarketFeeder(feed.getConnectionName());

				if (marketFeeder != null)
				{
					try {
						//get feed
						//MarketDataFeed mdFeed = marketConnection.getMarketDataFeed( feed.getFeedName());
						HashMap<String,String> viewCtx = new HashMap<String,String>();
						//create ctx for view definition
						viewCtx.put("feedName", feed.getFeedName().getName());
						//subscribe to feed
						if(singleThreadModel)
							marketFeeder.subscribe(singleInputChannel, feed.getFeedName());
						else
							marketFeeder.subscribe(inputChannels.get(feed), feed.getFeedName());


						feed.setConnected(true);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					getLogger().error("Undefined market connection \"" +
							feed.getConnectionName() + "\".");
				}
			}
		}
		if(singleThreadModel)
			singleInputChannel.start();

		this.started = true;
	}
	@Override
	public void stop()
	{
		if(singleThreadModel)
			singleInputChannel.stop();

		for (FeedReference feed : feeds)
		{
			if (feed.isEnable())
			{
				MarketFeeder marketFeeder = getMarketFeeder(feed.getConnectionName());

				if (marketFeeder != null)
				{
					try
					{
						if (feed.isConnected())
						{
							if(singleThreadModel)
								marketFeeder.unSubscribe(singleInputChannel, feed.getFeedName());
							else
								marketFeeder.unSubscribe(inputChannels.get(feed), feed.getFeedName());



							feed.setConnected(false);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
					feed.setConnected(false);
			}
		}

		this.started = false;
	}
	//endregion

	//region Iterface Subscriber
	@Override
	public void sendUpdate(Object data) {
		cep.sendEvent(data);
	}
	@Override
	public void sendUpdate(Object[] data) {
		for (int i=0; i<data.length; i++)
			cep.sendEvent(data[i]);
	}
	//endregion

    //region Interface BasePublisher
	@Override
	protected void initSubscription(NamedMapSubscriber<Object> subscriber,
									QueryDataViewRequest subject) throws Exception {
		dataViewManager.activateViews(subject.getDependences(), subject.getParameters());
		handlersManager.attachSubscriber(subscriber, subject.getQuery());
	}
	@Override
	protected void destroySubscription(NamedMapSubscriber<Object> subscriber,
									   QueryDataViewRequest subject) {
		handlersManager.detachSubscriber(subscriber,  subject.getQuery());
	}
	//endregion Interface BasePublisher

	//region Interface MarketDataPipeline
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isStarted()
	{
		return started;
	}

	@Override
	public boolean isSingleThreadModel() {
		return singleThreadModel;
	}

	@Override
	public void setSingleThreadModel(boolean singleThreadModel) {
		if(!this.singleThreadModel)
		{
			singleInputChannel =  new NativeSubscriberBuffer("mdpsinglethread", this);
			this.singleThreadModel = singleThreadModel;
		}
	}

	@Override
	public boolean addFeedListener(FeedReference feed)
	{
		if(feeds.add(feed))
		{
			inputChannels.put(feed, new SubscriberBuffer<Object>(name + "-" + feed.getConnectionName() +"-" + feed.getFeedName().getName(), this));
		}
		else
			return false;

		return true;
	}

	@Override
	public boolean removeFeedListener(FeedReference feed)
	{
		if (feeds.remove(feed))
		{
			inputChannels.remove(feed).dispose();
		}
		else
			return false;

		return true;
	}

	@Override
	public FeedReference getListeningFeed(String feedName)
	{
		for (FeedReference feed : getListeningFeeds())
			if (feed.getFeedName().getName().equals(feedName))
				return feed;

		return null;
	}

	@Override
	public Set<FeedReference> getListeningFeeds()
	{
		return feeds;
	}

	@Override
	public MarketDataFeed getMarketDataFeed(String feedName)
	{
		FeedReference feed = getListeningFeed(feedName);

		return feed == null ? null :
				owner.getMarketDataFeedProvider(feed.getConnectionName()).getMarketDataFeed(feed.getFeedName());
	}

	@Override
	public PipelineService getPipelineService(String serviceName)
	{
		return pipelineServices.get(serviceName);
	}

	@Override
	public void addDataViews(List<DataView> views) throws Exception {
		for(DataView view : views){
			dataViewManager.addDescription(view);
		}
	}
	//endregion


	protected SubscriberBuffer<Object> getInputChannel(FeedReference feed)
	{
		return inputChannels.get(feed);
	}

	protected MarketFeeder getMarketFeeder(String name)
	{
		return owner.getMarketFeeder(name);
	}
	
	protected OrderBookSnapshotsProvider getOrderBookSnapshotsProvider(String name)
	{
		return owner.getOrderBookSnapshotsProvider(name);
	}

	protected void registerPipelineService(String serviceName, PipelineService pipelineService)
	{
		pipelineServices.put(serviceName, pipelineService);
	}

	protected void unregisterPipelineService(String serviceName)
	{
		pipelineServices.remove(serviceName);
	}
	
	protected PublishersManager getPublishersManager(String pubManagerName)
	{
		return owner.getPublishersManager(pubManagerName);
	}
}
