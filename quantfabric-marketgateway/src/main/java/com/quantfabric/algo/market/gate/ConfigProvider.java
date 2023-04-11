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
package com.quantfabric.algo.market.gate;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.quantfabric.algo.market.gate.access.MarketDataServiceHost;
import com.quantfabric.algo.market.gate.access.MarketDataServiceHostFactory;
import com.quantfabric.algo.market.gate.access.product.ProductManager;
import com.quantfabric.algo.market.gate.access.product.SimpleProduct;
import com.quantfabric.algo.market.gate.access.product.publisher.PublishersContainer;
import com.quantfabric.algo.market.gate.access.product.publisher.ZMQPublisher;
import com.quantfabric.algo.market.gateway.InstrumentsManager;
import com.quantfabric.algo.market.gateway.MarketConnection;
import com.quantfabric.algo.market.gateway.MarketConnectionSettings;
import com.quantfabric.algo.market.gateway.MarketGatewayException;
import com.quantfabric.algo.market.gateway.feed.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.algo.backtesting.player.MDPTask;
import com.quantfabric.algo.backtesting.player.MDPTask.RepeatMode;
import com.quantfabric.algo.backtesting.player.track.FeedsTrack;
import com.quantfabric.algo.backtesting.player.track.FeedsTrack.Range;
import com.quantfabric.algo.backtesting.player.track.TrackInfo;
import com.quantfabric.algo.instrument.InstrumentImpl;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.FeedNameImpl;
import com.quantfabric.algo.market.dataprovider.MarketDataPipeline;
import com.quantfabric.algo.market.dataprovider.MarketDataPipelineBuider;
import com.quantfabric.algo.market.dataprovider.incremental.IncrementalUpdatesProducer;
import com.quantfabric.algo.market.gateway.MarketConnection.MarketConnectionMode;
import com.quantfabric.algo.market.gateway.access.product.ContentType;
import com.quantfabric.algo.market.gateway.access.product.publisher.PublisherManagersProvider;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed.MarketDataType;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.ConfigurationException;
import com.quantfabric.util.XMLConfigParser;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;


public class ConfigProvider {
    private static final Logger log = LoggerFactory.getLogger(ConfigProvider.class);

    protected static void doConfigure(MarketGatewayService configuration, InputStream stream, String resourceName) throws Exception {
        Document document = getDocument(stream, resourceName);
        doConfigure(configuration, document);
    }

    protected static Document getDocument(InputStream stream, String resourceName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        Document document = null;

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(stream);
        } catch (ParserConfigurationException ex) {
            throw new Exception("Could not get a DOM parser configuration: " + resourceName, ex);
        } catch (SAXException ex) {
            throw new Exception("Could not parse configuration: " + resourceName, ex);
        } catch (IOException ex) {
            throw new Exception("Could not read configuration: " + resourceName, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
                ConfigProvider.log.warn("could not close input stream for: " + resourceName, ioe);
            }
        }

