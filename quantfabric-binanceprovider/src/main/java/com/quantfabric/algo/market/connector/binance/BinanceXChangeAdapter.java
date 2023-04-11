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

import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter;
import com.quantfabric.market.connector.xchange.XChangeMarketAdapter.CurrencyConverter.CurrencyConversionException;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.MDPrice;
import com.quantfabric.algo.market.datamodel.MDPrice.PriceType;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderType;

import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.util.*;


public class BinanceXChangeAdapter extends XChangeMarketAdapter {
	
	private Properties credentials;
	private long sourceTimestamp;

	
	public BinanceXChangeAdapter(BinanceMarketConnection connection, Properties adapterSettings, Properties credentials) {
		super(connection,new BinanceCommandFactory(), adapterSettings);
	}

	@Override
	public void marketDataHandler(Trades trades, CurrencyPair currency) throws CurrencyConversionException {

		getLogger().debug(getSourceName() + "[" + currency + "] Trades size = " + trades.getTrades().size());
		setSourceTimestamp(System.currentTimeMillis());

		synchronized (this) {
			for (MDEvent event : collectEvents(trades, currency)) {
				try {
					publish(event);
				} catch (PublisherException e) {
					getLogger().error("Can't publish MDEvent", e);
				}

			}
		}
	}
	@Override
	public void marketDataHandler(OrderBook orderBook, CurrencyPair currency) throws CurrencyConversionException {

		getLogger().debug(getSourceName() + "[" + currency + "] BidBook size = " + orderBook.getBids().size());
		getLogger().debug(getSourceName() + "[" + currency + "] OfferBook size = "  + orderBook.getAsks().size());

		setSourceTimestamp(System.currentTimeMillis());

		synchronized (this) {
			for (MDEvent event : collectEvents(orderBook, currency)) {
				try {
					publish(event);
				} catch (PublisherException e) {
					getLogger().error("Can't publish MDEvent", e);
				}
			}
		}
	}
	@Override
	public String getVenueName() {
		return "BINANCE";
	}
	@Override
	public String sendMessage(LimitOrder order, TradeOrder tradeOrder) throws Exception {
		String orderId = null;

		try {
			orderId = this.connection.getExchange().getTradeService().placeLimitOrder(order);
		} catch (ExchangeException e) {
			getLogger().error("Rejecting order: [ {} ]. Reason: {} ", tradeOrder.getOrderReference(), e.getMessage());
			addNewOrder(e.getMessage(), tradeOrder, true);
		}

		return orderId;
	}

	@Override
	public <T> void marketDataHandler(T event) {
		if(event instanceof MDEvent) {
			try {
				publish(event);
			} catch (PublisherException e) {
				getLogger().error("Can't publish MDEvent", e);
			}
		}
	}

	private synchronized Collection<MDEvent> collectEvents(OrderBook orderBook, CurrencyPair currency) throws CurrencyConversionException {
		
		ArrayList<MDEvent> events = new ArrayList<>();
		String symbol = CurrencyConverter.toSymbol(currency);
		MarketDataFeed feed = getFeedProvider().getMarketDataFeed(symbol);
		Date timestamp = new Date(getSourceTimestamp());
		long messageId = orderBook.getTimeStamp().getTime();

		int itemCount = orderBook.getBids().size() + orderBook.getAsks().size();
		
		int count = 0;
		
		String priceId = "";

		for (LimitOrder bidOrder : orderBook.getBids()) {
			long bidPrice = feed.getInstrument().castToLong(bidOrder.getLimitPrice().doubleValue());
			double bidVolume = bidOrder.getOriginalAmount().doubleValue();
			priceId = String.valueOf(count);
			events.add(createMDPrice(messageId, timestamp, itemCount, count++,
					MDItemType.BID, priceId, symbol, feed.getFeedId(), bidPrice, bidVolume));
			
		}
		
		for (LimitOrder askOrder : orderBook.getAsks()) {
			long askPrice = feed.getInstrument().castToLong(askOrder.getLimitPrice().doubleValue());
			double askVolume = askOrder.getOriginalAmount().doubleValue();
			priceId = String.valueOf(count);
			events.add(createMDPrice(messageId, timestamp, itemCount, count++,
					MDItemType.OFFER, priceId, symbol, feed.getFeedId(), askPrice, askVolume));
		}
		
		return events;
	}

