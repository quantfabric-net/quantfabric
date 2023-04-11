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

import com.quantfabric.market.connector.xchange.XChange;
import com.quantfabric.market.connector.xchange.XChangeQuantfabricService;
import info.bitrich.xchangestream.binance.BinanceStreamingExchange;
import info.bitrich.xchangestream.core.ProductSubscription;
import io.reactivex.Completable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.dto.account.AssetDetail;
import org.knowm.xchange.utils.AuthUtils;

import java.io.IOException;
import java.util.Map;

public class BinanceStreamingXChange extends BinanceStreamingExchange implements XChange {

    enum Location {
        EU,
        COM
    }

    protected BinanceStreamingXChangeAdapter adapter;
    protected XChangeQuantfabricService quantfabricService;

    protected XChangeQuantfabricService marketDataService;
    protected XChangeQuantfabricService tradeService;

    private boolean isConnected = false;
    private Location location;
    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification spec = new ExchangeSpecification(this.getClass());
        spec.setSslUri("https://api.binance.com");
        spec.setHost("www.binance.com");
        spec.setPort(80);
        spec.setExchangeName("Binance");
        spec.setExchangeDescription("Binance Exchange.");
        AuthUtils.setApiAndSecretKey(spec, "binance");
        return spec;
    }


    @Override
    protected String getStreamingBaseUri() {
        if (Location.EU.equals(location)) {
            return Boolean.TRUE.equals(this.exchangeSpecification.getExchangeSpecificParametersItem("Use_Sandbox"))
                    ? "wss://testnet.binance.vision/" : "wss://stream.binance.us:9443/";
        } else return super.getStreamingBaseUri();
    }

    public ExchangeSpecification getDefaultUSExchangeSpecification() {
        ExchangeSpecification spec = new ExchangeSpecification(this.getClass());
        spec.setSslUri("https://api.binance.us");
        spec.setHost("www.binance.us");
        spec.setPort(80);
        spec.setExchangeName("Binance US");
        spec.setExchangeDescription("Binance US Exchange.");
        AuthUtils.setApiAndSecretKey(spec, "binanceus");
        return spec;
    }

    @Override
    protected void initServices() {
        super.initServices();
        this.adapter = (BinanceStreamingXChangeAdapter) exchangeSpecification.getParameter("adapter");
        this.quantfabricService = new BinanceStreamingMarketDataService(this, this.binance,
                this.getResilienceRegistries(), this.adapter);
        this.marketDataService = new BinanceStreamingMarketDataService(this, this.binance,
                this.getResilienceRegistries(), this.adapter);
        this.tradeService = new BinanceStreamingTradeService(this, this.binance,
                this.getResilienceRegistries(), this.adapter);
    }

    @Override
    public Completable connect(ProductSubscription... args) {
        isConnected = true;
        return super.connect(args);
    }

    @Override
    public void applySpecification(ExchangeSpecification exchangeSpecification) {
        super.applySpecification(exchangeSpecification);
        location = "Binance".equalsIgnoreCase(exchangeSpecification.getExchangeName()) ? Location.COM : Location.EU;
    }

    @Override
    public void remoteInit() {
        //TODO: ????
        BinanceStreamingMarketDataService marketDataService = (BinanceStreamingMarketDataService) this.quantfabricService;

        try {
            this.exchangeInfo = marketDataService.getExchangeInfo();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        Map<String, AssetDetail> assetDetailMap = null;
        this.postInit(assetDetailMap);
    }

    @Override
    public XChangeQuantfabricService getXChangeQuantfabricService() {
        return quantfabricService;
    }

    @Override
    public XChangeQuantfabricService getXChangeMarketDataService() {
        return marketDataService;
    }

    @Override
    public XChangeQuantfabricService getXChangeTradeService() {
        return tradeService;
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}