        return document;
    }

    /**
     * Parse the W3C DOM document.
     *
     * @param configuration is the configuration object to populate
     * @param doc           to parse
     * @throws ParseException
     * @throws DOMException
     * @throws ParserConfigurationException
     */
    protected static void doConfigure(MarketGatewayService configuration, Document doc) throws DOMException, ParseException, ParserConfigurationException {
        Element root = doc.getDocumentElement();

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("instruments")) {
                handleInstruments(configuration, element);
            } else if (nodeName.equals("marketConnections")) {
                handleMarketConnections(configuration, element);
            } else if (nodeName.equals("marketDataPipelines")) {
                handleMarketDataPipelines(configuration, element);
            } else if (nodeName.equals("esper-configuration")) {
                //Create new xml document from the element
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document newDoc = docBuilder.newDocument();
                Node nd = newDoc.importNode(element, true);
                newDoc.appendChild(nd);
                // load cep configuration
                Configuration esperConfig = new Configuration().configure(newDoc);
                configuration.setCepConfig(esperConfig);
            } else if (nodeName.equals("default-persister-settings")) {
                for (PersistingUpdateListenerConfig persisterConfig :
                        XMLConfigParser.parseEsperPersisterSettings(element)) {
                    configuration.addDefaultCepPersisterConfig(persisterConfig);
                }
            } else if (nodeName.equals("publishers")) {
                handlePublishersManager(configuration, element);
            } else if (nodeName.equals("mdServiceHosts")) {
                handleMdServiceHosts(configuration, element);
            } else if (nodeName.equals("feedHandlers")) {
                handleFeedHandlers(configuration, element);
            }
        }

        log.info("Gateway configuration finised.");
    }

    private static void handleMdServiceHosts(
            MarketGatewayService configuration, Element root) {
        DOMElementIterator hostsNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (hostsNodeIterator.hasNext()) {
            Element element = hostsNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("host")) {
                handleMdServiceHost(configuration, element);
            }
        }
    }

    private static void handleMdServiceHost(MarketGatewayService configuration,
                                            Element root) {
        String hostName = root.getAttribute("name");
        if (hostName == null || hostName.isEmpty())
            throw new ConfigurationException("Attribute \"name\" doesn't specified for mdHostService");

        String factoryClassName = root.getAttribute("factory-class-name");
        if (factoryClassName == null || factoryClassName.isEmpty())
            throw new ConfigurationException("Attribute \"factory-class-name\" doesn't specified for mdHostService");

        Class<?> clazz;
        try {
            clazz = Class.forName(factoryClassName);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Can't determine class for name \"" + factoryClassName + "\"");
        }

        Object obj;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException("Can't create instance of - \"" + factoryClassName + "\"", e);
        }

        if (!(obj instanceof MarketDataServiceHostFactory))
            throw new ConfigurationException("Class must be assignable from MarketDataServiceHostFactory");

        MarketDataServiceHostFactory factory = (MarketDataServiceHostFactory) obj;

        Properties settings = XMLConfigParser.findAndParseSettingsNode(root, "host-settings");

        MarketDataServiceHost serviceHost;
        try {
            serviceHost = factory.createMarketDataServiceHost(hostName, settings);
        } catch (MarketDataServiceHostFactory.MarketDataServiceHostFactoryException e) {
            throw new ConfigurationException("Can't create MarketDataServiceHost. ", e);
        }

        Element productsNode = XMLConfigParser.findNode(root, "products");
        if (productsNode != null)
            handleProducts(productsNode, serviceHost, configuration);

        configuration.addMarketDataServiceHost(serviceHost);
    }

    private static void handleProducts(Element root,
                                       ProductManager productManager, PublisherManagersProvider publisherManagersProvider) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();

            if (nodeName.equals("product"))
                productManager.addProduct(SimpleProduct.fromXml(element, publisherManagersProvider));
        }
    }

    private static void handlePublishersManager(MarketGatewayService configuration,
                                                Element root) {
        String managerName = root.getAttribute("managerName");
        String endpointAddress = root.getAttribute("endpointAddress");

        PublishersContainer publishersContainer = new PublishersContainer(endpointAddress);

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("publisher")) {
                String publisherType = element.getAttribute("type");

                if (publisherType != null) {
                    String productCode = element.getAttribute("productCode");

                    ContentType contentType = null;
                    String contentTypeStr = element.getAttribute("contentType");
                    if (contentTypeStr != null)
                        contentType = ContentType.valueOf(contentTypeStr.trim());

                    if (publisherType.equals("zmq"))
                        publishersContainer.addPublisher(productCode, contentType, ZMQPublisher.fromXml(element, endpointAddress));
                }
            }
        }

        configuration.addPublishersManager(managerName, publishersContainer);
    }

    private static void handleInstruments(InstrumentsManager instrumentsManager, Element parentElement) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("instrument")) {
                String id = element.getAttributes().getNamedItem("id").getTextContent();
                String baseCCY = element.getAttributes().getNamedItem("base").getTextContent();
                String localCCY = element.getAttributes().getNamedItem("local").getTextContent();
                int pointsInOne = Integer.parseInt(
                        element.getAttributes().getNamedItem("pointsInOne").getTextContent());

                instrumentsManager.addInstrument(new InstrumentImpl(id, baseCCY, localCCY, pointsInOne));
            }
        }

    }

    private static void handleMarketConnections(MarketGatewayService configuration, Element parentElement) throws DOMException, ParseException {

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();

            String name = element.getAttributes().getNamedItem("name").getTextContent();
            String provider = element.getAttributes().getNamedItem("provider").getTextContent();

            MarketConnectionSettings connectionSettings = new MarketConnectionSettings();
            connectionSettings.setProvider(provider);

            Node modeNode = element.getAttributes().getNamedItem("mode");
            if (modeNode != null) {
                String strMode = modeNode.getTextContent().toUpperCase();
                connectionSettings.setMode(MarketConnectionMode.valueOf(strMode));
            }

            Node autoConnectNode = element.getAttributes().getNamedItem("autoConnect");
            if (autoConnectNode != null) {
                boolean autoConnect = Boolean.parseBoolean(autoConnectNode.getTextContent());
                connectionSettings.setAutoConnect(autoConnect);
            }

            Properties feeds = new Properties();

            DOMElementIterator nodeIterator = new DOMElementIterator(
                    element.getChildNodes());

            while (nodeIterator.hasNext()) {
                Element subElement = nodeIterator.next();
                if (subElement.getNodeName().equals("adapter-settings")) {
                    handleAdapterSettings(connectionSettings, subElement);
                } else if (subElement.getNodeName().equals("credentials")) {
                    handleCredentials(connectionSettings, subElement);
                } else if (subElement.getNodeName().equals("feeds")) {
                    handleFeeds(feeds, subElement);
                } else if (subElement.getNodeName().equals("marketDataStorageProvider")) {
                    handleMarketDataStorageProvider(connectionSettings, subElement);
                }
                if (subElement.getNodeName().equals("marketDataPlayer")) {
                    handleMarketDataPlayer(connectionSettings, subElement);
                }

            }

            MarketConnection connection = null;
            try {
                connection = configuration.createConnection(name, connectionSettings);
            } catch (MarketGatewayException e) {
                log.error("Can't create market connection", e);
            }

            if (connection != null) {
                for (Entry<Object, Object> value : feeds.entrySet()) {
                    FeedName feedName = new FeedNameImpl(value.getKey().toString());

                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> attrs = (HashMap<String, Object>) value.getValue();

                    String type = attrs.get(TYPE).toString();

                    boolean doSaveData = false;
                    String instrumentId = attrs.get(INSTRUMENT).toString();
                    if (attrs.containsKey(SAVE))
                        doSaveData = Boolean.parseBoolean(attrs.get(SAVE).toString());

                    if (type.equals(MARKET_DATA)) {
                        MarketDataFeedImpl feed =
                                new MarketDataFeedImpl(feedName, connection, instrumentId, false);
                        feed.setSaveData(doSaveData);

                        if (attrs.containsKey(MARKET_DEPTH)) {
                            String textMarketDepth = attrs.get(MARKET_DEPTH).toString();

                            int marketDepth = MarketDataFeed.DEFAULT_MARKET_DEPTH;

                            if (textMarketDepth.equals(FULL_MARKET_DEPTH))
                                marketDepth = MarketDataFeed.FULL_MARKET_DEPTH;
                            else if (textMarketDepth.equals(TOP_MARKET_DEPTH))
                                marketDepth = MarketDataFeed.TOP_MARKET_DEPTH;
                            else
                                marketDepth = Integer.parseInt(textMarketDepth);

                            feed.setMarketDepth(marketDepth);
                        }

                        if (attrs.containsKey(MARKET_DATA_TYPE)) {

                            String marketDataTypeStr = attrs.get(MARKET_DATA_TYPE).toString();

                            feed.setMarketDataType(MarketDataType.valueOf(marketDataTypeStr.toUpperCase()));
                        }

                        if (attrs.containsKey(CHANNEL)) {
                            feed.setChannel(attrs.get(CHANNEL).toString());
                        }

                        feed.setFeedGroupId((Integer) attrs.get(FEED_GROUP_ID));
                        connection.addFeed(feed);
                    }
                    if (type.equals(EXECUTION)) {
                        ExecutionFeedImpl feed = new ExecutionFeedImpl(feedName, connection, instrumentId, false);
                        if (attrs.containsKey(EXECUTION_TYPE)) {

                            String marketDataTypeStr = attrs.get(EXECUTION_TYPE).toString();

                            feed.setExecutionType(ExecutionFeed.ExecutionType.valueOf(marketDataTypeStr.toUpperCase()));
                        }
                        feed.setSaveData(doSaveData);
                        connection.addFeed(feed);

                        if (attrs.containsKey(CREDIT_LIMIT)) {
                            Object creditLimitsObj = attrs.get(CREDIT_LIMIT);
                            if (creditLimitsObj instanceof Map<?, ?>) {
                                @SuppressWarnings("unchecked")
                                Map<String, Double> creditLimits = (Map<String, Double>) creditLimitsObj;
                                for (Map.Entry<String, Double> entry : creditLimits.entrySet())
                                    connection.setCreditLimit(connection.getInstrument(entry.getKey()), entry.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleMarketDataPlayer(
            MarketConnectionSettings connectionSettings, Element parentElement) throws DOMException, ParseException {
        DOMElementIterator eventTypeNodeIterator =
                new DOMElementIterator(parentElement.getChildNodes());

        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("playlist")) {
                handleMarketDataPlayerPlaylist(connectionSettings, element);
            }
        }
    }

    private static Date parseDate(String strDate) throws ParseException {
        return DateFormat.getInstance().parse(strDate);
    }

    private static void handleMarketDataPlayerPlaylist(
            MarketConnectionSettings connectionSettings, Element parentElement) throws DOMException, ParseException {
        DOMElementIterator eventTypeNodeIterator =
                new DOMElementIterator(parentElement.getChildNodes());

        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();

            TrackInfo trackInfo = null;

            if (nodeName.equals("feedsTrack")) {
                Collection<FeedName> feedNames = new ArrayList<FeedName>();

                handleTrackFeeds(feedNames, element);

                Range range = Range.ALL;

                Node rangeFromNode = element.getAttributes().getNamedItem("range-from");
                Node rangeToNode = element.getAttributes().getNamedItem("range-to");

                if (rangeFromNode != null) {
                    range.setFrom(parseDate(rangeFromNode.getTextContent()));
                } else
                    range.setFrom(Range.MIN_BOUND);

                if (rangeToNode != null) {
                    range.setTo(parseDate(rangeFromNode.getTextContent()));
                } else
                    range.setTo(Range.MAX_BOUND);

                Node contextIdNode = element.getAttributes().getNamedItem("contextId");

                int contextId = 0;

                if (contextIdNode != null)
                    contextId = Integer.parseInt(contextIdNode.getTextContent());
                else
                    throw new ConfigurationException("contextId not specified. Attribute \"contextId\" is required for \"feedsTrack\" node");

                trackInfo = new FeedsTrack(contextId, feedNames, range);
            }

            if (trackInfo != null) {
                Node trackNumberNode = element.getAttributes().getNamedItem("trackNumber");
                if (trackNumberNode != null) {
                    int trackNumber = Integer.parseInt(trackNumberNode.getTextContent());
                    trackInfo.setTrackNumber(trackNumber);
                } else
                    throw new ConfigurationException("trackNumber not specified. Attribute \"trackNumber\" is required for Tracks node");

                MDPTask task = new MDPTask(trackInfo);

                Node repeatModeNode = element.getAttributes().getNamedItem("repeatMode");
                if (repeatModeNode != null) {
                    RepeatMode repeatMode =
                            RepeatMode.valueOf(repeatModeNode.getTextContent().toUpperCase());
                    task.setRepeatMode(repeatMode);
                }

                Node executeDelayNode = element.getAttributes().getNamedItem("executeDelay");
                if (executeDelayNode != null) {
                    boolean executeDelay = Boolean.valueOf(executeDelayNode.getTextContent().toUpperCase());
                    task.setExecuteDelay(executeDelay);
                }

                Node startTimeNode = element.getAttributes().getNamedItem("startTime");
                if (startTimeNode != null) {
                    SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
                    dateFormat.applyPattern("MM/dd/yyyy HH:mm:ss");
                    Date startTime = dateFormat.parse(startTimeNode.getTextContent());
                    task.setStartTime(startTime);
                }

                connectionSettings.getMarketDataPlayerPlaylist().add(task);
            }
        }
    }

    private static void handleTrackFeeds(Collection<FeedName> feedNames,
                                         Element parentElement) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("feed")) {
                FeedName feedName =
                        new FeedNameImpl(element.getAttributes().getNamedItem(NAME).getTextContent());

                feedNames.add(feedName);
            }
        }
    }

    private static void handleMarketDataStorageProvider(
            MarketConnectionSettings connectionSettings, Element parentElement) {
        String className =
                parentElement.getAttributes().getNamedItem("class-name").getTextContent();

        connectionSettings.setMarketDataStorageProviderClassName(className);

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("storage-settings")) {
                handleMarketDataStorageProviderSettings(connectionSettings, element);
            }
        }
    }

    private static void handleMarketDataStorageProviderSettings(
            MarketConnectionSettings connectionSettings, Element parentElement) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("setting")) {
                String name = element.getAttributes().getNamedItem(NAME).getTextContent();
                String value = element.getAttributes().getNamedItem("value").getTextContent();
                connectionSettings.addMarketDataStorageProviderSetting(name, value);
            }
        }
    }

    private static void handleAdapterSettings(MarketConnectionSettings connectionSettings, Element parentElement) {

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("setting")) {
                String name = element.getAttributes().getNamedItem(NAME).getTextContent();
                String value = element.getAttributes().getNamedItem("value").getTextContent();
                connectionSettings.getSettings().put(name, value);
            }
        }
    }

    private static void handleCredentials(MarketConnectionSettings connectionSettings, Element parentElement) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("credential")) {
                String name = element.getAttributes().getNamedItem(NAME).getTextContent();
                String value = element.getAttributes().getNamedItem("value").getTextContent();
                connectionSettings.getCredentials().put(name, value);
            }
        }
    }

    private static void handleFeeds(Properties feeds, Element parentElement) {
        DOMElementIterator marketConnectionFeedsIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (marketConnectionFeedsIterator.hasNext()) {
            Element element = marketConnectionFeedsIterator.next();
            String nodeName = element.getNodeName();

            if (nodeName.equals("feedGroup")) {
                int feedGroupId = Feed.DEFAULT_FEED_GROUP_ID;
                Node idNode = element.getAttributes().getNamedItem("id");
                if (idNode != null)
                    feedGroupId = Integer.parseInt(idNode.getTextContent());

                handleFeeds(feeds, element, feedGroupId);
            }
            if (nodeName.equals("feed")) {
                handleFeed(feeds, element, Feed.DEFAULT_FEED_GROUP_ID);
            }
        }
    }

    private static void handleFeeds(Properties feeds, Element parentElement, int feedGroupId) {
        DOMElementIterator feedGroupNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (feedGroupNodeIterator.hasNext()) {
            Element element = feedGroupNodeIterator.next();
            if (element.getNodeName().equals("feed")) {
                handleFeed(feeds, element, feedGroupId);
            }
        }
    }

    private static void handleFeed(Properties feeds, Element element, int feedGroupId) {
        if (element.getNodeName().equals("feed")) {
            String name = element.getAttributes().getNamedItem(NAME).getTextContent();

            HashMap<String, Object> attrs = new HashMap<String, Object>();

            if (feedGroupId == Feed.DEFAULT_FEED_GROUP_ID &&
                    element.hasAttribute(FEED_GROUP_ID))
                attrs.put(FEED_GROUP_ID,
                        Integer.parseInt(element.getAttributes().getNamedItem(FEED_GROUP_ID).getTextContent()));
            else
                attrs.put(FEED_GROUP_ID, feedGroupId);

            attrs.put(NAME, name);
            attrs.put(TYPE, element.getAttributes().getNamedItem(TYPE).getTextContent());

            Node instrument = element.getAttributes().getNamedItem(INSTRUMENT);
            if (instrument != null)
                attrs.put(INSTRUMENT, element.getAttributes().getNamedItem(INSTRUMENT).getTextContent());

            Node doSaveData = element.getAttributes().getNamedItem(SAVE);
            if (doSaveData != null)
                attrs.put(SAVE, doSaveData.getTextContent());

            Node marketDepth = element.getAttributes().getNamedItem(MARKET_DEPTH);
            if (marketDepth != null)
                attrs.put(MARKET_DEPTH, marketDepth.getTextContent());

            Node marketDataType = element.getAttributes().getNamedItem(MARKET_DATA_TYPE);
            if (marketDataType != null)
                attrs.put(MARKET_DATA_TYPE, marketDataType.getTextContent());

            Node executionType = element.getAttributes().getNamedItem(EXECUTION_TYPE);
            if (executionType != null)
                attrs.put(EXECUTION_TYPE, executionType.getTextContent());

            Node channel = element.getAttributes().getNamedItem(CHANNEL);
            if (channel != null)
                attrs.put(CHANNEL, channel.getTextContent());

            DOMElementIterator feedNodeIterator = new DOMElementIterator(element.getChildNodes());
            Map<String, Double> creditLimits = new HashMap<String, Double>();
            while (feedNodeIterator.hasNext()) {
                Element subElement = feedNodeIterator.next();
                if (subElement.getNodeName().equals(CREDIT_LIMIT)) {
                    handleCreditLimit(creditLimits, subElement);
                }
            }

            attrs.put(CREDIT_LIMIT, creditLimits);

            feeds.put(name, attrs);
        }
    }

    private static void handleCreditLimit(Map<String, Double> creditLimits,
                                          Element element) {
        if (element.getNodeName().equals(CREDIT_LIMIT)) {
            String instrumentId = null;
            Node instrumentNode = element.getAttributes().getNamedItem(INSTRUMENT);
            if (instrumentNode != null)
                instrumentId = instrumentNode.getTextContent();

            Double value = 0.;
            Node valueNode = element.getAttributes().getNamedItem("value");
            if (valueNode != null)
                value = Double.parseDouble(valueNode.getTextContent());

            if (instrumentId != null)
                creditLimits.put(instrumentId, value);
        }

    }

    private static void handleMarketDataPipelines(MarketGatewayService configuration, Element parentElement) {
        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
        while (eventTypeNodeIterator.hasNext()) {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("marketDataPipeline")) {
                if (element.getAttributes().getNamedItem("\"class-name\"") != null)
                    log.warn("class-name attribute is deprecated for using in pipeline definition.");

                Node pipelineBuilderClassNode = element.getAttributes().getNamedItem("pipeline-builder");

                if (pipelineBuilderClassNode != null) {
                    String pipelineBuilderClassName = pipelineBuilderClassNode.getTextContent();

                    MarketDataPipelineBuider builder = null;

                    try {
                        builder = (MarketDataPipelineBuider)
                                Class.forName(pipelineBuilderClassName).getConstructor().newInstance();

                        try {
                            MarketDataPipeline pipeline = builder.buildPipeline(element, configuration,
                                    configuration.getCepConfig(), configuration.getDefaultCepPersisterConfigs());

                            configuration.addMDPipeline(pipeline);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                } else
                    log.error("Pipeline builder (\"pipeline-builder\" attribute) doesn't specified.");
            }
        }
    }

    private static void handleFeedHandlers(MarketGatewayService configuration, Element root) {

        DOMElementIterator hostsNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (hostsNodeIterator.hasNext()) {
            Element element = hostsNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("feedHandler")) {
                handleFeedHandler(configuration, element);
            }
        }
    }

    private static void handleFeedHandler(MarketGatewayService configuration, Element root) {

        String handlerType = root.getAttribute("type");
        if (handlerType == null || handlerType.isEmpty())
            throw new ConfigurationException("Attribute \"type\" doesn't specified for feedHandler");

        String connectionName = root.getAttribute("connection");
        if (connectionName == null || connectionName.isEmpty())
            throw new ConfigurationException("Attribute \"connection\" doesn't specified for feedHandler");

        String feedName = root.getAttribute("feedName");
        if (feedName == null || feedName.isEmpty())
            throw new ConfigurationException("Attribute \"feedName\" doesn't specified for feedHandler");

        if (handlerType.equals("IncrementalUpdatesProducer")) {

            Properties settings = XMLConfigParser.findAndParseSettingsNode(root, "settings");

            configuration.addFeedHandler(IncrementalUpdatesProducer.createIncrementalUpdatesProducer(configuration, connectionName, feedName,
                    settings));
        }
    }

}
