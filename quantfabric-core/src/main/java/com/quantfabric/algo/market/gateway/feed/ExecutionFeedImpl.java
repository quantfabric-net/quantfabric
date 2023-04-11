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
package com.quantfabric.algo.market.gateway.feed;

import com.quantfabric.algo.instrument.InstrumentProvider;
import com.quantfabric.algo.market.dataprovider.FeedName;
import io.reactivex.disposables.Disposable;

public class ExecutionFeedImpl extends AbstractFeed implements ExecutionFeed
{

	ExecutionType type;
	Disposable disposable;


	public ExecutionFeedImpl(FeedName feedName)
	{
		super(feedName);
	}

	public ExecutionFeedImpl(FeedName feedName, InstrumentProvider instrumentProvider, String instrumentId, boolean cachingInstrument) {
		super(feedName, instrumentProvider, instrumentId, cachingInstrument);
	}

	@Override
	public ExecutionType getExecutionType() {
		return type;
	}
	@Override
	public void setExecutionType(ExecutionType type) {
		this.type = type;
	}

	@Override
	public Disposable getDisposable() {
		return disposable;
	}

	@Override
	public void setDisposable(Disposable disposable) {
		this.disposable = disposable;
	}


}
