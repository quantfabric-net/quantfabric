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

import com.quantfabric.algo.market.datamodel.MDItem;
import com.quantfabric.algo.market.datamodel.MDMessageInfo;
import com.quantfabric.algo.market.datamodel.MDTopOfBook;
import com.quantfabric.algo.market.datamodel.MDTrade;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.market.connector.xchange.XChangeQuantfabricService;
import com.quantfabric.market.connector.xchange.streaming.StreamingMarketDataService;
import com.quantfabric.util.UnsupportedMDTypeForExchange;
import info.bitrich.xchangestream.binance.BinanceStreamingExchange;
import info.bitrich.xchangestream.binance.dto.BinanceRawTrade;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.dto.marketdata.BinanceBookTicker;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import com.quantfabric.algo.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;

import static com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingXChangeAdapter.getCurrencyPair;
import static com.quantfabric.algo.market.connector.binance.stream.BinanceStreamingXChangeAdapter.getProductSubscriptionBuilder;

public class BinanceStreamingMarketDataService extends BinanceMarketDataServiceRaw
        implements XChangeQuantfabricService, StreamingMarketDataService {
    private static final Logger logger = LoggerFactory.getLogger(BinanceStreamingMarketDataService.class);
    BinanceStreamingXChangeAdapter adapter;

    BinanceStreamingMarketDataService(
            BinanceStreamingExchange exchange,
            BinanceAuthenticated binance,
            ResilienceRegistries resilienceRegistries,
            BinanceStreamingXChangeAdapter adapter) {
        super(exchange, binance, resilienceRegistries);
        this.adapter = adapter;
    }

    /*
     * Connect to Binance exchange
     * Set up handler for OrderBook -> BinanceXChangeStreamingAdapter.OrderBookObserver
     * Set up handler for Trades    -> BinanceXChangeStreamingAdapter.TradeObserver
     * */

    @Override
    public void subscribe(HashMap<Feed, BiConsumer<Feed, Object>>  feedEventMap) {

        logger.info("Connecting Binance Streaming Exchange");
        if(!(((BinanceStreamingXChange)exchange).isConnected())) {
            ((BinanceStreamingXChange)exchange).connect(
                    getProductSubscriptionBuilder(feedEventMap).build()
            ).blockingAwait();
        }

        for (Feed f : feedEventMap.keySet()) {
            if(f instanceof MarketDataFeed) {
                CurrencyPair currencyPair = getCurrencyPair(f.getInstrument());
                Disposable subscription = applyHandler(feedEventMap, f, currencyPair);
                f.setDisposable(subscription);
            }
        }
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        return ((BinanceStreamingXChange)exchange).getStreamingMarketDataService().getOrderBook(currencyPair);
    }

    @Override
    public Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
        return getOrderBook(getCurrencyPair(instrument), args);
    }

    @Override
    public Observable<MDTrade> getTrades(CurrencyPair currencyPair, Object... args) {
        //feed
        return (((BinanceStreamingXChange)exchange)
                .getStreamingMarketDataService()
                .getRawTrades(currencyPair))
                .map(trade -> fromRaw(trade, (MarketDataFeed) args[0])); //+fromRaw(Feed)
    }

    @Override
    public Observable<MDTrade> getTrades(Instrument instrument, Object... args) {
        return getTrades(getCurrencyPair(instrument), args);
    }

    @Override
    public Observable<MDTopOfBook> getTopOfBook(CurrencyPair currencyPair, Object... args) {
        return (((BinanceStreamingXChange)exchange)
                .getStreamingMarketDataService()
                .getRawBookTicker(currencyPair))
                .map(ticker -> fromRaw(ticker, (MarketDataFeed) args[0]));
    }

    @Override
    public Observable<MDTopOfBook> getTopOfBook(Instrument instrument, Object... args) {
        return getTopOfBook(getCurrencyPair(instrument), args);
    }

    private Disposable applyHandler(HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap, Feed feed, CurrencyPair currencyPair) {
        Disposable subscription = null;
        if(feed instanceof MarketDataFeed) {
            switch (((MarketDataFeed) feed).getMarketDataType()) {
                case ORDERBOOK:
                    subscription = getOrderBook(currencyPair, feed).subscribe(
                            //reference: BinanceStreamingXChangeAdapter.orderBookObservation()
                            orderBook -> feedEventMap.get(feed).accept(feed, orderBook),
                            throwable -> LOG.error("ERROR during processing order book: ", throwable)
                    );
                    break;
                case TRADES:
                    subscription = getTrades(currencyPair, feed).subscribe(
                            //reference: BinanceStreamingXChangeAdapter.tradeObservation()
                            trade -> feedEventMap.get(feed).accept(feed, trade),
                            throwable -> LOG.error("ERROR during processing trade: ", throwable)
                    );
                    break;
                case TOP_OF_BOOK:
                    subscription = getTopOfBook(currencyPair, feed).subscribe(
                            //reference: BinanceStreamingXChangeAdapter.topOfBookObservation()
                            ticker -> feedEventMap.get(feed).accept(feed, ticker),
                            throwable -> LOG.error("ERROR during processing trade: ", throwable)
                    );
                    break;
                default:
                    throw new UnsupportedMDTypeForExchange("BinanceStreamingExchange does not support {}" + ((MarketDataFeed) feed).getMarketDataType());
            }
        }
        return subscription;
    }

    private MDTrade fromRaw(BinanceRawTrade rawTrade, MarketDataFeed feed) { //,Feed feed)
        MDTrade mdTrade = new MDTrade();
        mdTrade.setMdItemId(new Date().toString());
        mdTrade.setMdItemType(rawTrade.isBuyerMarketMaker() ? MDItem.MDItemType.BID : MDItem.MDItemType.ASK);
        mdTrade.setSourceTimestamp(rawTrade.getTimestamp());
        mdTrade.setTradeSide(MDTrade.MDTradeSide.NA);
        mdTrade.setBuyerMarketMaker(rawTrade.isBuyerMarketMaker());
        mdTrade.setBuyerOrderId(rawTrade.getBuyerOrderId());
        mdTrade.setSellerOrderId(rawTrade.getSellerOrderId());

        Instrument instrument = adapter.getInstrumentProvider().getInstrumentBySymbol(rawTrade.getSymbol());
        mdTrade.setCurrency(instrument.getSymbol());
        mdTrade.setInstrumentId(instrument.getId());
        mdTrade.setPrice(instrument.castToLong(rawTrade.getPrice()));

        mdTrade.setFeedId(feed.getFeedId());
        mdTrade.setFeedGroupId(feed.getFeedGroupId());
        mdTrade.setFeedName(feed.getFeedName().getName());
        return mdTrade;
    }


    private MDTopOfBook fromRaw(BinanceBookTicker ticker, MarketDataFeed feed) {
        Instrument instrument = adapter.getInstrumentProvider().getInstrumentBySymbol(ticker.getSymbol());
        return new MDTopOfBook(
                new Date().getTime(), ticker.updateId, MDMessageInfo.MDMessageType.SNAPSHOT, ticker.getSymbol(),
                (ticker.toTicker().getTimestamp() != null ? ticker.toTicker().getTimestamp() : new Date()),
                (ticker.getAskQty().intValue() + ticker.getBidQty().intValue()),
                0, MDItem.MDItemType.TOP_OF_BOOK, ticker.getSymbol(),
                feed.getFeedId(), new Date().getTime(),
                ticker.getCurrencyPair().toString(), instrument.castToLong(ticker.getBidPrice()),
                ticker.getBidQty().doubleValue(), instrument.castToLong(ticker.getAskPrice()),
                ticker.getAskQty().doubleValue()
        );

    }
}
