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
package com.quantfabric.algo.configuration;

public class QuantfabricConstants {

    //Adapter settings
    public static final String SUBSCRIBE_TYPE = "subscribeType";
    public static final String POSTFIX = "postfix";
    public static final String BATCH_SUBSCRIPTION = "batch_subscription";
    public static final String CONFIG_URL = "configUrl";
    public static final String LOG_MESSAGES = "logMessages";

    public static final String BINANCE_TICKER_USE_REALTIME = "Binance_Ticker_Use_Realtime";

    //Credential
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String API_KEY = "apiKey";
    public static final String SECRET_KEY = "secretKey";
    public static final String ACCOUNT = "account";
    public static final String NAMESPACE = "namespace";

    //Feed
    public static final String TYPE = "type";
    public static final String MARKET_DATA_TYPE = "marketDataType";
    public static final String EXECUTION_TYPE = "executionType";
    public static final String NAME = "name";
    public static final String INSTRUMENT = "instrument";
    public static final String MARKET_DEPTH = "marketDepth";
    public static final String SAVE = "save";
    public static final String CHANNEL = "channel";
    public static final String FEED_GROUP_ID = "feedGroupId";

    public static final String CREDIT_LIMIT = "creditLimits";


    //FeedType
    public static final String MARKET_DATA = "marketData";
    public static final String EXECUTION = "execution";


    //MarketDepth
    public static final String FULL_MARKET_DEPTH = "FULL_MARKET_DEPTH";
    public static final String TOP_MARKET_DEPTH = "TOP_MARKET_DEPTH";
    public static final String DEFAULT_MARKET_DEPTH = "DEFAULT_MARKET_DEPTH";


    private QuantfabricConstants() {}
}
