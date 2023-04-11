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


@Deprecated
public class TradeOHLCValue extends OHLCValue {

    /**
     *
     */
    private static final long serialVersionUID = 6071811580830123119L;

    private int tradeCount;
    private int buyCount;
    private int sellCount;
    private double buySellRatio;
    private long cumulativeBuyPrice;
    private long cumulativeSellPrice;
    private long avgBuy;
    private long avgSell;


    public TradeOHLCValue() {

        super();
    }

    public TradeOHLCValue(int timeFrameInSeconds) {

        super(timeFrameInSeconds);

    }

    public TradeOHLCValue(int timeFrameInSeconds, long open, long openSourceTimestamp, long high,
						  long highSourceTimestamp, long low, long lowSourceTimestamp,
                          long close, long closeSourceTimestamp, boolean closed, boolean justOpened) {

        super(timeFrameInSeconds, open, openSourceTimestamp, high, highSourceTimestamp, low, lowSourceTimestamp,
				close, closeSourceTimestamp, closed, justOpened);
    }

    protected TradeOHLCValue(long barId, int timeFrameInSeconds, long open, long openSourceTimestamp, long high,
							 long highSourceTimestamp, long low, long lowSourceTimestamp, long close,
							 long closeSourceTimestamp, long typical, long barSize, boolean closed, long closeTimestamp,
                             boolean closeByTimeout, boolean justOpened) {

        super(barId, timeFrameInSeconds, open, openSourceTimestamp, high, highSourceTimestamp, low,
                lowSourceTimestamp, close, closeSourceTimestamp, typical, barSize, closed, closeTimestamp,
                closeByTimeout, justOpened);
    }

    private TradeOHLCValue(int tradeCount, int buyCount, int sellCount, double buySellRatio, long avgBuy, long avgSell,
                           long barId, int timeFrameInSeconds, long open, long openSourceTimestamp, long high,
                           long highSourceTimestamp, long low, long lowSourceTimestamp, long close, long closeSourceTimestamp,
                           long typical, long barSize, boolean closed, long closeTimestamp, boolean closeByTimeout,
                           boolean justOpened) {

        super(barId, timeFrameInSeconds, open, openSourceTimestamp, high, highSourceTimestamp, low,
                lowSourceTimestamp, close, closeSourceTimestamp, typical, barSize, closed, closeTimestamp,
                closeByTimeout, justOpened);

        this.tradeCount = tradeCount;
        this.buyCount = buyCount;
        this.sellCount = sellCount;
        this.buySellRatio = buySellRatio;
        this.avgBuy = avgBuy;
        this.avgSell = avgSell;
    }

    public void update(long timestamp, int price, MDTrade trade) throws Exception {

        update(timestamp, price);

        switch (trade.getTradeSide()) {
            case BUY:
                buyCount++;
                cumulativeBuyPrice += trade.getPrice();
                break;
            case SELL:
                sellCount++;
                cumulativeSellPrice += trade.getPrice();
                break;
            case NA:
                break;
            default:
                break;
        }

        tradeCount = buyCount + sellCount;

        if (tradeCount != 0)
            buySellRatio = (buyCount - sellCount) * 100 / tradeCount;
        else
            buySellRatio = 0;

        if (cumulativeBuyPrice != 0)
            avgBuy = (int) (cumulativeBuyPrice / buyCount);
        else
            avgBuy = 0;

        if (cumulativeSellPrice != 0)
            avgSell = (int) (cumulativeSellPrice / sellCount);
        else
            avgSell = 0;

    }


    public int getTradeCount() {
        return tradeCount;
    }


    public int getBuyCount() {
        return buyCount;
    }


    public int getSellCount() {
        return sellCount;
    }


    public double getBuySellRatio() {
        return buySellRatio;
    }


    public long getAvgBuy() {
        return avgBuy;
    }


    public long getAvgSell() {
        return avgSell;
    }

    @Override
    public TradeOHLCValue clone() {

        return new TradeOHLCValue(tradeCount, buyCount, sellCount, buySellRatio, avgBuy, avgSell, getBarId(),
				getTimeFrameInSeconds(), getOpen(), getOpenSourceTimestamp(), getHigh(),
                getHighSourceTimestamp(), getLow(), getLowSourceTimestamp(), getClose(),
				getCloseSourceTimestamp(), getTypical(), getBarSize(), isClosed(),
				getCloseTimestamp(), isCloseByTimeout(), isJustOpened());
    }
}
