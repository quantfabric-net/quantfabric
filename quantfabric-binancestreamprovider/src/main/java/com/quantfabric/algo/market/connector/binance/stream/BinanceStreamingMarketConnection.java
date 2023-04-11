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
package com.quantfabric.algo.market.connector.binance.stream;

import com.quantfabric.algo.market.connector.binance.BinanceMarketConnection;
import com.quantfabric.algo.market.gateway.MarketAdapter;
import com.quantfabric.algo.market.gateway.MarketConnectionException;
import com.quantfabric.algo.market.gateway.MarketGateway;
import org.knowm.xchange.ExchangeSpecification;


import java.util.Properties;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;

public class BinanceStreamingMarketConnection extends BinanceMarketConnection {
    private static final String USE_SANDBOX = "Use_Sandbox";
    public BinanceStreamingMarketConnection(MarketGateway owner, String name, Properties adapterSettings, Properties credentials) throws MarketConnectionException {
        super(owner, name, adapterSettings, credentials);
    }

    @Override
    protected MarketAdapter createMarketAdapter(Properties adapterSettings, Properties credentials) {
        return new BinanceStreamingXChangeAdapter(this, adapterSettings, credentials);
    }

    @Override
    public ExchangeSpecification createExchangeSpec() {
        ExchangeSpecification exSpec;
        if(settings.getProperty(POSTFIX).equalsIgnoreCase("com")) {
            exSpec = new BinanceStreamingXChange().getDefaultExchangeSpecification();
        } else {
            exSpec = new BinanceStreamingXChange().getDefaultUSExchangeSpecification();
        }

        if (credentials.containsKey(USERNAME))
            exSpec.setUserName(credentials.getProperty(USERNAME));
        if (credentials.containsKey(PASSWORD))
            exSpec.setPassword(credentials.getProperty(PASSWORD));
        if (credentials.containsKey(API_KEY))
            exSpec.setApiKey(credentials.getProperty(API_KEY));
        if (credentials.containsKey(SECRET_KEY))
            exSpec.setSecretKey(credentials.getProperty(SECRET_KEY));

        if(getAdapterSettings().containsKey(USE_SANDBOX)) {
            exSpec.setExchangeSpecificParametersItem(USE_SANDBOX,
                    Boolean.parseBoolean(getAdapterSettings().getProperty(USE_SANDBOX)));
        }
        if(getAdapterSettings().containsKey(BINANCE_TICKER_USE_REALTIME)) {
            exSpec.setExchangeSpecificParametersItem(BINANCE_TICKER_USE_REALTIME,
                    Boolean.parseBoolean(getAdapterSettings().getProperty(BINANCE_TICKER_USE_REALTIME)));
        }
        exSpec.setExchangeSpecificParametersItem("adapter", this.getAdapter());

        return exSpec;
    }

}
