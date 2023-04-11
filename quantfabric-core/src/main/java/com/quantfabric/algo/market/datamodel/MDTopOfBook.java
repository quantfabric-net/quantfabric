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
package com.quantfabric.algo.market.datamodel;

import java.util.Date;

public class MDTopOfBook extends MDItem {
    private long bidPrice;
    private double bidSize;

    private long askPrice;
    private double askSize;

    private long snapshotId;
    private String currency; //productCode

    public MDTopOfBook() {
    }

    public MDTopOfBook(long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super();
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long timestamp, long messageId, MDMessageType messageType, String sourceName,
                       long sourceTimestamp, int itemCount, int itemIndex,
                       MDItemType mdItemType, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long messageId, MDMessageType messageType, String sourceName, long sourceTimestamp,
                       int itemCount, int itemIndex, MDItemType mdItemType, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    //TODO: replace ...Size with double
    public MDTopOfBook(long timestamp, long messageId, MDMessageType messageType, String sourceName,
                       Date sourceTimestamp, int itemCount, int itemIndex,
                       MDItemType mdItemType, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long messageId, MDMessageType messageType, String sourceName, Date sourceTimestamp,
                       int itemCount, int itemIndex, MDItemType mdItemType, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long timestamp, long messageId, MDMessageType messageType, String sourceName,
                       long sourceTimestamp, int itemCount, int itemIndex,
                       MDItemType mdItemType, String mdItemId, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, mdItemId, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long messageId, MDMessageType messageType, String sourceName, long sourceTimestamp,
                       int itemCount, int itemIndex, MDItemType mdItemType, String mdItemId, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, mdItemId, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long timestamp, long messageId, MDMessageType messageType, String sourceName,
                       Date sourceTimestamp, int itemCount, int itemIndex,
                       MDItemType mdItemType, String mdItemId, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(timestamp, messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, mdItemId, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }

    public MDTopOfBook(long messageId, MDMessageType messageType, String sourceName, Date sourceTimestamp,
                       int itemCount, int itemIndex, MDItemType mdItemType, String mdItemId, String symbol, int feedId,
                       long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        super(messageId, messageType, sourceName, sourceTimestamp, itemCount, itemIndex, mdItemType, mdItemId, symbol, feedId);
        init(snapshotId, currency, bidPrice, bidSize, askPrice, askSize);
    }


    private void init(long snapshotId, String currency, long bidPrice, double bidSize, long askPrice, double askSize) {
        setSnapshotId(snapshotId);
        setCurrency(currency);
        setBidPrice(bidPrice);
        setBidSize(bidSize);
        setAskPrice(askPrice);
        setAskSize(askSize);
    }

    public long getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(long bidPrice) {
        this.bidPrice = bidPrice;
    }

    public double getBidSize() {
        return bidSize;
    }

    public void setBidSize(double bidSize) {
        this.bidSize = bidSize;
    }

    public long getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(long askPrice) {
        this.askPrice = askPrice;
    }

    public double getAskSize() {
        return askSize;
    }

    public void setAskSize(double askSize) {
        this.askSize = askSize;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
