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
package com.quantfabric.algo.market.provider.aggregator;

import java.util.List;
import java.util.Properties;

import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.OWAP;
import com.quantfabric.algo.market.datamodel.OWAP.OWAPSides;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWAPAggregator extends BaseMarketViewAggregator {
    private static final Logger log = LoggerFactory.getLogger(OWAPAggregator.class);
    private final OWAP currentBidOWAP = new OWAP();
    private final OWAP currentOfferOWAP = new OWAP();

    private OWAP snapshotBidOWAP = null;
    private OWAP snapshotOfferOWAP = null;

    private boolean bidUpdated = false;
    private boolean offerUpdated = false;

    private final boolean bidIsPrePopulated = false;
    private final boolean offerIsPrePopulated = false;

    public OWAPAggregator(String identifier, Properties properties) {
        super(identifier, properties);
        currentBidOWAP.setSide(OWAPSides.BID);
        currentOfferOWAP.setSide(OWAPSides.OFFER);
    }

    public OWAP getBidOWAP() {
        return snapshotBidOWAP;
    }

    public OWAP getOfferOWAP() {
        return snapshotOfferOWAP;
    }

    private void createBidOWAPSnapshot(long snapshotId) {
        try {
            snapshotBidOWAP = (OWAP) currentBidOWAP.clone();
            snapshotBidOWAP.setSnapshotId(snapshotId);
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage());
        }
    }

    private void createOfferOWAPSnapshot(long snapshotId) {
        try {
            snapshotOfferOWAP = (OWAP) currentOfferOWAP.clone();
            snapshotOfferOWAP.setSnapshotId(snapshotId);
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
            throws OrderBookSnapshotListenerException {
        OrderBookView orderBook = orderBookSnapshot.getOrderBookView();

        List<MDPrice> prices = orderBook.getAllLevels();

        if (orderBook.getOrderBookType() == OrderBookTypes.BID_BOOK) {
            if (!bidIsPrePopulated && !prices.isEmpty()) {
                currentBidOWAP.pupulate(prices.get(0));
            }
            OwapCalcResult result = calcOWAP(prices);
            currentBidOWAP.setPrice(result.getPrice());
            currentBidOWAP.setAmountOrders(result.getAmountOrders());
        } else if (orderBook.getOrderBookType() == OrderBookTypes.OFFER_BOOK) {
            if (!offerIsPrePopulated && !prices.isEmpty()) {
                currentOfferOWAP.pupulate(prices.get(0));
            }
            OwapCalcResult result = calcOWAP(prices);
            currentOfferOWAP.setPrice(result.getPrice());
            currentOfferOWAP.setAmountOrders(result.getAmountOrders());
        }
    }

    @Override
    public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
                                 boolean isBookModified) {
        if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK) {
            bidUpdated = true;
            createBidOWAPSnapshot(updateId);
        } else if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK) {
            offerUpdated = true;
            createOfferOWAPSnapshot(updateId);
        }

        if (bidUpdated && offerUpdated) {
            bidUpdated = false;
            offerUpdated = false;
            publish(new Object[]{getBidOWAP(), getOfferOWAP()});
        }
    }

    private static class OwapCalcResult {
        private final int price;
        private final int amountOrders;

        public OwapCalcResult(int price, int amountOrders) {
            super();
            this.price = price;
            this.amountOrders = amountOrders;
        }

        public int getAmountOrders() {
            return amountOrders;
        }

        public int getPrice() {
            return price;
        }
    }

    private OwapCalcResult calcOWAP(List<MDPrice> prices) {
        int multipPriceAmountOrdersSum = 0;
        int amountOrdersSum = 0;
        for (MDPrice price : prices) {
            multipPriceAmountOrdersSum += price.getPrice() * price.getAmountOrders();
            amountOrdersSum += price.getAmountOrders();

            //boundary for calculation
            if (amountOrdersSum >= 5)
                break;
        }
        if (amountOrdersSum != 0)
            return new OwapCalcResult(multipPriceAmountOrdersSum / amountOrdersSum, amountOrdersSum);
        else
            return new OwapCalcResult(0, 0);
    }

    @Override
    public void processNoUpdate(long snapshotId) {

    }
}
