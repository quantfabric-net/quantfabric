module quantfabric.strategyengine {
    requires quantfabric.core;
    requires slf4j.api;
    requires java.xml;
    requires esper;
    requires java.desktop;
    requires java.management;
    requires esperio.socket;
    requires commons.lang;
    exports com.quantfabric.algo.trading.strategy.settings;
    exports com.quantfabric.algo.trading.strategy;
    exports com.quantfabric.algo.trading.strategyrunner;
    exports com.quantfabric.algo.trading.strategyrunner.jmx;
    exports com.quantfabric.algo.trading.strategyrunner.jmx.mbean;
    exports com.quantfabric.algo.trading.strategyrunner.jmx.notifications;
}