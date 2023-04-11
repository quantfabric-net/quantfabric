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

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

public class HistoricalDataViewRequestImpl extends AbstractDataViewRequest
		implements HistoricalDataViewRequest
{
	private final String serviceReference;
	private final String feedName;
	private final String timeFrame;
	private final int depth;
		
	public HistoricalDataViewRequestImpl(
			Set<String> dependences, Map<String, String> parameters,
			String serviceReference,String feedName, String timeFrame, int depth)
	{
		super(dependences, parameters);
		this.serviceReference = serviceReference;
		this.feedName = feedName;
		this.timeFrame = timeFrame;
		this.depth =  depth;
	}

	@Override
	public String getServiceReference()
	{
		return serviceReference;
	}

	@Override
	public String getFeedName()
	{
		return feedName;
	}

	@Override
	public String getTimeFrame()
	{
		return timeFrame;
	}

	@Override
	public int getDepth()
	{
		return depth;
	}
	
	@Override
	public String toString()
	{
		return "HistoricalDataViewRequestImpl [serviceReference="
				+ serviceReference + ", feedName=" + feedName + ", timeFrame="
				+ timeFrame + ", depth=" + depth + ", dependences="
				+ dependences + ", parameters=" + parameters + "]";
	}

	public static HistoricalDataViewRequest fromXML(Node queryDataViewRequestRootNode, 
			Set<String> dependences, Map<String, String> parameters)
	{
		String serviceName = parameters.get("serviceName");
		String feedName = parameters.get("feedName");
		String timeFrame = parameters.get("timeFrame");
		String depth = parameters.get("depth");			

		return new HistoricalDataViewRequestImpl(dependences, parameters,
				serviceName, feedName, timeFrame, Integer.parseInt(depth));		
	}
}
