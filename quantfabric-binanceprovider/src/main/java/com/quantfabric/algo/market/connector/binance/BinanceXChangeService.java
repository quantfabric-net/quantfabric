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
package com.quantfabric.algo.market.connector.binance;

import com.quantfabric.algo.market.datamodel.*;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.market.connector.xchange.MarketDataSingleEventHandler;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter;
import com.quantfabric.market.connector.xchange.XChangeQuantfabricService;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.marketdata.BinanceAggTrades;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exceptions.ExchangeException;


import java.io.IOException;
import java.util.*;

import static com.quantfabric.algo.market.dataprovider.BaseMarketDataItemsHandler.getLogger;


//MarketDataService BaseService
public class BinanceXChangeService extends BinanceMarketDataServiceRaw implements XChangeQuantfabricService {

    private final BinanceXChangeAdapter adapter;

    public BinanceXChangeService(
            BinanceExchange exchange,
            BinanceAuthenticated binance,
            ResilienceRegistries resilienceRegistries,
            BinanceXChangeAdapter adapter)
    {
        super(exchange, binance, resilienceRegistries);
        this.adapter = adapter;
    }

    protected BinanceOrderbook getOrderBook(CurrencyPair pair, Object... args) throws IOException {
        try {
            int limitDepth = 100;
            if (args != null && args.length == 1) {
                Object arg0 = args[0];
                if (!(arg0 instanceof Integer)) {
                    throw new ExchangeException("Argument 0 must be an Integer!");
                }

                limitDepth = (Integer)arg0;
            }

            return this.getBinanceOrderbook(pair, limitDepth);
        } catch (BinanceException var5) {
            throw BinanceErrorAdapter.adapt(var5);
        }
    }

    @Override
    public int getAndPublishOrderBook(long snapshotId,MarketDataFeed feed,long timestamp, MarketDataSingleEventHandler handler) throws XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException, IOException {

        CurrencyPair currency = XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(feed.getInstrument().getSymbol());

        BinanceOrderbook book = getOrderBook(currency,100);
        int itemCount = book.bids.size() + book.asks.size();
        int[] itemIndex = new int[1];

        String priceId="";

        book.bids.forEach((key, value) -> {
            long price = feed.getInstrument().castToLong(value.doubleValue());
            double volume = key.doubleValue();
            try {
                adapter.publish(
                        new MDPrice(snapshotId, MDMessageInfo.MDMessageType.SNAPSHOT, adapter.getSourceName(),
                                timestamp, itemCount, itemIndex[0]++, MDItem.MDItemType.BID, priceId, feed.getInstrument().getSymbol(),
                                feed.getFeedId(), price, volume, MDPrice.PriceType.DEALABLE, true, MDPrice.DEFAULT_AMOUNT_ORDERS)

                );
            } catch (Publisher.PublisherException e) {
                getLogger().error("Can't publish MDEvent", e);
            }
        });


        book.asks.forEach((key, value) -> {
            long price = feed.getInstrument().castToLong(value.doubleValue());
            double volume = key.doubleValue();
            try {
                adapter.publish(
                        new MDPrice(snapshotId, MDMessageInfo.MDMessageType.SNAPSHOT, adapter.getSourceName(),
                                timestamp, itemCount, itemIndex[0]++, MDItem.MDItemType.OFFER, priceId, feed.getInstrument().getSymbol(),
                                feed.getFeedId(), price, volume, MDPrice.PriceType.DEALABLE, true, MDPrice.DEFAULT_AMOUNT_ORDERS)

                );
            } catch (Publisher.PublisherException e) {
                getLogger().error("Can't publish MDEvent", e);
            }
        });

        return itemCount;
    }

    @Override
    public int getAndPublishTrade(long snapshotId, MarketDataFeed feed, long timestamp, MarketDataSingleEventHandler handler) throws XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException, IOException {
        int[] itemIndex = new int[1];

        CurrencyPair currency = XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(feed.getInstrument().getSymbol());
        //List<BinanceAggTrades> aggTrades = this.binance.aggTrades(currency, fromId, startTime, endTime, limit);
        List<BinanceAggTrades> aggTrades = this.binance.aggTrades(
                 BinanceAdapters.toSymbol(currency),
                null, timestamp, null, 1000);
        int itemCount = aggTrades.size();
        aggTrades.stream().forEach((tr)->{
            try {
                long price = feed.getInstrument().castToLong(tr.price.doubleValue());

                adapter.publish(
                        //TODO: Review MDtrade fields
                    new MDTrade(
                            tr.getTimestamp().getTime(),
                            MDMessageInfo.MDMessageType.INCREMENTAL_REFRESH,
                            adapter.getSourceName(),
                            new Date(timestamp),
                            itemCount,
                            itemIndex[0]++,
                            MDItem.MDItemType.TRADE,
                            feed.getInstrument().getSymbol(),
                            price,
                            BinanceAdapters.toSymbol(currency),
                            feed.getFeedId(),
                            MDTrade.MDTradeSide.BUY,
                            tr.buyerMaker,
                            0,
                            0)
                );

            } catch (Publisher.PublisherException e) {
                getLogger().error("Can't publish MDEvent", e);
            }
        });
        return aggTrades.size();
    }

}


