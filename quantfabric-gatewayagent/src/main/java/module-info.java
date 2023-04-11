module quantfabric.gatewayagent {
    requires java.rmi;
    requires quantfabric.core;
    requires quantfabric.marketgateway;
    exports com.quantfabric.algo.market.gateway.access.remote;
}