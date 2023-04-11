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

import com.quantfabric.algo.market.datamodel.OWAP.OWAPSides;
import com.quantfabric.algo.market.datamodel.VWAP.VWAPSides;

public class ComplexMarketView extends BaseMDFeedEvent {
    /**
     *
     */
    private static final long serialVersionUID = 8924162369810992616L;
    private long snapshotId;
    private MDDealableQuote topQuote;
    private VWAP bidVWAP;
    private VWAP offerVWAP;
    private OWAP bidOWAP;
    private OWAP offerOWAP;

    private long midVWAPPrice;
    private long midOWAPPrice;
    private long midTopPrice;

    public ComplexMarketView() {
        super();
    }

    public ComplexMarketView(long snapshotId) {
        super();
        this.snapshotId = snapshotId;
    }

    public ComplexMarketView(MDFeedEvent event, long snapshotId) {
        super(event);
        this.snapshotId = snapshotId;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public MDDealableQuote getTopQuote() {
        return topQuote;
    }

    public void setTopQuote(MDDealableQuote quote) {
        this.topQuote = quote;
    }

    public VWAP getBidVWAP() {
        return bidVWAP;
    }

    public void setBidVWAP(VWAP vwap) {
        this.bidVWAP = vwap;
    }

    public VWAP getOfferVWAP() {
        return offerVWAP;
    }

    public void setOfferVWAP(VWAP vwap) {
        this.offerVWAP = vwap;
    }

    public OWAP getBidOWAP() {
        return bidOWAP;
    }

    public void setBidOWAP(OWAP bidOWAP) {
        this.bidOWAP = bidOWAP;
    }

    public OWAP getOfferOWAP() {
        return offerOWAP;
    }

    public void setOfferOWAP(OWAP offerOWAP) {
        this.offerOWAP = offerOWAP;
    }

    public long getMidVWAPPrice() {
        return midVWAPPrice;
    }

    public void setMidVWAPPrice(long midVWAPPrice) {
        this.midVWAPPrice = midVWAPPrice;
    }

    public long getMidOWAPPrice() {
        return midOWAPPrice;
    }

    public void setMidOWAPPrice(long midOWAPPrice) {
        this.midOWAPPrice = midOWAPPrice;
    }

    public long getMidTopPrice() {
        return midTopPrice;
    }

    public void setMidTopPrice(long midTopQuotePrice) {
        this.midTopPrice = midTopQuotePrice;
    }

    private long calcMid(long bidPrice, long offerPrice) {
        return (bidPrice + offerPrice) / 2;
    }

    public void update(Object event) {
        if (event instanceof MDDealableQuote) {
            this.setTopQuote((MDDealableQuote) event);
            this.setMidTopPrice(calcMid(getTopQuote().getBidPrice(), getTopQuote().getOfferPrice()));
        } else if (event instanceof VWAP) {
            VWAP vwap = (VWAP) event;

            if (vwap.getSide() == VWAPSides.BID)
                this.setBidVWAP(vwap);
            else
                this.setOfferVWAP(vwap);

            if (getBidVWAP() != null && getOfferVWAP() != null)
                this.setMidVWAPPrice(calcMid(getBidVWAP().getPrice(), getOfferVWAP().getPrice()));
            else
                this.setMidVWAPPrice(0);
        } else if (event instanceof OWAP) {
            OWAP owap = (OWAP) event;

            if (owap.getSide() == OWAPSides.BID)
                this.setBidOWAP(owap);
            else
                this.setOfferOWAP(owap);

            if (getBidOWAP() != null && getOfferOWAP() != null)
                this.setMidOWAPPrice(calcMid(getBidOWAP().getPrice(), getOfferOWAP().getPrice()));
            else
                this.setMidOWAPPrice(0);
        }
    }
}
