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
package com.quantfabric.algo.trading.strategy;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.quantfabric.algo.market.dataprovider.DataViewRequest;
import com.quantfabric.messaging.SubscriberBuffer;

public class DataSinkImpl implements Serializable, DataSink
{
	private static final long serialVersionUID = 5239628884469750769L;
	
	private static final AtomicInteger datasinkCounter = new AtomicInteger(0);
	private static final String DEFAULT_DATASINK_PREFIX_NAME = "DATA_SINK_";
	
	private final int datasinkId;
	private String name;
	private String pipeline;
	private DataViewRequest observation;
	private boolean isEmbedded;
	private boolean isActive;
	private SubscriberBuffer<Object> subscriberBuffer;

	@ConstructorProperties({ "name", "pipeline", "observation" })
	public DataSinkImpl(String name, String pipeline,
			DataViewRequest observation)
	{
		setName(name);
		setPipeline(pipeline);
		setObservation(observation);
		this.datasinkId = datasinkCounter.getAndIncrement();
	}

	@ConstructorProperties({ "name", "pipeline" })
	public DataSinkImpl(String name, String pipeline)
	{
		this(name, pipeline, null);
	}
	
	public DataSinkImpl()
	{
		this(DEFAULT_DATASINK_PREFIX_NAME + datasinkCounter.get(), "");
	}
		
	public int getDatasinkId()
	{
		return datasinkId;
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}

	@Override
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}

	@Override
	public void setName(String name)
	{
		if (isActive)
			throw new IllegalStateException(
					"Can not change property in active state");
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setObservation(DataViewRequest observation)
	{
		if (isActive)
			throw new IllegalStateException(
					"Can not change property in active state");
		this.observation = observation;
	}

	@Override
	public DataViewRequest getObservation()
	{
		return observation;
	}

	@Override
	public String getPipeline()
	{
		return pipeline;
	}

	@Override
	public void setPipeline(String pipeline)
	{
		if (isActive)
			throw new IllegalStateException(
					"Can not change property in active state");
		this.pipeline = pipeline;
	}

	@Override
	public boolean isEmbedded()
	{
		return isEmbedded;
	}

	@Override
	public void setSubscriberBuffer(SubscriberBuffer<Object> subscriberBuffer)
	{
		this.subscriberBuffer = subscriberBuffer;

	}

	@Override
	public SubscriberBuffer<Object> getSubscriberBuffer()
	{
		return this.subscriberBuffer;
	}
}