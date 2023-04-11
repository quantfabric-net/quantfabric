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
import com.quantfabric.algo.market.datamodel.VWAP;
import com.quantfabric.algo.market.datamodel.VWAP.VWAPSides;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookInfo.OrderBookTypes;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookSnapshot;
import com.quantfabric.algo.market.dataprovider.orderbook.OrderBookView;
import com.quantfabric.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VWAPAggregator extends BaseMarketViewAggregator {
    private static final Logger log = LoggerFactory.getLogger(VWAPAggregator.class);
    private final VWAP currentBidVWAP = new VWAP();
    private final VWAP currentOfferVWAP = new VWAP();

    private VWAP snapshotBidVWAP = null;
    private VWAP snapshotOfferVWAP = null;

    private boolean bidUpdated = false;
    private boolean offerUpdated = false;

    private final boolean bidIsPrePopulated = false;
    private final boolean offerIsPrePopulated = false;

    public VWAPAggregator(String identifier, Properties properties) {
        super(identifier, properties);
        currentBidVWAP.setSide(VWAPSides.BID);
        currentOfferVWAP.setSide(VWAPSides.OFFER);
    }

    public VWAP getBidVWAP() {
        return snapshotBidVWAP;
    }

    public VWAP getOfferVWAP() {
        return snapshotOfferVWAP;
    }

    private void createBidVWAPSnapshot(long snapshotId) {
        try {
            snapshotBidVWAP = (VWAP) currentBidVWAP.clone();
            snapshotBidVWAP.setSnapshotId(snapshotId);
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage());
        }
    }

    private void createOfferVWAPSnapshot(long snapshotId) {
        try {
            snapshotOfferVWAP = (VWAP) currentOfferVWAP.clone();
            snapshotOfferVWAP.setSnapshotId(snapshotId);
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void processNewSnapshot(OrderBookSnapshot orderBookSnapshot)
            throws OrderBookSnapshotListenerException {
        OrderBookView orderBookView = orderBookSnapshot.getOrderBookView();

        OrderBookView orderBook = orderBookView.clone();

        List<MDPrice> prices = orderBook.getAllLevels();

        if (getProperties().containsKey("depth")) {
            int depth = Integer.parseInt(getProperties().getProperty("depth"));
            CollectionHelper.shrinkList(prices, depth);
            CollectionHelper.shrinkList(prices, depth);
            currentBidVWAP.setDepth(depth);
            currentOfferVWAP.setDepth(depth);
        }

        if (orderBook.getOrderBookType() == OrderBookTypes.BID_BOOK) {
            if (!bidIsPrePopulated && !prices.isEmpty()) {
                currentBidVWAP.pupulate(prices.get(0));
            }
            VwapCalcResult result = calcVWAP(prices);
            currentBidVWAP.setPrice(result.getPrice());
            currentBidVWAP.setSize(result.getSize());
        } else if (orderBook.getOrderBookType() == OrderBookTypes.OFFER_BOOK) {
            if (!offerIsPrePopulated && !prices.isEmpty()) {
                currentOfferVWAP.pupulate(prices.get(0));
            }
            VwapCalcResult result = calcVWAP(prices);
            currentOfferVWAP.setPrice(result.getPrice());
            currentOfferVWAP.setSize(result.getSize());
        }
    }

    @Override
    public void processEndUpdate(OrderBookInfo orderBookInfo, long updateId,
                                 boolean isBookModified) {
        if (orderBookInfo.getOrderBookType() == OrderBookTypes.BID_BOOK) {
            bidUpdated = true;
            createBidVWAPSnapshot(updateId);
        } else if (orderBookInfo.getOrderBookType() == OrderBookTypes.OFFER_BOOK) {
            offerUpdated = true;
            createOfferVWAPSnapshot(updateId);
        }

        if (bidUpdated && offerUpdated) {
            bidUpdated = false;
            offerUpdated = false;
            publish(new Object[]{getBidVWAP(), getOfferVWAP()});
        }
    }

    private static class VwapCalcResult {
        private final int price;
        private final double size;

        public VwapCalcResult(int price, double size) {
            super();
            this.price = price;
            this.size = size;
        }

        public int getPrice() {
            return price;
        }

        public double getSize() {
            return size;
        }
    }

    private VwapCalcResult calcVWAP(List<MDPrice> prices) {
        long multipPriceSizeSum = 0;
        double sizesSum = 0.;
        for (MDPrice price : prices) {
            multipPriceSizeSum += price.getPrice() * price.getSize();
            sizesSum += price.getSize();
        }

        if (sizesSum != 0)
            return new VwapCalcResult((int) (multipPriceSizeSum / sizesSum), sizesSum);
        else
            return new VwapCalcResult(0, 0);
    }

    @Override
    public void processNoUpdate(long snapshotId) {
    }
}
