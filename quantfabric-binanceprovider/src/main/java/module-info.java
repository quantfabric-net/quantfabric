module quantfabric.binanceprovider {
    requires java.sql;
    requires quantfabric.core;
    requires xchange.core;
    requires quantfabric.xchange;
    requires xchange.stream.binance;
    requires xchange.binance;
    requires slf4j.api;
    requires io.reactivex.rxjava2;
}