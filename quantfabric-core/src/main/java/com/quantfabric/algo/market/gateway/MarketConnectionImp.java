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
package com.quantfabric.algo.market.gateway;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.quantfabric.algo.market.connector.VirtualCoinMarketAdapter;
import com.quantfabric.algo.market.gateway.commands.*;
import com.quantfabric.messaging.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.storage.MarketDataCacheProvider;
import com.quantfabric.algo.commands.Command;
import com.quantfabric.algo.instrument.Instrument;
import com.quantfabric.algo.market.datamodel.StatusChanged;
import com.quantfabric.algo.market.datamodel.StatusChanged.MarketConnectionStatuses;
import com.quantfabric.algo.market.dataprovider.FeedName;
import com.quantfabric.algo.market.dataprovider.orderbook.processor.OrderBookSnapshotListener;
import com.quantfabric.algo.market.gateway.MarketAdapter.AdapterStatus;
import com.quantfabric.algo.market.gateway.MarketAdapter.LogonListener;
import com.quantfabric.algo.market.gateway.MarketAdapter.MarketAdapterException;
import com.quantfabric.algo.market.gateway.MarketConnectionImp.MarketConnectionConfigException.ConfigPropertyIssue;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeed;
import com.quantfabric.algo.market.gateway.feed.ExecutionFeedCollection;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.market.gateway.feed.FeedsManager;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeed;
import com.quantfabric.algo.market.gateway.feed.MarketDataFeedCollection;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.report.InterruptFailed;
import com.quantfabric.algo.order.report.Interrupted;
import com.quantfabric.algo.order.report.Rejected;
import com.quantfabric.algo.order.report.Trade;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.messaging.Subscriber;
import com.quantfabric.util.PropertiesViewer;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;

