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
package com.espertech.esper.example.ohlc;

import java.util.Date;

public class OHLCTick
{
    private final String ticker;
    private final double price;
    private final long timestamp;

    public OHLCTick(String ticker, double price, long timestamp)
    {
        this.ticker = ticker;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getTicker()
    {
        return ticker;
    }

    public double getPrice()
    {
        return price;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String toString()
    {
        return "ticker " + ticker +
               " price " + price +
               " timestamp " + printTime(timestamp);
    }

    private String printTime(long timestamp)
    {
        return new Date(timestamp).toString();
    }
}