	private ArrayList<MDEvent> collectEvents(Trades trades, CurrencyPair currency) throws CurrencyConversionException {
		String symbol = CurrencyConverter.toSymbol(currency);
		MarketDataFeed feed = getFeedProvider().getMarketDataFeed(symbol);
		Date timestamp = new Date(getSourceTimestamp());
		long messageId;
		int itemCount = trades.getTrades().size();
		int count = 0;
		String priceId = "";
		ArrayList<MDEvent> events = new ArrayList<>();
		for(Trade trade : trades.getTrades()) {
			messageId = trade.getTimestamp().getTime();
			switch (trade.getType()) {
				case EXIT_ASK:
				case ASK:
					long askPrice = feed.getInstrument().castToLong(trade.getPrice().doubleValue());
					double askVolume = trade.getOriginalAmount().doubleValue();
					priceId = String.valueOf(count);
					events.add(createMDPrice(messageId, timestamp, itemCount, count++,
							MDItemType.OFFER, priceId, symbol, feed.getFeedId(), askPrice, askVolume));
					break;
				case EXIT_BID:
				case BID:
					long bidPrice = feed.getInstrument().castToLong(trade.getPrice().doubleValue());
					double bidVolume = trade.getOriginalAmount().doubleValue();
					priceId = String.valueOf(count);
					events.add(createMDPrice(messageId, timestamp, itemCount, count++,
							MDItemType.BID, priceId, symbol, feed.getFeedId(), bidPrice, bidVolume));
					break;
			}
		}
		return events;
	}


	protected  MDPrice createMDPrice(long messageId, Date sourceTimestamp, int itemCount, int itemIndex,
			MDItemType mdItemType, String priceId, String symbol, int feedId, long price, double size)
	{
		return new MDPrice(messageId,
				MDMessageType.SNAPSHOT,
				getSourceName(),
				sourceTimestamp,
				itemCount,
				itemIndex,
				mdItemType,
				priceId,
				symbol,
				feedId,
				price,
				size,
				PriceType.DEALABLE,
				true,
				MDPrice.DEFAULT_AMOUNT_ORDERS);
	}

	protected long getSourceTimestamp() {
		return sourceTimestamp;
	}


	public void setSourceTimestamp(long sourceTimestamp) {
		this.sourceTimestamp = sourceTimestamp;
	}

	@Override
	public void unsubscribeMarketData(String symbol) {
		//TODO: finish .getExecutionFeed by symbol
		unsubscribeMarketData(getFeedProvider().getMarketDataFeed(symbol));
		//unsubscribeExecution(getFeedProvider().getExecutionFeed(symbol));
	}

	public void unsubscribeMarketData(MarketDataFeed feed) {
		getTimer().cancel();
	}

	@Override
	public void unsubscribeExecution(ExecutionFeed feed) {
		getTimer().cancel();
	}


	@SuppressWarnings("unused")
	private OrderType toOrderType(org.knowm.xchange.binance.dto.trade.OrderType binanceOrderType) throws Exception {
		if(binanceOrderType.equals(org.knowm.xchange.binance.dto.trade.OrderType.LIMIT))
			return OrderType.BTC_EXCHANGE_LIMIT;
		throw new Exception("Can't convert BinanceOrderType - " + binanceOrderType);
	}
	@SuppressWarnings("unused")
	private org.knowm.xchange.binance.dto.trade.OrderType toBinanceOrderType(OrderType orderType) throws Exception {
		switch (orderType) {
			case BTC_EXCHANGE_LIMIT:
				return org.knowm.xchange.binance.dto.trade.OrderType.LIMIT;
			case MARKET:
				return org.knowm.xchange.binance.dto.trade.OrderType.MARKET;
			case TAKE_PROFIT_LIMIT:
				return org.knowm.xchange.binance.dto.trade.OrderType.TAKE_PROFIT_LIMIT;
			case STOP_LOSS:
				return org.knowm.xchange.binance.dto.trade.OrderType.STOP_LOSS;
			case STOP_LOSS_LIMIT:
				return org.knowm.xchange.binance.dto.trade.OrderType.STOP_LOSS_LIMIT;
			case TAKE_PROFIT:
				return org.knowm.xchange.binance.dto.trade.OrderType.TAKE_PROFIT;
			case LIMIT_MAKER:
				return org.knowm.xchange.binance.dto.trade.OrderType.LIMIT_MAKER;
			default:
				throw new Exception("Can't convert OrderType - " + orderType);
		}
	}
}