public abstract class MarketConnectionImp
		implements MarketConnection
{
	public static class MarketConnectionConfigException extends MarketConnectionException
	{
		public enum ConfigPropertyIssue
		{
			NOT_SPECIFIED,
			PARSING_ERROR,
			WRONG_VALUE,
			EXTERNAL_RESOURCE_ISNOT_EXISTS
		}

		/**
		 *
		 */
		private static final long serialVersionUID = -7201026107246888914L;

		private static String getIssueDescription(ConfigPropertyIssue issue)
		{
			switch (issue)
			{
				case NOT_SPECIFIED:
					return "not specified property";
				case PARSING_ERROR:
					return "can't parse value of property";
				case WRONG_VALUE:
					return "wrong value specified for property";
				case EXTERNAL_RESOURCE_ISNOT_EXISTS:
					return "external resource does not found";
				default :
					return "error";
			}
		}

		private static String getMessage(ConfigPropertyIssue issue, String propertyName)
		{
			return getIssueDescription(issue) + " (property name - " + propertyName +")";
		}

		public MarketConnectionConfigException(ConfigPropertyIssue issue, String propertyName)
		{
			super(getMessage(issue, propertyName));
		}

		public MarketConnectionConfigException(ConfigPropertyIssue issue,
											   String propertyName, Throwable innerException)
		{
			super(getMessage(issue, propertyName), innerException);
		}

		public MarketConnectionConfigException(Throwable innerException)
		{
			super(innerException);
		}
	}

	private class AdapterLogonListener implements LogonListener
	{
		@Override
		public void loggedin(MarketAdapter sender)
		{
			getLogger().info("Connection {} logged in {}", getName(), sender.getIdentifier());

			if(VirtualCoinMarketAdapter.SubscribeType.PUSHING.name()
					.equalsIgnoreCase(getAdapterSettings().getProperty(SUBSCRIBE_TYPE))
					&& settings.containsKey(BATCH_SUBSCRIPTION)
					&& Boolean.parseBoolean(settings.getProperty(BATCH_SUBSCRIPTION))) {
				delegatedExecutionToAdapter(new BatchSubscribeCommand(marketDataFeedsManager.getCollection().values(),
						executionFeedsManager.getCollection().values()));
			}
			else {
				for (MarketDataFeed feed : marketDataFeedsManager.getCollection().values()) {
					delegatedExecutionToAdapter(new SubscribeCommand(feed));
				}
			}
			publishStatusChanged(MarketConnectionStatuses.CONNECTED);
		}

		@Override
		public void logout(MarketAdapter sender)
		{
			getLogger().info("Connection {}  logout from {}", getName(), sender.getIdentifier());

			publishStatusChanged(MarketConnectionStatuses.DISCONNECTED);
		}

		private void publishStatusChanged(MarketConnectionStatuses marketConnectionStatus)
		{
			try
			{
				StatusChanged event = new StatusChanged(getName(), mode, marketConnectionStatus);

				publish(event);

				statusChangedListeners.removeIf(statusChangedListener -> !statusChangedListener.onStatusChanged(event));
			}
			catch (PublisherException e)
			{
				log.error("Can't publish status notification", e);
			}
		}
	}

	private static final Logger log = LoggerFactory.getLogger(MarketConnectionImp.class);

	private String name;
	private MarketAdapter adapter;
	private final MarketGateway owner;
	private MarketConnectionMode mode = MarketConnectionMode.BASIC;
	private final FeedsManager<MarketDataFeed, MarketDataFeedCollection> marketDataFeedsManager;
	private final FeedsManager<ExecutionFeed, ExecutionFeedCollection> executionFeedsManager;
	//private final BackTestingMarketAdapter backTestingMarketAdapter;
	//private final VirtualCoinBackTestingMarketAdapter virtualCoinBackTestingMarketAdapter;
	private final List<StatusChangedListener> statusChangedListeners = new ArrayList<>();
	private final Map<Instrument, CreditLimit> creditLimits =	new HashMap<>();
	private final CreditCalculator creditCalculator = new CreditCalculator();

	protected Properties settings;
	protected Properties credentials;


	public MarketConnectionImp(MarketGateway owner, String name, Properties adapterSettings, Properties credentials) throws MarketConnectionException {
		this.owner = owner;
		this.name = name;

		this.settings = adapterSettings;
		this.credentials = credentials;

/*		backTestingMarketAdapter = new BackTestingMarketAdapter();
		backTestingMarketAdapter.addLogonListerner(new AdapterLogonListener());
		backTestingMarketAdapter.setFeedProvider(this);
		backTestingMarketAdapter.setInstrumentProvider(this);

		virtualCoinBackTestingMarketAdapter = new VirtualCoinBackTestingMarketAdapter();
		virtualCoinBackTestingMarketAdapter.addLogonListerner(new AdapterLogonListener());
		virtualCoinBackTestingMarketAdapter.setFeedProvider(this);
		virtualCoinBackTestingMarketAdapter.setInstrumentProvider(this);*/

		initializeAdapter(adapterSettings, credentials);
		getAdapter().setFeedProvider(this);
		getAdapter().setInstrumentProvider(this);
		executionFeedsManager = new FeedsManager<>(new ExecutionFeedCollection());
		marketDataFeedsManager = new FeedsManager<>(new MarketDataFeedCollection());
	}


	@Override
	public String getIdentifier()
	{
		return getCurrentMarketAdapter().getIdentifier();
	}

	@Override
	public Instrument getInstrument(String id)
	{
		return owner.getInstrument(id);
	}

	@Override
	public Instrument getInstrumentBySymbol(String symbol)
	{
		return owner.getInstrumentBySymbol(symbol);
	}

	@Override
	public Collection<ExecutionFeed> getExecutionFeeds()
	{
		return executionFeedsManager.getCollection().values();
	}

	@Override
	public MarketDataFeed getMarketDataFeed(String symbol)
	{
		return marketDataFeedsManager.getCollection().get(symbol);
	}

	public ExecutionFeed getExecutionFeed(String symbol) {
		return executionFeedsManager.getCollection().get(symbol);
	}

	@Override
	public void unregisterSubscriber(Subscriber<Object> subscriber)
	{
		getCurrentMarketAdapter().unregisterSubscriber(subscriber);
	}

	@Override
	public void subscribe(int subscriberId, FeedName subject)
			throws Publisher.PublisherException
	{
		if(!checkSubscribeNow(subject))
		{
			throw new PublisherException("Can't subscribe while market connection is active");
		}

		getCurrentMarketAdapter().subscribe(subscriberId, subject);
	}

	@Override
	public void unSubscribe(int subscriberId, FeedName subject)
	{
		getCurrentMarketAdapter().unSubscribe(subscriberId, subject);
	}

	@Override
	public void publish(Object data) throws PublisherException
	{
		getCurrentMarketAdapter().publish(data);
	}

	@Override
	public void subscribe(Subscriber<Object> subscriber, FeedName subject)
			throws Publisher.PublisherException
	{
		if(!checkSubscribeNow(subject))
		{
			throw new PublisherException("Can't subscribe while market connection is active");
		}

		getCurrentMarketAdapter().subscribe(subscriber, subject);
	}

	@Override
	public void unSubscribe(Subscriber<Object> subscriber, FeedName subject)
	{
		getCurrentMarketAdapter().unSubscribe(subscriber, subject);
	}

	@Override
	public void addFeed(ExecutionFeed feed)
	{
		executionFeedsManager.add(feed);
	}

	@Override
	public void removeFeed(ExecutionFeed feed)
	{
		executionFeedsManager.remove(feed);
	}

	@Override
	public void addFeed(MarketDataFeed feed)
	{
		if (marketDataFeedsManager.add(feed))
			delegatedExecutionToAdapter(new SubscribeCommand(feed));
	}

	@Override
	public void removeFeed(MarketDataFeed feed)
	{
		if (marketDataFeedsManager.remove(feed))
			delegatedExecutionToAdapter(new UnsubscribeCommand(feed));
	}

	@Override
	public Feed getFeed(int feedId)
	{
		Feed feed = getMarketDataFeed(feedId);
		if (feed == null)
			feed = getExecutionFeed(feedId);

		return feed;
	}

	@Override
		public MarketDataFeed getMarketDataFeed(int feedId)
	{
		return marketDataFeedsManager.getCollection().get(feedId);
	}

	@Override
	public ExecutionFeed getExecutionFeed(int feedId)
	{
		return executionFeedsManager.getCollection().get(feedId);
	}

	@Override
	public MarketDataFeed getMarketDataFeed(FeedName feedName)
	{
		return marketDataFeedsManager.getCollection().get(feedName);
	}

	@Override
	public ExecutionFeed getExecutionFeed(FeedName feedName)
	{
		return executionFeedsManager.getCollection().get(feedName);
	}

	@Override
	public MarketConnectionMode getMode()
	{
		return mode;
	}

	@Override
	public void registerSubscriber(Subscriber<Object> subscriber) {
		getCurrentMarketAdapter().registerSubscriber(subscriber);
	}

	@Override
	public void execute(Command command)
	{
		if (command.getClass() == SubscribeCommand.class)
		{
			SubscribeCommand sc = (SubscribeCommand)command;
			MarketDataFeed feed = sc.getFeed();

			if (!marketDataFeedsManager.add(feed))
				return;
		}

		if (command.getClass() == UnsubscribeCommand.class)
		{
			UnsubscribeCommand sc = (UnsubscribeCommand)command;
			MarketDataFeed feed = sc.getFeed();

			if (!marketDataFeedsManager.remove(feed.getFeedName()))
				return;
		}

		if (command.getClass() == SubmitOrderCommand.class)
		{
			SubmitOrderCommand submitOrderCommand = (SubmitOrderCommand)command;
			TradeOrder order = submitOrderCommand.getOrder();
			if (order.getInstrument() == null && order.getInstrumentId() != null)
					order.setInstrument(getInstrument(order.getInstrumentId()));

			if (!checkCanSubmitOrder(submitOrderCommand, true))
			{
				rejectOrder(order, "Exceeded credit limit (instrumentId=" + order.getInstrumentId());
				return;
			}
		}

		if (command.getClass() == ReplaceOrderCommand.class)
		{
			ReplaceOrderCommand replaceOrderCommand = (ReplaceOrderCommand)command;
			TradeOrder order = replaceOrderCommand.getOrder();
			if (order.getInstrument() == null && order.getInstrumentId() != null)
					order.setInstrument(getInstrument(order.getInstrumentId()));

			if (!checkCanReplaceOrder(replaceOrderCommand, true))
			{
				rejectInterruptuion(replaceOrderCommand, "Exceeded credit limit (instrumentId=" + order.getInstrumentId());
				return;
			}
		}

		delegatedExecutionToAdapter(command);
	}

	@Override
	public boolean isReadyToExecution()
	{
		return getCurrentMarketAdapter().isReadyToExecution();
	}

	@Override
	public void setLogonPassword(String password) throws MarketConnectionException
	{
		try
		{
			getCurrentMarketAdapter().setPassword(password);
		}
		catch (MarketAdapterException e)
		{
			throw new MarketConnectionException(e);
		}
	}

	@Override
	public void addOrderBookSnapshotListener(FeedName feedName,
											 OrderBookSnapshotListener listener)
	{
		if (checkAddOrderBookSnapshotListenerNow(feedName))
			getCurrentMarketAdapter().addOrderBookSnapshotListener(feedName, listener);
	}

	@Override
	public void removeOrderBookSnapshotListener(FeedName feedName,
												String listenerName)
	{
		getCurrentMarketAdapter().removeOrderBookSnapshotListener(feedName, listenerName);
	}

	@Override
	public void addStatusChangedListener(StatusChangedListener listener)
	{
		statusChangedListeners.add(listener);
	}

	@Override
	public CreditCalculator getCreditCalculator()
	{
		return creditCalculator;
	}

	@Override
	public void cancelLoanByTrade(TradeOrder order, Trade report)
	{
		cancelLoan(order.getInstrument(), report.getQuantity());
	}

	@Override
	public void cancelLoanByInterrupted(TradeOrder order, Interrupted report)
	{
		if (!report.getSourceName().equals(getName()))
		{
			cancelLoan(order.getInstrument(), order.getSize());
		}
	}

	@Override
	public void cancelLoanByReplaceFailed(TradeOrder order, InterruptFailed report,
										  double failedReplaceSize)
	{
		if (!report.getSourceName().equals(getName()))
		{
			cancelLoan(order.getInstrument(), (failedReplaceSize - order.getSize()));
		}
	}

	@Override
	public void setCreditLimit(Instrument instrument, double limitValue)
	{
		if (!creditLimits.containsKey(instrument))
			creditLimits.put(instrument, new CreditLimitImpl(instrument, limitValue));
		else
			creditLimits.get(instrument).setLimitValue(limitValue);
	}

	@Override
	public Collection<CreditLimit> getCreditLimits()
	{
		return creditLimits.values();
	}

	@Override
	public CreditLimit getCreditLimit(Instrument instrument)
	{
		return creditLimits.get(instrument);
	}

	protected Logger getLogger()
	{
		return log;
	}

	public MarketAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MarketAdapter adapter) {
		this.adapter = adapter;
	}

	public Properties getAdapterSettings()
	{
		return settings;
	}

	public Collection<MarketDataFeed> getMarketDataFeeds()
	{
		return marketDataFeedsManager.getCollection().values();
	}



	private boolean checkSubscribeNow(FeedName feedName)
	{
		return (getFeed(feedName) instanceof ExecutionFeed || feedName.getName().equals("SERVICE")) || !isConnected();
	}

	private boolean checkAddOrderBookSnapshotListenerNow(FeedName feedName)
	{
		return !isConnected();
	}


	/*public MarketDataPlayer getMarketDataPlayer()
	{
		return backTestingMarketAdapter.getPlayer();
	}*/

	public void setMarketDataStorageProvider(MarketDataCacheProvider marketDataCacheProvider)
	{
		getAdapter().setMarketDataCacheProvider(marketDataCacheProvider);
		//backTestingMarketAdapter.setMarketDataStorageProvider(marketDataStorageProvider);
		//virtualCoinBackTestingMarketAdapter.setMarketDataStorageProvider(marketDataStorageProvider);
	}

	protected abstract void initializeAdapter(Properties adapterSettings, Properties credentals) throws MarketConnectionException;

	protected MarketAdapter getCurrentMarketAdapter()
	{
		if (mode == MarketConnectionMode.BASIC) {
			return getAdapter();
			//case BACK_TESTING:
			//	return backTestingMarketAdapter;
			//case BITCOIN_BACK_TESTING:
			//	return virtualCoinBackTestingMarketAdapter;
		}

		return null;
	}



	public Feed getFeed(FeedName feedName)
	{
		Feed feed = getMarketDataFeed(feedName);
		if (feed == null)
			feed = getExecutionFeed(feedName);

		return feed;
	}


	protected void setMarketAdapter(MarketAdapter adapter)
	{
		this.setAdapter(adapter);
		this.getAdapter().addLogonListerner(new AdapterLogonListener());
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setMode(MarketConnectionMode mode) throws MarketConnectionException
	{
		if (getCurrentMarketAdapter().getStatus() != AdapterStatus.DISCONNECTED)
			throw new MarketConnectionException("Can't change mode while connection is active");

		this.mode = mode;
	}


	public void connect() throws MarketConnectionException
	{
		try
		{
			MarketAdapter currentAdapter = getCurrentMarketAdapter();

			if (getMode() == MarketConnectionMode.BACK_TESTING || getMode() == MarketConnectionMode.BITCOIN_BACK_TESTING)
				getLogger().warn("Connection " + this.getName() + " working in BackTesting mode");

			currentAdapter.logon();
		}
		catch (MarketAdapterException e)
		{
			throw new MarketConnectionException(e);
		}
	}

	public void disconnect() throws MarketConnectionException
	{
		try
		{
			getCurrentMarketAdapter().logout();
		}
		catch (MarketAdapterException e)
		{
			throw new MarketConnectionException(e);
		}
	}


	private void delegatedExecutionToAdapter(Command command)
	{
		if (getCurrentMarketAdapter() != null &&
				getCurrentMarketAdapter().getStatus() == AdapterStatus.CONNECTED)
			try
			{
				getCurrentMarketAdapter().execute(command);
			}
			catch (Throwable t)
			{
				rejectCommand(command, "Execution failed");
				getLogger().error("Execution failed" , t);
			}
		else
			rejectCommand(command, "Executor is inactive");
	}

	private void quietPublish(Object data)
	{
		try
		{
			publish(data);
		}
		catch (PublisherException e)
		{
			getLogger().error("Can't publish (" + data.toString() + ")", e);
		}
	}

	private void rejectOrder(TradeOrder order, String reason)
	{
		if (order.getInstrument() == null && order.getInstrumentId() != null)
				order.setInstrument(getInstrument(order.getInstrumentId()));

		Rejected rejectedReport = new Rejected(0, getName(), GregorianCalendar.getInstance().getTime(),
				order.getOrderReference(), order.getOrderReference(), "REJ-" + order.getOrderReference(),
				reason);

		rejectedReport.setText(rejectedReport.getReason());

		quietPublish(rejectedReport);
	}

	private void rejectInterruptuion(ManageAcceptedOrder command, String reason)
	{
		TradeOrder order = command.getOrder();

		if (order.getInstrument() == null && order.getInstrumentId() != null)
				order.setInstrument(getInstrument(order.getInstrumentId()));

		InterruptFailed interruptFailed = new InterruptFailed(0, getName(), GregorianCalendar.getInstance().getTime(),
				command.getInstitutionOrderReference(), order.getOrderReference());

		interruptFailed.setOriginalLocalOrderReference(order.getOrderReference());
		interruptFailed.setExecutionID("REP_REJ-" + order.getOrderReference());
		interruptFailed.setText(reason);


		quietPublish(interruptFailed);
	}

	private void rejectCommand(Command command, String reason)
	{
		if (command instanceof SubmitOrder)
		{
			SubmitOrder submitOrderCommand = (SubmitOrder)command;
			TradeOrder order = submitOrderCommand.getOrder();
			rejectOrder(order, reason);
		}

		if (command instanceof ManageAcceptedOrder)
		{
			ManageAcceptedOrder manageAcceptedOrderCommand = (ManageAcceptedOrder)command;
			rejectInterruptuion(manageAcceptedOrderCommand, reason);
		}

	}


	public boolean isConnected()
	{
		return getCurrentMarketAdapter().getStatus() == AdapterStatus.CONNECTED;
	}

	public static Properties makeCommonSettings(
			Properties settings, Properties credentals)
			throws MarketConnectionConfigException
	{
		Properties commonSettings = new Properties(settings);

		String pathToExternalCfg =
				PropertiesViewer.getProperty(settings, CONFIG_URL, null);

		if (pathToExternalCfg != null)
		{
			pathToExternalCfg = QuantfabricRuntime.getAbsolutePath(pathToExternalCfg);
			try (FileReader fileReader = new FileReader(pathToExternalCfg))
			{
				commonSettings.load(fileReader);
			}
			catch (FileNotFoundException e)
			{
				throw new MarketConnectionConfigException(
						ConfigPropertyIssue.EXTERNAL_RESOURCE_ISNOT_EXISTS,
						pathToExternalCfg);
			}
			catch (IOException e)
			{
				throw new MarketConnectionConfigException(e);
			}
		}

		for (String propertyName : credentals.stringPropertyNames())
		{
			String newName;

			if (propertyName.length() > 1)
				newName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
			else
				newName = propertyName.toUpperCase();

			commonSettings.setProperty(newName, credentals.getProperty(propertyName));
		}

		return commonSettings;
	}



	private boolean checkCanReplaceOrder(ReplaceOrder replaceCommand, boolean loanIfCan)
	{
		double additionLoanValue = replaceCommand.getReplacementSize() - replaceCommand.getOrder().getSize();
		Instrument instrument = replaceCommand.getOrder().getInstrument();
		return checkCanLoan(instrument, additionLoanValue, loanIfCan);
	}

	private boolean checkCanSubmitOrder(SubmitOrder submitOrder, boolean loanIfCan)
	{
		double additionLoanValue = submitOrder.getOrder().getSize();
		Instrument instrument = submitOrder.getOrder().getInstrument();
		return checkCanLoan(instrument, additionLoanValue, loanIfCan);
	}

	private boolean checkCanLoan(Instrument instrument, double value,  boolean loanIfCan)
	{
		double currentLoanValue = creditCalculator.getCreditValue(instrument);

		boolean isCan = true;
		if(isCreditLimitedInstrument(instrument))
			isCan = ((currentLoanValue + value) <= getCreditLimit(instrument).getLimitValue());

		if (isCan && loanIfCan)
			creditCalculator.loan(instrument, value);

		getLogger().debug("Credit calculator : loan - " + isCan + ", Instrument (" + instrument.getId() + ") credit " + currentLoanValue + " -> " + creditCalculator.getCreditValue(instrument));

		return isCan;
	}



	private void cancelLoan(Instrument instrument, double value)
	{
		double currentLoanValue = creditCalculator.getCreditValue(instrument);

		creditCalculator.cancel(instrument, value);

		getLogger().debug("Credit calculator : Instrument (" + instrument.getId() + ") credit " + currentLoanValue + " -> " + creditCalculator.getCreditValue(instrument));
	}


	protected boolean isCreditLimitedInstrument(Instrument instrument)
	{
		return creditLimits.containsKey(instrument);
	}


	//private IncrementalUpdatesHandler askHandler;
	//private IncrementalUpdatesHandler bidHandler;

}
