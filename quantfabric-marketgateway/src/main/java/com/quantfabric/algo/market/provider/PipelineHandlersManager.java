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
package com.quantfabric.algo.market.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.map.MapEventType;
import com.quantfabric.cep.ICEPProvider;
import com.quantfabric.cep.StatementDefinition;
import com.quantfabric.cep.StatementDefinitionImpl;
import com.quantfabric.messaging.NamedMapSubscriber;


class PipelineHandlersManager implements StatementProvider {
	
	private static final Logger log = LoggerFactory.getLogger(PipelineHandlersManager.class);
	private class PipelineDataHandler implements UpdateListener
	{
		private final ICEPProvider cep;
		private final EPStatement statement;
		private final String name;
		private boolean isMapEventType;
		private String eventTypeName;
		private final boolean isPermanent=true;
		private final List<NamedMapSubscriber<Object>> destributionList = new ArrayList<NamedMapSubscriber<Object>>();
		
		@SuppressWarnings("unused")
		public ICEPProvider getCep()
		{
			return cep;
		}
		
		/**
		 * @return the isPermanent
		 */
		@SuppressWarnings("unused")
		public boolean isPermanent() {
			return isPermanent;
		}
		/**
		 * @return the name
		 */
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		/**
		 * @return the statement
		 */
		@SuppressWarnings("unused")
		public EPStatement getStatement() {
			return statement;
		}
		
		public void attachSubscriber(NamedMapSubscriber<Object> subscriber){
			if(destributionList.size() < 1) {
				statement.addListener(this);
		        
			}
			destributionList.add(subscriber);
		}
		public void deattachSubscriber(NamedMapSubscriber<Object> subscriber){
			destributionList.remove(subscriber);
			if(destributionList.size() < 1)
				statement.removeListener(this);
		}
		@Override
		public void update(EventBean[] newEvents, EventBean[] oldEvents) {
			try {
			if(newEvents != null && newEvents.length>0) {
				if(isMapEventType) {
					
					Map<?, ?>[] events = new HashMap[newEvents.length];
					for (int i = 0; i < newEvents.length; i++)
			        {
						events[i]=(Map<?, ?>)newEvents[i].getUnderlying();
			        }
					for (int j = 0; j < destributionList.size(); j++) {
						destributionList.get(j).update(events,eventTypeName);
					}	
				}
				else {
					Object[] events = new Object[newEvents.length];
					for (int i = 0; i < newEvents.length; i++)
			        {
						events[i]=newEvents[i].getUnderlying();

			        }
					for (int j = 0; j < destributionList.size(); j++) {
						
						//System.out.println(""+ Thread.currentThread().getId()+"  destribution " +((com.quantfabric.algo.market.datamodel.MDQuote) events[0]).getSymbol());
						destributionList.get(j).sendUpdate(events);
 					}
				}
			}
			}
			catch(Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		@SuppressWarnings("unused")
		public void destroy(){
		}
		public PipelineDataHandler(ICEPProvider cep, StatementDefinition stmt) throws Exception {
			this.cep = cep;
			statement = cep.registerStatement(  stmt.getName(),
												stmt.getStatement(),
												stmt.getPersistMode(),
												stmt.getPersisterCustomSettingBlocks(),
												stmt.isDebugMode());
			this.name=statement.getName();
			EventType eventType = statement.getEventType();
			if(eventType instanceof MapEventType ) {
				isMapEventType=true;
				eventTypeName =eventType.getName();	
			}

			log.debug("Create PipelineDataHandler " + stmt.hashCode() + " for stmtname:" + stmt.getName() + " stmt:" + stmt.getStatement() );
		}
	}
	private final Map<Integer,PipelineDataHandler> stmts = new HashMap<Integer, PipelineDataHandler>();
	private final ICEPProvider cep;
	
	public PipelineHandlersManager(ICEPProvider cep){
		this.cep=cep;
	}

	@Override
	public void attachSubscriber(NamedMapSubscriber<Object> subscriber, String statement) throws Exception {
		int key = statement.hashCode();
		if(!stmts.containsKey(key)) {
			stmts.put(key, 
					new PipelineDataHandler(cep,new StatementDefinitionImpl(
														"strategy", 
														statement, 
														false, 
														false)));
		}
		stmts.get(key).attachSubscriber(subscriber);
	}

	@Override
	public void detachSubscriber(NamedMapSubscriber<Object> subscriber, String statement) {
		int key = statement.hashCode();
		if(stmts.containsKey(key))
			stmts.get(key).deattachSubscriber(subscriber);				
	}

	@Override
	public void registerStatement(StatementDefinition statement) throws Exception {
		int key = statement.hashCode();
		if(!stmts.containsKey(key))
			stmts.put(key,new PipelineDataHandler(cep,statement));
	}
}
