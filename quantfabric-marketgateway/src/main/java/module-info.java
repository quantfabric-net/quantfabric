module quantfabric.marketgateway {
    exports com.quantfabric.algo.market.gate;
    exports com.quantfabric.algo.market.gate.jmx.mbean;
    exports com.quantfabric.algo.market.gate.access.product.subscriber;
    exports com.quantfabric.algo.market.gate.access.product.producer;
    requires quantfabric.core;
    requires java.sql;
    requires slf4j.api;
    requires esper;
    requires java.xml;
    requires java.rmi;
    requires java.management;
    requires quickfixj.all;
    requires quantfabric.indicators;
    requires xchange.core;
    requires com.fasterxml.jackson.databind;
}