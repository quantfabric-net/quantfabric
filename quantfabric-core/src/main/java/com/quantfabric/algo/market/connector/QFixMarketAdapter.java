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
package com.quantfabric.algo.market.connector;

import java.time.ZoneId;
import java.util.Date;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.ExecInst;
import quickfix.field.MDElementName;
import quickfix.field.MDEntryType;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.NoMDEntries;
import quickfix.field.OrdType;
import quickfix.field.SendingTime;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;

import com.quantfabric.algo.commands.CommandFactory;
import com.quantfabric.algo.market.datamodel.EndUpdate;
import com.quantfabric.algo.market.datamodel.MDEvent;
import com.quantfabric.algo.market.datamodel.MDItem.MDItemType;
import com.quantfabric.algo.market.datamodel.MDMessageInfo.MDMessageType;
import com.quantfabric.algo.market.datamodel.MDPrice.PriceType;
import com.quantfabric.algo.market.datamodel.NewSnapshot;
import com.quantfabric.algo.market.gateway.BaseMarketAdapter;
import com.quantfabric.algo.market.gateway.feed.Feed;
import com.quantfabric.algo.order.OCOSettings;
import com.quantfabric.algo.order.OCOSettings.StopSide;
import com.quantfabric.algo.order.TradeOrder;
import com.quantfabric.algo.order.TradeOrder.ExecutionInstructions;
import com.quantfabric.algo.order.TradeOrder.OrderSide;
import com.quantfabric.algo.order.TradeOrder.OrderType;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.messaging.NativeSubscriberBuffer;
import com.quantfabric.messaging.Subscriber;

