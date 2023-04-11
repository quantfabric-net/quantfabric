module quantfabric.xchange {
    exports com.quantfabric.market.connector.xchange;
    exports com.quantfabric.market.connector.xchange.commands;
    requires java.sql;
    requires quantfabric.core;
    requires xchange.core;
    requires slf4j.api;
    requires io.reactivex.rxjava2;
    requires xchange.stream.core;
    requires com.fasterxml.jackson.databind;
}