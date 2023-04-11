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
package com.quantfabric.algo.market.gateway.commands;

import com.quantfabric.algo.commands.Command;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;

import java.util.Collection;

public class BatchSubscribeCommand implements Command, Subscribe{

    private Collection<MarketDataFeed> mdFeeds;

    private Collection<ExecutionFeed> exFeeds;

    public BatchSubscribeCommand(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds){
        //TODO: add execution and market to one collection or add another feeds collection for execution
        this.mdFeeds = mdFeeds;
        this.exFeeds = exFeeds;
    }

    public Collection<MarketDataFeed> getMdFeeds() {
        return mdFeeds;
    }

    public Collection<ExecutionFeed> getExFeeds() {
        return exFeeds;
    }

    public void setMdFeeds(Collection<MarketDataFeed> mdFeeds) {
        this.mdFeeds = mdFeeds;
    }

    public void setExFeeds(Collection<ExecutionFeed> exFeeds) {
        this.exFeeds = exFeeds;
    }
}
