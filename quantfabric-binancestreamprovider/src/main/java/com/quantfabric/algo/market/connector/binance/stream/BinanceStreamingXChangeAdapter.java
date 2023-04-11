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

import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.datamodel.*;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.OrderExecutionReport;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter;
import com.quantfabric.messaging.Publisher;
import com.quantfabric.util.UnsupportedMDTypeForExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.BiConsumer;

public class BinanceStreamingXChangeAdapter extends XChangeMarketAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BinanceStreamingXChangeAdapter.class);

    public BinanceStreamingXChangeAdapter(BinanceStreamingMarketConnection connection, Properties adapterSettings, Properties credentials) {
        super(connection, new BinanceStreamingCommandFactory(), adapterSettings);
    }

    public void tradeObservation(Feed feed, Object o) {
        MDTrade trade = (MDTrade) o;
        try {
            publish(trade);
        } catch (PublisherException e) {
            logger.error("Can't publish MDTrade", e);
        }
        logger.info("MDTrade - Next trade handled {}", feed.getFeedName());
    }

    public Feed getFeed(String symbol) {
        Feed feed = null;
        Collection<ExecutionFeed> executionFeeds = getFeedProvider().getExecutionFeeds();
        Collection<MarketDataFeed> marketDataFeeds = getFeedProvider().getMarketDataFeeds();

        if(!executionFeeds.isEmpty()) {
            for (ExecutionFeed f : executionFeeds) {
                if (f.getInstrumentId().equals(symbol))
                    feed = f;
            }
        } else if (!marketDataFeeds.isEmpty()) {
            for (MarketDataFeed f : marketDataFeeds) {
                if(f.getInstrumentId().equals(symbol))
                    feed = f;
            }
        }

        if (feed == null)
            getLogger().error("Can't find provided feed");

        return feed;
    }

    @Override
    protected HashMap<Feed, BiConsumer<Feed, Object>> createFeedEventMap(Collection<MarketDataFeed> mdFeeds, Collection<ExecutionFeed> exFeeds) {
        HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap = new HashMap<>();
        if(!mdFeeds.isEmpty()) {
            for (MarketDataFeed feed : mdFeeds) {
                switch (feed.getMarketDataType()) {
                    case TRADES:
                        feedEventMap.put(feed, this::tradeObservation);
                        break;
                    case ORDERBOOK:
                        feedEventMap.put(feed, this::orderBookObservation);
                        break;
                    case TOP_OF_BOOK:
                        feedEventMap.put(feed, this::topOfBookObservation);
                        break;
                    default:
                        throw new UnsupportedMDTypeForExchange(
                                "BinanceStreamingExchange does not support {}" + feed.getMarketDataType()
                        );
                }
            }
        }
        if(!exFeeds.isEmpty()) {
            for (ExecutionFeed feed : exFeeds) {
                switch (feed.getExecutionType()) {
                    case ORDER_CHANGES:
                        feedEventMap.put(feed, this::orderChangesObservation);
                        break;
                    case EXECUTION_REPORT:
                        feedEventMap.put(feed, this::userTradesObservation);
                        break;
                    default:
                        throw new UnsupportedMDTypeForExchange(
                                "BinanceStreamingExchange does not support {}" + feed.getExecutionType()
                        );
                }
            }
        }
        return feedEventMap;
    }

    @Override
    public void unsubscribeMarketData(MarketDataFeed feed) {
        connection.getMarketDataFeeds().remove(feed);
        feed.getDisposable().dispose();
        if(connection.getMarketDataFeeds().isEmpty() && connection.getExecutionFeeds().isEmpty())
            ((BinanceStreamingXChange)connection.getExchange()).disconnect();
    }

    public void unsubscribeExecution(ExecutionFeed feed) {
        connection.getExecutionFeeds().remove(feed);
        feed.getDisposable().dispose();
        if(connection.getMarketDataFeeds().isEmpty() && connection.getExecutionFeeds().isEmpty())
            ((BinanceStreamingXChange)connection.getExchange()).disconnect();
    }

    //BatchUnsub
    @Override
    public void unsubscribeMarketData(Collection<Feed> feeds) {
        for(Feed f : feeds) {
           f.getDisposable().dispose();
            if(f instanceof ExecutionFeed)
                connection.getExecutionFeeds().remove(f);
            if(f instanceof MarketDataFeed)
                connection.getMarketDataFeeds().remove(f);
        }
        ((BinanceStreamingXChange)connection.getExchange()).disconnect();
    }

    public void orderBookObservation(Feed feed, Object o) {
        OrderBook order = (OrderBook) o;
        int itemCount = order.getBids().size() + order.getAsks().size();

        int[] itemIndex = new int[1];

        String priceId = "";

        order.getBids().forEach(p -> {
            long price = feed.getInstrument().castToLong(p.getOriginalAmount());
            int size = order.getBids().size();
            try {
                publish(
                        new MDPrice(new Date().getTime(), MDMessageInfo.MDMessageType.SNAPSHOT, getSourceName(),
                                order.getTimeStamp(), itemCount, itemIndex[0]++, MDItem.MDItemType.BID, priceId,
                                feed.getInstrument().getSymbol(), feed.getFeedId(), price, size,
                                MDPrice.PriceType.DEALABLE, true, MDPrice.DEFAULT_AMOUNT_ORDERS)

                );
            } catch (Publisher.PublisherException e) {
                logger.error("Can't publish orderBook", e);
            }
            logger.info("OrderBook - Next order handled {}", feed.getFeedName());
        });

        order.getAsks().forEach(p -> {
            long price = feed.getInstrument().castToLong(p.getOriginalAmount());
            double volume = order.getBids().size();
            try {
                publish(
                        new MDPrice(new Date().getTime(), MDMessageInfo.MDMessageType.SNAPSHOT, getSourceName(),
                                order.getTimeStamp(), itemCount, itemIndex[0]++, MDItem.MDItemType.OFFER, priceId, feed.getInstrument().getSymbol(),
                                feed.getFeedId(), price, volume, MDPrice.PriceType.DEALABLE, true, MDPrice.DEFAULT_AMOUNT_ORDERS)

                );
            } catch (Publisher.PublisherException e) {
                getLogger().error("Can't publish orderBook", e);
            }
        });
    }

    public void topOfBookObservation(Feed feed, Object o) {
        MDTopOfBook book = (MDTopOfBook) o;
        try {
            publish(book);
        } catch (PublisherException e) {
            getLogger().error("Can't publish topOfBook", e);
        }
        logger.info("TopOfBook - Next topOfBook handled, feed: {}", feed.getFeedName());
    }

    public void userTradesObservation(Feed feed, Object o) {
        OrderExecutionReport rep = (OrderExecutionReport) o;
        try {
            publish(rep);
        } catch (PublisherException e) {
            logger.error("Can't publish Order Execution Report", e);
        }
        logger.info("Order Execution Report - Next report handled, feed: {}, type: {}",
                feed.getFeedName(), rep.getExecutionReportType());
    }

    public void orderChangesObservation(Feed feed, Object o) {
        TradeOrder order = (TradeOrder) o;
        try {
            publish(order);
        } catch (PublisherException e) {
            logger.error("Can't publish OrderChanges", e);
        }
        logger.info("Order Execution Report - Next OrderChanges handled, feed: {}, type: {}",
                feed.getFeedName(), order.getOrderType());
    }

    @Override
    public String getVenueName() {
        return "BINANCE-STREAMING";
    }

    public static CurrencyPair getCurrencyPair(MarketDataFeed feed) {
        CurrencyPair currency = null;
        try {
            currency = XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(feed.getInstrument().getSymbol());
        } catch (XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException e) {
            logger.error("Cant convert to currency pair", e);
        }
        return currency;
    }

    public static CurrencyPair getCurrencyPair(Instrument instrument) {
        CurrencyPair currencyPair;
        try {
            currencyPair = XChangeMarketAdapter.CurrencyConverter.toCurrencyPair(instrument.getSymbol());
        } catch (XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException e) {
            logger.error("Can't convert symbol to CurrencyPair ", e);
            throw new RuntimeException(e);
        }
        return currencyPair;
    }

    public static ProductSubscription.ProductSubscriptionBuilder getProductSubscriptionBuilder(HashMap<Feed, BiConsumer<Feed, Object>> feedEventMap) {
        ProductSubscription.ProductSubscriptionBuilder builder = ProductSubscription.create();
        for (Feed f : feedEventMap.keySet()) {
            CurrencyPair currencyPair = getCurrencyPair(f.getInstrument());
            if (f instanceof MarketDataFeed) {
                switch (((MarketDataFeed) f).getMarketDataType()) {
                    case TRADES:
                        builder.addTrades(currencyPair);
                        break;
                    case ORDERBOOK:
                        builder.addOrderbook(currencyPair);
                        break;
                    case TOP_OF_BOOK:
                        builder.addTicker(currencyPair);
                        break;
                    default:
                        throw new UnsupportedMDTypeForExchange(
                                "BinanceStreamingExchange does not support " + ((MarketDataFeed) f).getMarketDataType()
                        );
                }
            } else if (f instanceof ExecutionFeed) {
                switch (((ExecutionFeed) f).getExecutionType()) {
                    case EXECUTION_REPORT:
                        builder.addUserTrades(currencyPair);
                        break;
                    case ORDER_CHANGES:
                        builder.addOrders(currencyPair);
                        break;
                    default:
                        throw new UnsupportedMDTypeForExchange(
                                "BinanceStreamingExchange does not support " + ((ExecutionFeed) f).getExecutionType()
                        );
                }
            }
        }
        return builder;
    }
}