public abstract class QFixMarketAdapter extends BaseMarketAdapter implements
		Application
{
	public static class TypeConverter
	{
		public static class ConversionError extends Exception
		{
			private static final long serialVersionUID = -1623084771382259579L;
		
			public ConversionError(String message)
			{
				super(message);
			}
		}
		
		public static Side toQFixSide(TradeOrder.OrderSide orderSide) throws ConversionError
		{
			switch (orderSide)
			{
				case BUY :
					return new Side(Side.BUY);
				case SELL :
					return new Side(Side.SELL);					
				default : 
					throw new TypeConverter.ConversionError("Can't convert Order.OrderSide." + orderSide);
			}
		}
		
		public static OrderSide toOrderSide(Side side) throws ConversionError
		{
			return toOrderSide(side.getValue());
		}
		
		public static OrderSide toOrderSide(char sideValue) throws ConversionError
		{
			switch (sideValue)
			{
				case Side.BUY :
					return OrderSide.BUY;
				case Side.SELL:
					return OrderSide.SELL;					
				default : 
					throw new TypeConverter.ConversionError("Can't convert Side - " + sideValue);
			}
		}
		
		public static TradeOrder.OrderType toOrderType(OrdType ordType) throws ConversionError
		{
			return toOrderType(ordType.getValue());			
		}
		
		public static TradeOrder.OrderType toOrderType(char ordTypeValue) throws ConversionError
		{
			switch (ordTypeValue)
			{
				case OrdType.FOREX_MARKET:
					return OrderType.FOREX_MARKET;
				case OrdType.FOREX_LIMIT:
					return OrderType.FOREX_LIMIT;
				case OrdType.MARKET:
					return OrderType.MARKET;
				case OrdType.LIMIT:
					return OrderType.LIMIT;
				case OrdType.STOP_LIMIT:
					return OrderType.STOP_LIMIT;
				case OrdType.STOP_STOP_LOSS:
					return OrderType.STOP_LOSS;
				case 'W':
					return OrderType.ONE_CANCELS_THE_OTHER;		
				case 'V':
					return OrderType.TRAILING_STOP;
				default:
					throw new TypeConverter.ConversionError("Can't convert OrdType - " + ordTypeValue);
			}
			
		}
		
		public static OrdType toQFixOrdType(TradeOrder.OrderType orderType) throws ConversionError
		{
			switch (orderType)
			{
				case MARKET:
					return new OrdType(OrdType.MARKET);
				case STOP_LIMIT:
					return new OrdType(OrdType.STOP_LIMIT);
				case STOP_LOSS:
					return new OrdType(OrdType.STOP_STOP_LOSS);
				case LIMIT :
					return new OrdType(OrdType.LIMIT);
				case QUANTFABRIC_LIMIT :
					return new OrdType(OrdType.LIMIT);
				case FOREX_LIMIT :
					return new OrdType(OrdType.FOREX_LIMIT);
				case FOREX_MARKET:
					return new OrdType(OrdType.FOREX_MARKET);
				case ONE_CANCELS_THE_OTHER :
					return new OrdType('W');
				case THRESHOLD:
					return new OrdType('U');
				case IF_DONE:
					return new OrdType('X');
				case IF_DONE_OCO:
					return new OrdType('Y');
				case PEGGED:
					return new OrdType(OrdType.PEGGED);
				case TRAILING_STOP:
					return new OrdType('V');
				case ICEBERG:
					return new OrdType('Z');
				case MARKET_LIMIT:
					return new OrdType('K');
				default :
					throw new TypeConverter.ConversionError("Can't convert Order.OrderType." + orderType);
			}			
		}
		
		public static TimeInForce toQFixTimeInforce(TradeOrder.TimeInForceMode timeInForceMode) throws ConversionError
		{
			switch (timeInForceMode)
			{
				case DAY :
					return new TimeInForce(TimeInForce.DAY);
				case QUANTFABRIC_DAY :
					return new TimeInForce(TimeInForce.DAY);
				case FILL_OR_KILL :
					return new TimeInForce(TimeInForce.FILL_OR_KILL);
				case GOOD_TILL_CANCEL :
					return new TimeInForce(TimeInForce.GOOD_TILL_CANCEL);
				case GOOD_TILL_DATE :
					return new TimeInForce(TimeInForce.GOOD_TILL_DATE);
				case IMMEDIATE_OR_CANCEL :
					return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
				case GOOD_TILL_SECONDS :
					return new TimeInForce('X');
				default :
					throw new TypeConverter.ConversionError("Can't convert Order.TimeInForceMode." + timeInForceMode);
			}
		}
		
		public static TradeOrder.TimeInForceMode toTimeInForceMode(char timeInForce) throws ConversionError
		{
			switch (timeInForce)
			{
				case TimeInForce.DAY :
					return TradeOrder.TimeInForceMode.DAY;
				case TimeInForce.GOOD_TILL_CANCEL :
					return TradeOrder.TimeInForceMode.GOOD_TILL_CANCEL;
				case TimeInForce.IMMEDIATE_OR_CANCEL :
					return TradeOrder.TimeInForceMode.IMMEDIATE_OR_CANCEL;
				case TimeInForce.FILL_OR_KILL :
					return TradeOrder.TimeInForceMode.FILL_OR_KILL;
				case 'X' :
					return TradeOrder.TimeInForceMode.GOOD_TILL_SECONDS;
				default :
					throw new TypeConverter.ConversionError("Can't convert Order.TimeInForce - " + timeInForce);
			}
		}


		public static int toQFixOCOLegType(OCOSettings.LegType legType) throws ConversionError
		{
			switch (legType)
			{
				case STOP_LOSS :
					return 3;
				case STOP_LIMIT:
					return 4;
				default :
					throw new TypeConverter.ConversionError("Can't convert OCOSettings.LegType." + legType);
			}
		}
		
		public static int toQFixOCOLegSide(OCOSettings.LegSide legSide) throws ConversionError
		{
			switch (legSide)
			{
				case BUY :
					return 1;
				case SELL:
					return 2;
				default :
					throw new TypeConverter.ConversionError("Can't convert OCOSettings.LegSide." + legSide);
			}
		}
		
		public static int toQFixOCOStopSide(StopSide stopSide) throws ConversionError
		{
			switch (stopSide)
			{
				case BID :
					return 1;
				case OFFER:
					return 2;
				default :
					throw new TypeConverter.ConversionError("Can't convert OCOSettings.StopSide." + stopSide);
			}
		}
	
		public static char toQFixExecInst(ExecutionInstructions ei) throws ConversionError
		{
			switch (ei)
			{
				case ALL_OR_NONE:
					return ExecInst.ALL_OR_NONE_AON;
				case MARKET_PEG:
					return ExecInst.MARKET_PEG;
				case PRIMARY_PEG:
					return ExecInst.PRIMARY_PEG;
				default:
					throw new TypeConverter.ConversionError("Can't convert ExecutionInstructions." + ei);
			}
		}
		
		public static class ConvertedMDElementName
		{
			private PriceType priceType = PriceType.UNKNOWN;
			private int depthLevel = 0;
			
			protected ConvertedMDElementName(MDElementName elementName) throws ConversionError
			{
				convert(elementName);
			}
			
			private void convert(MDElementName elementName) throws ConversionError
			{
				switch (elementName.getValue())
				{
					case MDElementName.BEST_BID : 
					case MDElementName.BEST_OFFER :
						priceType = PriceType.INDICATOR_BEST;					
						break;
					case MDElementName.PAID : 
					case MDElementName.GIVEN : 
						priceType = PriceType.TRADE;
						break;
					case MDElementName.DEALABLE_BID : 
					case MDElementName.DEALABLE_OFFER : 
						priceType = PriceType.DEALABLE;
						depthLevel = 1;
						break;
					case MDElementName.DEALABLE_PLUS_BID : 
					case MDElementName.DEALABLE_PLUS_OFFER : 
						priceType = PriceType.DEALABLE;
						depthLevel = 2;
						break;
					case MDElementName.DEALABLE_OUTSIDE_BID : 
					case MDElementName.DEALABLE_OUTSIDE_OFFER : 
						priceType = PriceType.DEALABLE;
						depthLevel = 3;		
						break;
					case MDElementName.LOCAL_BID :
					case MDElementName.LOCAL_OFFER : 
						priceType = PriceType.LOCAL;
						break;
					case MDElementName.DEALABLE_REGULAR_BID : 
					case MDElementName.DEALABLE_REGULAR_OFFER : 
						priceType = PriceType.DEALABLE;
						break;
					default : 
						throw new TypeConverter.ConversionError("Can't convert quickfix.field.MDElementName." + elementName);
				}
			}

			public PriceType getPriceType()
			{
				return priceType;
			}

			public int getDepthLevel()
			{
				return depthLevel;
			}
		}
		
		public static ConvertedMDElementName toPriceTypeAndLevel(MDElementName elementName) throws ConversionError
		{
			return new ConvertedMDElementName(elementName);
		}

		public static MDItemType toMdItemType(MDEntryType entryType, MDElementName elementName) throws ConversionError
		{
			switch (entryType.getValue())
			{
				case MDEntryType.BID:
					return MDItemType.BID;
				case MDEntryType.OFFER:
					return MDItemType.OFFER;
				case MDEntryType.TRADE:
					switch (elementName.getValue())
					{
						case MDElementName.PAID:
							return MDItemType.PAID;
						case MDElementName.GIVEN:
							return MDItemType.GIVEN;
						default:								
					}
				default:
					throw new TypeConverter.ConversionError("Can't convert quickfix.field.MDEntryType." + entryType);
			}
		}
		
		public static MDItemType toMdItemType(MDEntryType entryType) throws ConversionError
		{
			switch (entryType.getValue())
			{
				case MDEntryType.BID:
					return MDItemType.BID;
				case MDEntryType.OFFER:
					return MDItemType.OFFER;
				case MDEntryType.EMPTY_BOOK:
					return MDItemType.EMPTY;
				case MDEntryType.TRADE:
					throw new TypeConverter.ConversionError("Can't convert quickfix.field.MDEntryType." + entryType + ". Use toMdItemType(MDEntryType, MDElementName) method.");
				default:
					throw new TypeConverter.ConversionError("Can't convert quickfix.field.MDEntryType." + entryType);
			}
		}
	}
		
	@SuppressWarnings("unused")
	private static class MessageBuffer extends NativeSubscriberBuffer
	{
		private final SessionID sessionId;
		private final QFixMarketAdapter messageProcessor;
		
		public MessageBuffer(String name, QFixMarketAdapter messageProcessor, SessionID sessionId)
		{
			super(name, createSubscriber(sessionId, messageProcessor));
			this.sessionId = sessionId;	
			this.messageProcessor = messageProcessor;
		}		

		public SessionID getSessionId()
		{
			return this.sessionId;
		}

		public QFixMarketAdapter getMessageProcessor()
		{
			return this.messageProcessor;
		}
		
		private static Subscriber<Object> createSubscriber(final SessionID sessionId, final QFixMarketAdapter messageProcessor)
		{
			return new Subscriber<Object>() {
				
				@Override
				public void sendUpdate(Object[] data)
				{
					for (Object o : data)
						sendUpdate(o);					
				}
				
				@Override
				public void sendUpdate(Object data)
				{
					try
					{
						messageProcessor.responseHandler((Message)data, sessionId);
					}
					catch (MarketAdapterException e)
					{
						BaseMarketAdapter.getLogger().error("error during response handling", e);
					}					
				}
			};
		}
	}
	
	private final SocketInitiator initiator;

	//private Map<SessionID, MessageBuffer> messageBuffers = new HashMap<SessionID, QFixMarketAdapter.MessageBuffer>();
	
	public QFixMarketAdapter(SessionSettings sessionSettings,
			CommandFactory commandFactory, boolean logFIXMessages)
			throws ConfigError
	{
		super(commandFactory);
		if (sessionSettings.size() > 1)
			throw new ConfigError("Supported only one session");

		try
		{
			if (sessionSettings.isSetting("DataDictionary")) 			
				sessionSettings.setString("DataDictionary", 
						QuantfabricRuntime.getAbsolutePath(sessionSettings.getString("DataDictionary")));
	
			if (sessionSettings.isSetting("FileLogPath")) 			
				sessionSettings.setString("FileLogPath", 
						QuantfabricRuntime.getAbsolutePath(sessionSettings.getString("FileLogPath")));
			
			if (sessionSettings.isSetting("FileStorePath")) 			
				sessionSettings.setString("FileStorePath", 
						QuantfabricRuntime.getAbsolutePath(sessionSettings.getString("FileStorePath")));
			
			if (sessionSettings.isSetting("SocketKeyStore")) 			
				sessionSettings.setString("SocketKeyStore", 
						QuantfabricRuntime.getAbsolutePath(sessionSettings.getString("SocketKeyStore")));
			
		}
		catch(Exception e)
		{
			throw new ConfigError(e);
		}
		FileStoreFactory storeFactory = new FileStoreFactory(sessionSettings);
		MessageFactory messageFactory = new DefaultMessageFactory();

		LogFactory logFactory = null;
		if (logFIXMessages)
		{
			logFactory = new FileLogFactory(sessionSettings);			
		}
		else
		{
			logFactory = new ScreenLogFactory(false, false, false);
		}			
			
		initiator = new SocketInitiator(this, storeFactory, sessionSettings,
				logFactory, messageFactory);
	}


	@Override
	public String getIdentifier()
	{
		StringBuffer identifier = new StringBuffer(); 
		try
		{
			identifier.append(getVenueName() + ";"); 
			identifier.append(initiator.getSettings().getString("SocketConnectHost"));
			identifier.append(":" + initiator.getSettings().getString("SocketConnectPort") + ";");
			identifier.append(initiator.getSettings().getString(SessionSettings.SENDERCOMPID));
		}
		catch (Exception e)
		{
			getLogger().warn("can't to compose all identifier info");
		}
		return identifier.toString();
	}



	protected Session getSession()
	{
		return Session.lookupSession(initiator.getSessions().get(0));
	}

	protected SessionSettings getSettings()
	{
		return initiator.getSettings();
	}

	protected MessageFactory getMessageFactory()
	{
		return getSession().getMessageFactory();
	}

	public void start()
	{
		try
		{
			initiator.start();
		}
		catch (RuntimeError e)
		{
			getLogger().error("start fail", e);
		}
		catch (ConfigError e)
		{
			getLogger().error("start fail", e);
		}
	}

	@Override
	public void setPassword(String password) throws MarketAdapterException
	{
		initiator.getSettings().setString("Password", password);
	}
	
	public void sendMessage(Message message)
	{
		getSession().send(message);
	}
	
	protected long getMessageId(Message message) throws FieldNotFound
	{
		return message.getHeader().getInt(MsgSeqNum.FIELD);
	}
	
	protected Date getSourceTimestamp(Message message) throws FieldNotFound
	{
		return java.util.Date.from(message.getHeader()
				.getUtcTimeStamp(SendingTime.FIELD)
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}
	
	protected int getItemCount(Message message) throws FieldNotFound
	{
		return message.getInt(NoMDEntries.FIELD);
	}
	
	protected String getSourceName()
	{
		return getVenueName();
	}
	
	protected void messageProcessed(Message message, long messageInTimestamp) throws FieldNotFound, PublisherException
	{
		if (message.getHeader().isSetField(MsgType.FIELD))
		{
			String msgType = message.getHeader().getString(MsgType.FIELD);
							
			if (msgType.equals(MsgType.MARKET_DATA_INCREMENTAL_REFRESH)) 
			{
				EndUpdate endUpdate = 
					new EndUpdate(getMessageId(message), MDMessageType.INCREMENTAL_REFRESH,
						getSourceName(), getSourceTimestamp(message), getItemCount(message));

				publish(endUpdate);
			}
			if (msgType.equals(MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH))
			{
				EndUpdate endUpdate = new EndUpdate(getMessageId(message), MDMessageType.SNAPSHOT,
						getSourceName(), getSourceTimestamp(message), getItemCount(message));
		
				publish(endUpdate);
			}
		}
	}
	
	protected void stop()
	{
		initiator.stop();
	}

	protected void stop(boolean forceDisconnect)
	{
		initiator.stop(forceDisconnect);
	}

	@Override
	public void logon()
	{
		start();
		getSession().logon();
	}

	@Override
	public void logout()
	{
		stop();
	}

	@Override
	public AdapterStatus getStatus()
	{
		return initiator.isLoggedOn() ? AdapterStatus.CONNECTED : AdapterStatus.DISCONNECTED;
	}

	private long calcLatencyOffset(Message message) throws FieldNotFound
	{
		return MDEvent.getCurrentTime() - getSourceTimestamp(message).getTime();
	}
	
	private void responseHandler(Message message, SessionID sessionID) throws MarketAdapterException
	{
		try
		{			
			long messageInTimestamp = System.nanoTime();
			
			if (message.getHeader().isSetField(MsgType.FIELD))
			{
				String msgType = message.getHeader().getString(MsgType.FIELD);
				
				if (msgType.equals(MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH))
				{
					String symbol = message.getString(Symbol.FIELD);
					Feed feed = getFeedProvider().getMarketDataFeed(symbol);
					
					publish(new NewSnapshot(getMessageId(message), getSourceName(), 
							getSourceTimestamp(message), getItemCount(message), 
							symbol, feed.getFeedId(), feed.getFeedName().getName()));
				}
				
				if (msgType.equals(MsgType.HEARTBEAT))
					setMarketLatencyOffset(calcLatencyOffset(message));
			}
			
			if (responseProcessor(message, sessionID)) 
				messageProcessed(message, messageInTimestamp);
			
		}
		catch (FieldNotFound e)
		{
			getLogger().error("FIX message caused an exception: " + message);
			throw new MarketAdapterException(e);
		}
		catch (PublisherException e)
		{
			getLogger().error("FIX message caused an exception: " + message);
			throw new MarketAdapterException(e);
		}
		catch (Exception e) {
			getLogger().error("FIX message caused an exception: " + message);
			getLogger().error("Exception: " + e);
		}
	}
	
	protected abstract boolean responseProcessor(Message message,
			SessionID sessionID) throws MarketAdapterException;

	@Override
	public void fromApp(Message arg0, SessionID arg1)
	{
		/*if (messageBuffers.containsKey(arg1))
			messageBuffers.get(arg1).sendUpdate(arg0);
		else	*/
			try
			{
				responseHandler(arg0, arg1);
			}
			catch (MarketAdapterException e)
			{
				getLogger().error("error during response handling", e);
			}
	}

	@Override
	public void fromAdmin(Message arg0, SessionID arg1)
	{
		/*if (messageBuffers.containsKey(arg1))
			messageBuffers.get(arg1).sendUpdate(arg0);
		else	*/	
			try
			{
				responseHandler(arg0, arg1);
			}
			catch (MarketAdapterException e)
			{
				getLogger().error("error during response handling", e);
			}
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1)
	{
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend
	{
	}

	@Override
	public void onCreate(SessionID arg0)
	{
	}

	@Override
	public void onLogon(SessionID arg0)
	{	
		/*if (!messageBuffers.containsKey(arg0))
			messageBuffers.put(arg0, new MessageBuffer("MessageBuffer-" + getSourceName() + "-" + SessionId.getSessionID(), this, arg0));
		
		messageBuffers.get(arg0).start();*/
		
		invokeLogonListenersByLoggedIn();
	}

	@Override
	public void onLogout(SessionID arg0)
	{
		invokeLogonListenersByLogout();
		
		/*if (messageBuffers.containsKey(arg0))
		{
			messageBuffers.get(arg0).stop();
			messageBuffers.remove(arg0).dispose();
		}*/
	}
}
