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
package com.quantfabric.algo.trading.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import com.quantfabric.algo.trading.strategy.exceptions.QuantfabricStrategyRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPStatement;
import com.quantfabric.algo.market.datamodel.ComplexMarketView;
import com.quantfabric.algo.market.datamodel.MDOrderBook;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.PeggedSettings;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.OrderType;
import com.quantfabric.algo.order.TradeOrder.StopSides;
import com.quantfabric.algo.order.TradeOrder.TimeInForceMode;
import com.quantfabric.algo.trading.Messages;
import com.quantfabric.algo.trading.execution.ExecutionProvider;
import com.quantfabric.algo.trading.execution.commands.CancelStrategyOrderCommand;
import com.quantfabric.algo.trading.execution.commands.ReplaceStrategyOrderCommand;
import com.quantfabric.algo.trading.strategy.events.StrategyEvent;
import com.quantfabric.algo.trading.strategy.events.StrategyInfoChangedEvent;
import com.quantfabric.algo.trading.strategy.events.StrategySettingChangedEvent;
import com.quantfabric.algo.trading.strategy.events.StrategyStateChangedEvent;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting.ModificationMode;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting.Scope;
import com.quantfabric.algo.trading.strategy.settings.StrategySettingImpl;
import com.quantfabric.algo.trading.strategy.settings.viewlayout.LayoutDefinitionProvider;
import com.quantfabric.algo.trading.strategyrunner.StrategyRunner;
import com.quantfabric.cep.CEPVariable;
import com.quantfabric.cep.ICEPProvider;
import com.quantfabric.cep.QuantfabricCEPException;
import com.quantfabric.util.Converter;
import com.quantfabric.util.VariableWrapper;

