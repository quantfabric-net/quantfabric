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
package com.quantfabric.algo.market.gate.jmx;

import java.util.HashMap;
import java.util.Map;

import com.quantfabric.algo.market.dataprovider.FeedReference;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.gate.jmx.mbean.MarketDataPipelineMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketDataPipelineMgmt implements MarketDataPipelineMBean
{
	MarketDataPipeline pipeline;

	private static final Logger log = LoggerFactory.getLogger(MarketDataPipelineMgmt.class);
	
	public MarketDataPipelineMgmt(MarketDataPipeline pipeline)
	{
		this.pipeline = pipeline;
	}

	@Override
	public String getName()
	{
		return pipeline.getName();
	}

	@Override
	public void start() 
	{
		try {
			pipeline.start();
		} catch (Exception e) {
			log.error(e.getMessage());
		}		
	}

	@Override
	public void stop()
	{
		try {
			pipeline.stop();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public Map<String, String> getPipelineFeeds()
	{
		Map<String, String> feeds = new HashMap<String, String>();
		
		for (FeedReference feedRef : pipeline.getListeningFeeds())
			feeds.put(feedRef.getFeedName().getName(), feedRef.getConnectionName());
		
		return feeds;
	}

	@Override
	public boolean isStrated()
	{
		return pipeline.isStarted();
	}	
}
