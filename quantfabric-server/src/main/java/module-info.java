module quantfabric.server {
    requires java.sql;
    requires jdk.management.agent;
    requires jdk.management;
    requires jdk.attach;
    requires quickserver;
    requires slf4j.api;
    requires java.xml;
    requires jnacl;
    requires java.rmi;
    requires java.logging;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires jdk.jconsole;
    requires quantfabric.core;
    requires quantfabric.strategyengine;
    requires quantfabric.marketgateway;
    requires commons.lang;
    exports com.quantfabric.algo.server.jmx;
}