public class BaseTradingStrategy implements TradingStrategy,
        StrategyConfiguration {

    private static final Map<String, BaseTradingStrategy> strategies =
            new HashMap<String, BaseTradingStrategy>();

    //private MarketUpdatesDataStreamPoster marketUpdatesDataStreamPoster;

    public static BaseTradingStrategy getStrategy(String strategyId) {
        return strategies.get(strategyId);
    }

    /******************************************************************
     *               FIELDS
     ******************************************************************/
    private String name;
    private String type;
    private String description;
    private final String id;

    private final Map<String, StrategySettingImpl> settings = new LinkedHashMap<String, StrategySettingImpl>();
    private LayoutDefinitionProvider layoutDefinitionProvider = null;

    private final Set<ExecutionPoint> execEndPoints = new HashSet<ExecutionPoint>();
    private final Set<DataSink> dataSinks = new HashSet<DataSink>();
    private final Map<StrategyEpStatement, EPStatement> stmts = new LinkedHashMap<StrategyEpStatement, EPStatement>();
    private final ICEPProvider cep;
    private final ExecutionProvider executor;
    private final StrategyRunner runtime;
    private boolean isRunning;
    private boolean isPlugged;
    private boolean isEnabled = DEFAULT_ENABLE;
    private final VariableWrapper<Boolean> isExecutionAllowed;

    private final static Logger log = LoggerFactory.getLogger(BaseTradingStrategy.class);

    /******************************************************************
     *               PROPERTIES
     * @return
     *******************************************************************/
    protected EPStatement getRegisteredEpStatement(StrategyEpStatement defenition) {
        return stmts.get(defenition);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "setName"));
        this.name = name;

        signalStrategyInfoChanged();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "setType"));
        this.type = type;

        signalStrategyInfoChanged();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String desc) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "setDescription"));
        this.description = desc;

        signalStrategyInfoChanged();
    }

    @Override
    public boolean isExecutionAllowed() {
        return isExecutionAllowed.getValue();
    }

    @Override
    public void setExecutionAllowed(boolean isExecutionAllowed) {
        this.isExecutionAllowed.setValue(isExecutionAllowed);
        signalStrategyStateChanged();
    }

    protected Map<String, StrategySettingImpl> getAllSettings() {
        return Collections.unmodifiableMap(settings);
    }

    @Override
    public Map<String, StrategySetting> getSettings() {
        Map<String, StrategySetting> publicSettings = new LinkedHashMap<String, StrategySetting>();
        for (StrategySetting strategySetting : settings.values())
            if (strategySetting.getScope() == Scope.PUBLIC)
                publicSettings.put(strategySetting.getName(), strategySetting);

        return Collections.unmodifiableMap(publicSettings);
    }

    @Override
    public Map<String, String> getSettingValues() {
        Map<String, String> settingValues = new LinkedHashMap<String, String>();

        for (StrategySetting strategySetting : getSettings().values())
            settingValues.put(strategySetting.getName(), strategySetting.getValue());

        return settingValues;
    }

    @Override
    public void setSettingValue(String name, String value) {
        if (!settings.containsKey(name))
            throw new QuantfabricStrategyRuntimeException("Unknown setting : " + name);

        StrategySettingImpl strategySetting = settings.get(name);

        strategySetting.setValue(value);

        try {
            if (cep.isExistVariable(name)) {
                Object prevValue = cep.getVariableValue(name);

                Class<?> settingType = Class.forName(strategySetting.getType());

                if (Boolean.class == settingType)
                    cep.setVariableValue(name, Boolean.parseBoolean(value));
                else if (Integer.class == settingType)
                    cep.setVariableValue(name, Integer.parseInt(value));
                else if (Long.class == settingType)
                    cep.setVariableValue(name, Long.parseLong(value));
                else if (Double.class == settingType)
                    cep.setVariableValue(name, Double.parseDouble(value));
                else if (Short.class == settingType)
                    cep.setVariableValue(name, Short.parseShort(value));
                else if (Byte.class == settingType)
                    cep.setVariableValue(name, Byte.parseByte(value));
                else
                    cep.setVariableValue(name, value);


                sendUpdate(new StrategySettingChangedEvent(
                        this.name, id, name, strategySetting.getType(), prevValue, cep.getVariableValue(name)));
            } else
                throw new QuantfabricStrategyRuntimeException("Unknown variable : " + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<ExecutionPoint> getExecutionEndPoints() {
        return Collections.unmodifiableSet(execEndPoints);
    }

    @Override
    public Set<DataSink> getDataSinks() {
        return Collections.unmodifiableSet(dataSinks);
    }

    /**
     * @return the stmts
     */
    @Override
    public Set<StrategyEpStatement> getStrategyStatements() {
        return stmts.keySet();
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public synchronized boolean isPlugged() {
        return isPlugged;
    }

    /**
     * @return the isEnabled
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * @param isEnabled the isEnabled to set
     */
    @Override
    public void setEnabled(boolean isEnabled) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "setEnabled"));
        this.isEnabled = isEnabled;
        signalStrategyStateChanged();
    }

    @Override
    public ExecutionProvider getExecutionProvider() {
        return executor;
    }

    /******************************************************************
     *               CTORs
     *******************************************************************/
    public BaseTradingStrategy(String name, StrategyRunner runtime) {
        this(name, runtime, new Configuration());
    }

    public BaseTradingStrategy(String name, StrategyRunner runtime, Configuration cepConfig) {
        this.id = name + " - " + UUID.randomUUID();

        this.name = name;
        this.runtime = runtime;
        executor = runtime.getExecutionProvider(this);

        cepConfig.addEventTypeAutoName("com.quantfabric.algo.trading.strategy.events");
        cepConfig.addEventTypeAutoName("com.quantfabric.algo.order");
        cepConfig.addEventTypeAutoName("com.quantfabric.algo.trading.execution");

        cep = runtime.getCEPProvider(name, "STR_" + name, cepConfig);
        cep.addVariable("StrategyId", "java.lang.String", this.id, true);

        isExecutionAllowed = new CEPVariable<Boolean>(cep, "isExecutionAllowed", Boolean.class.getName(), true);

        init();

        strategies.put(this.getId(), this);
    }

    public String getId() {
        return id;
    }

    protected ICEPProvider getCep() {
        return cep;
    }

    @Override
    public void destroyCep() {
        cep.destroy();
    }

    /*private void createExecutor() {
        int threadCore = Runtime.getRuntime().availableProcessors();
        poolExecutor = new ThreadPoolExecutor(
                threadCore,
                Runtime.getRuntime().availableProcessors() * threadCore,
                10, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(true),//queue,
                new ThreadFactory() {
                    long count = 0;

                    public Thread newThread(Runnable r) {
                        log.info("Create strategy thread " + (count + 1));
                        return new Thread(r, "EsperServer-" + count++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        super.rejectedExecution(r, e);
                    }
                }
        );
        poolExecutor.prestartAllCoreThreads();
    }*/
    private final void init() {
        loadEmbeddedDataSinks();
        loadEmbeddedExecutionPoint();
        loadEmbeddedEventProcess();
    }

    protected void loadEmbeddedExecutionPoint() {
    }

    protected void loadEmbeddedDataSinks() {
    }

    protected void loadEmbeddedEventProcess() {
    }

    /******************************************************************
     *               RUNTIME
     *******************************************************************/
    @Override
    public synchronized void start() throws Exception {
        if (isEnabled && !isRunning) {
            boolean prevIsExecutionAllowed = isExecutionAllowed.getValue();
            isExecutionAllowed.setValue(false);

            for (EPStatement stmt : stmts.values())
                if (stmt.isStopped()) {
                    stmt.start();
                }

            executor.start();
            activateDataSinks();
            isRunning = true;
            signalStrategyStateChanged();
            log.info("Strategy (" + this.getName() + ") started");

            isExecutionAllowed.setValue(prevIsExecutionAllowed);
        } else throw new IllegalStateException(Messages.ERR_STR_ILLEGAL_START);
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isRunning) {

            for (EPStatement stmt : stmts.values())
                if (stmt.isStarted())
                    stmt.stop();

            // Destroys context creating statements
            for (StrategyEpStatement s : getStrategyStatements())
                if (s.isContextCreator())
                    if (!stmts.get(s).isDestroyed())
                        stmts.get(s).destroy();

            deactivateDataSinks();


            isRunning = false;
            signalStrategyStateChanged();
            executor.stop();

            log.info("Strategy (" + this.getName() + ") was stoped");
        } else throw new IllegalStateException(Messages.ERR_STR_ILLEGAL_STOP);
    }

    private void signalStrategyStateChanged() {
        sendUpdate(new StrategyStateChangedEvent(getName(), getId(), isEnabled(), isRunning(), isExecutionAllowed()));
    }

    private void signalStrategyInfoChanged() {
        sendUpdate(new StrategyInfoChangedEvent(getName(), getId(), getType(), getDescription()));
    }

    @Override
    public void sendUpdate(final Object data) {

        if (data instanceof StatusChanged) {
            runtime.connectionStatusChanged(this, (StatusChanged) data);
        }

        cep.sendEvent(data);

        if (data instanceof StrategyEvent) {
            executor.sendToStrategyDataStream(data);
        }

        if (data instanceof com.quantfabric.algo.market.datamodel.BaseLightweightMDFeedEvent) {

            executor.sendToStrategyDataStream(data);
        }

        if (data instanceof ComplexMarketView || data instanceof MDOrderBook) {
            executor.sendToStrategyDataStream(data);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void update(final Map data, final String eventTypeName) {
        cep.sendEvent(data, eventTypeName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void update(final Map[] data, final String dataTypeName) {
        for (Map event : data) {
            update(event, dataTypeName);
        }
    }

    @Override
    public void sendUpdate(final Object[] data) {
        for (Object event : data) {
            sendUpdate(event);
        }
    }

    @Override
    public void activateDataSinks() {

        ArrayList<DataSinkImpl> activeSinks = new ArrayList<DataSinkImpl>(getDataSinks().size());

        for (DataSink sink : getDataSinks()) {
            try {
                if (!sink.isActive())
                    runtime.activateSink(this, sink);
            } catch (Exception e) {
                String errMsg = String.format(Messages.ERR_STR_ACTIVATE_SINK, sink.getName(), getName());
                log.error(errMsg, e);
                //on rollback deactivate sinks
                for (DataSink actSink : activeSinks) {
                    try {
                        runtime.deActivateSink(this, actSink);
                    } catch (Exception ex) {
                        log.error(String.format(Messages.ERR_STR_RBACK_DEACT_SINK, actSink.getName(), getName()), ex);
                    }
                }
                throw new QuantfabricStrategyRuntimeException(errMsg);

            }
        }
    }

    @Override
    public void deactivateDataSinks() {


        for (DataSink sink : getDataSinks()) {
            try {
                if (!sink.getName().equals("StatusChanged")) {
                    runtime.deActivateSink(this, sink);
                }
            } catch (Exception e) {
                log.error(String.format(Messages.ERR_STR_RBACK_DEACT_SINK,
                        sink.getName(), getName()), e);

            }
        }

    }

    @Override
    public boolean validateEndPoints() {
        return false;
    }


    /******************************************************************
     *               DESIGN & CONFIG
     *******************************************************************/
    @Override
    public synchronized ExecutionPoint addExecutionPoint(ExecutionPoint point) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "addExecutionPoint"));
        synchronized (execEndPoints) {
            for (ExecutionPoint excPoint : execEndPoints) {
                if (excPoint.getTargetMarket().equals(point.getTargetMarket())) {
                    if (point.getConnection() != "")
                        ((ExecutionPointImpl) excPoint).setConnection(point.getConnection());
                    return excPoint;
                }
            }
            execEndPoints.add(point);
        }
        return point;
    }

    @Override
    public synchronized void removeExecutionPoint(String point) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "removeExecutionPoint"));
        synchronized (execEndPoints) {
            for (ExecutionPoint excPoint : execEndPoints) {
                if (excPoint.getTargetMarket().equals(point)) {
                    execEndPoints.remove(excPoint);
                    return;
                }
            }
        }
    }

    @Override
    public synchronized DataSink addDataSink(DataSink sink) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "addDataSink"));
        synchronized (dataSinks) {
            for (DataSink isink : dataSinks) {
                if (isink.getName().equals(sink.getName())) {
                    if (sink.getPipeline() != "")
                        isink.setPipeline(sink.getPipeline());
                    return isink;
                }
            }
            dataSinks.add(sink);
        }
        return sink;
    }

    @Override
    public synchronized void removeDataSink(String sinkName) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "removeDataSink"));
        synchronized (dataSinks) {
            for (DataSink sink : dataSinks) {
                if (sink.getName().equals(sinkName)) {
                    dataSinks.remove(sink);
                    return;
                }
            }
        }
    }

    public synchronized void addStatement(StrategyEpStatement definition) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "addStatement"));

        if (!stmts.containsKey(definition)) {
            final EPStatement stmt;
            try {
                stmt = cep.registerStatement(definition.getName(),
                        definition.getStatement(),
                        definition.getPersistMode(),
                        definition.getPersisterCustomSettingBlocks(),
                        definition.isDebugMode());
            } catch (Exception ex) {
                throw new QuantfabricCEPException(String.format("Register statement failed, name - '%s', epl - '%s'", definition.getName(), definition.getStatement()), ex);
            }
            if (definition.isExecutionEventProvider()) {
                cep.setSubscriber(stmt, executor);
            }
            stmts.put(definition, stmt);
        }
    }

    @Override
    public synchronized void removeStatement(String name) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "removeStatement"));


    }

    @Override
    public void setLayoutDefinitionProvider(
            LayoutDefinitionProvider layoutDefinitionProvider) {
        this.layoutDefinitionProvider = layoutDefinitionProvider;
    }

    @Override
    public String getSettingsLayoutDefinition() {
        if (layoutDefinitionProvider == null)
            return null;

        Document layoutDefinitionDocument = layoutDefinitionProvider.getLayoutDefinition();

        if (layoutDefinitionDocument == null)
            return null;

        try {
            return Converter.domToString(layoutDefinitionDocument);
        } catch (TransformerException e) {
            log.error("Can't transform layout definition document to text", e);
            return null;
        }
    }

    public synchronized void reloadSettingsToCep() {
        for (Entry<String, StrategySettingImpl> entry : settings.entrySet()) {
            cep.removeVariable(entry.getKey());
            addVariableToCep(entry.getKey(), entry.getValue().getType(), entry.getValue().getValue(),
                    entry.getValue().getModificationMode());
        }
    }

    @Override
    public synchronized void addSetting(String name, String value, String type,
                                        String scope, String modificationMode, String regionName,
                                        String displayName, String parametersViewId, String groupId) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "addSetting"));

        synchronized (settings) {
            StrategySettingImpl strategySetting = new StrategySettingImpl(name, type,
                    Scope.valueOf(scope.toUpperCase()), ModificationMode.valueOf(modificationMode.toUpperCase()), regionName,
                    displayName, parametersViewId, groupId);

            strategySetting.setValue(value);

            settings.put(name, strategySetting);

            try {
                addVariableToCep(name, type, value, strategySetting.getModificationMode());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private void addVariableToCep(String name, String type, String value, ModificationMode modificationMode) {
        cep.addVariable(name, type, value,
                (modificationMode == ModificationMode.READONLY || modificationMode == ModificationMode.NOT_RUNTIME));
    }

    @Override
    public synchronized void removeSetting(String name) {
        if (isRunning)
            throw new IllegalStateException(String.format(Messages.ERR_STR_ILLEGAL_STATE_CALL, "removeSetting"));

        synchronized (settings) {
            settings.remove(name);

            try {
                cep.removeVariable(name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    private int strategyDataStreamPort;

    @Override
    public void setStrategyDataStreamPort(int port) {
        this.strategyDataStreamPort = port;
    }

    @Override
    public int getStrategyDataStreamPort() {
        return strategyDataStreamPort;
    }

    @Override
    public void submitMarketOrder(String source, String executionPoint,
                                  OrderSide side, String instrumentId, int size, TimeInForceMode timeInForce, int expireSec) {
        log.debug("Manual Trading. Incomming submit MARKET order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size);

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setOrderType(OrderType.FOREX_MARKET);
        order.setOrderSide(side);
        order.setTimeInForceMode(timeInForce);
        if (timeInForce.equals(TimeInForceMode.GOOD_TILL_SECONDS))
            order.setExpireSeconds(expireSec);

        executor.update(executionPoint, order);
    }

    @Override
    public void submitLimitOrder(String source, String executionPoint,
                                 OrderSide side, String instrumentId, int size, int price, TimeInForceMode timeInForce, int expireSec) {
        log.debug("Manual Trading. Incomming submit LIMIT order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size + ", price=" + price);

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setPrice(price);
        order.setOrderType(OrderType.QUANTFABRIC_LIMIT);
        order.setTimeInForceMode(timeInForce);
        if (timeInForce.equals(TimeInForceMode.GOOD_TILL_SECONDS))
            order.setExpireSeconds(expireSec);
        order.setOrderSide(side);
        executor.update(executionPoint, order);
    }

    @Override
    public void submitStopLimitOrder(String source, String executionPoint,
                                     OrderSide side, String instrumentId, int size, int price,
                                     StopSides stopSide, int stopPrice, TimeInForceMode timeInForce, int expireSec) {
        log.debug("Manual Trading. Incomming submit STOP-LIMT order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size + ", price=" + price + ", stopSide=" + stopSide + ", stopPrice=" + stopPrice);

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setPrice(price);
        order.setOrderType(OrderType.STOP_LIMIT);
        order.setTimeInForceMode(timeInForce);
        if (timeInForce.equals(TimeInForceMode.GOOD_TILL_SECONDS))
            order.setExpireSeconds(expireSec);
        order.setOrderSide(side);
        order.setStopSide(stopSide);
        order.setStopPrice(stopPrice);
        executor.update(executionPoint, order);
    }

    @Override
    public void submitStopLossOrder(String source, String executionPoint,
                                    OrderSide side, String instrumentId, int size, StopSides stopSide,
                                    int stopPrice, TimeInForceMode timeInForce, int expireSec) {
        log.debug("Manual Trading. Incomming submit STOP-LOSS order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size + ", stopSide=" + stopSide + ", stopPrice=" + stopPrice);

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setOrderType(OrderType.STOP_LOSS);
        order.setTimeInForceMode(timeInForce);
        if (timeInForce.equals(TimeInForceMode.GOOD_TILL_SECONDS))
            order.setExpireSeconds(expireSec);
        order.setOrderSide(side);
        order.setStopSide(stopSide);
        order.setStopPrice(stopPrice);
        executor.update(executionPoint, order);

    }

    @Override
    public void submitTrailingStopOrder(String source, String executionPoint,
                                        OrderSide side, String instrumentId, int size,
                                        int price, StopSides stopSide, int stopPrice, int trailBy,
                                        int maxSlippage, int initialTriggerRate, TimeInForceMode timeInForce, int expireSec) {
        log.debug("Manual Trading. Incomming submit TRAILING-STOP order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size + ", stopSide=" + stopSide + ", stopPrice=" + stopPrice + ", trialBy=" + trailBy + ", maxSlippage=" + maxSlippage + ", initialTriggerRate=" + initialTriggerRate);

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setPrice(price);
        order.setOrderType(OrderType.TRAILING_STOP);
        order.setTimeInForceMode(timeInForce);
        if (timeInForce.equals(TimeInForceMode.GOOD_TILL_SECONDS))
            order.setExpireSeconds(expireSec);
        order.setOrderSide(side);
        order.setStopSide(stopSide);
        order.setStopPrice(stopPrice);
        order.setTrailBy(trailBy * 10);
        order.setMaxSlippage(maxSlippage);
        order.setInitialTriggerRate(initialTriggerRate);
        order.setPeggedSettings(new PeggedSettings());
        executor.update(executionPoint, order);
    }

    @Override
    public void submitOCOOrder(String source, String executionPoint,
                               OrderSide side, String instrumentId, int size,
                               OCOSettings ocoSettings) {
        log.debug("Manual Trading. Incomming submit OCO order request (source=" + source + ", executionPoint=" + executionPoint + ", side=" + side + ", instrumentId=" + instrumentId + ", size=" + size + ", leg1LimitRate=" + ocoSettings.getLeg1LimitRate() + ", leg2Type=" + ocoSettings.getLeg2Type() + ", leg2Side=" + ocoSettings.getLeg2Side() + ", leg2StopRate=" + ocoSettings.getLeg2StopRate() + ", leg2StopSide=" + ocoSettings.getLeg2StopSide() + ", leg2StopLimitRate=" + ocoSettings.getLeg2StopLimitRate());

        TradeOrder order = new TradeOrder("MT_" + source + "_" + TradeOrder.generateReference());
        order.setComplexOrderReference(source);
        order.setInstrumentId(instrumentId);
        order.setSize(size);
        order.setOrderType(OrderType.ONE_CANCELS_THE_OTHER);
        order.setTimeInForceMode(TimeInForceMode.GOOD_TILL_CANCEL);
        order.setOrderSide(side);
        order.setOcoSettings(ocoSettings);
        executor.update(executionPoint, order);
    }

    @Override
    public void cancelOrder(String source, String executionPoint,
                            String originalOrderReference) {
        log.debug("Manual Trading. Incomming cancel order request (source=" + source + ", executionPoint=" + executionPoint + ", originalOrderReference=" + originalOrderReference);
        executor.update(executionPoint, new CancelStrategyOrderCommand(originalOrderReference));
    }

    @Override
    public void replaceOrder(String source, String executionPoint,
                             String originalOrderReference, String newOrderReference, int size,
                             int price) {
        log.debug("Manual Trading. Incomming replace order request (source=" + source + ", executionPoint=" + executionPoint + ", originalOrderReference=" + originalOrderReference + ", newOrderReference=" + newOrderReference + ", size=" + size + ", price=" + price);
        executor.update(executionPoint,
                new ReplaceStrategyOrderCommand(originalOrderReference, newOrderReference, price, size));
    }
}
