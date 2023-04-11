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


import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;


public class ExecutionPointImpl implements ExecutionPoint 
{
	private boolean isActive = false;
	private String targetMarket;
	private String connection;
	private final boolean isEmbedded;
	private Map<String, String> executionSettings = new HashMap<String, String>();
	
	public ExecutionPointImpl(){
		this("","",false,false,null);
	}
	
	public ExecutionPointImpl(String market){
		this(market,"",false,false,null);
	}
	public ExecutionPointImpl(String market,String connection){
		this(market,connection,false,false,null);
	}
	
	public ExecutionPointImpl(String market,String connection, Map<String, String> executionSettings){
		this(market,connection,false,false,executionSettings);
	}
	
	
	@ConstructorProperties({"market", "connection", "embedded", "active"})
	private ExecutionPointImpl(String market,String connection,boolean isEmbedded,boolean isActive, 
			Map<String, String> executionSettings){
		this.targetMarket = market;
		this.connection=connection;
		this.isEmbedded=isEmbedded;
		this.isActive =isActive;
		this.setExecutionSettings(executionSettings);
	}
	public String getExecutionSettingByName(String settingName) {
		return executionSettings.get(settingName);
	}
	
	public Map<String, String> getExecutionSettings() {
		
		return this.executionSettings;
	}
	
	public void setExecutionSettingByName(String name, String value) {
		this.executionSettings.put(name, value);
	}

	public void setExecutionSettings(Map<String, String> executionSettings) {
		this.executionSettings = executionSettings;
	}

	/**
	 * @return the targetMarket
	 */
	public String getTargetMarket() {
		return targetMarket;
	}

	/**
	 * @param targetMarket the targetMarket to set
	 */
	public void setTargetMarket(String targetMarket) {
		if(isActive) throw new IllegalStateException("Can not change property in active state");
		this.targetMarket = targetMarket;
	}

	/**
	 * @return the connection
	 */
	public String getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(String connection) {
		if(isActive) throw new IllegalStateException("Can not change property in active state");
		this.connection = connection;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * @return the isEmbedded
	 */
	public boolean isEmbedded() {
		return isEmbedded;
	}
	
	
	public static ExecutionPoint from(CompositeData data) {
		
		final boolean isActive 		= (Boolean)data.get("isActive");
		final String targetMarket	= (String) data.get("targetMarket");
		final String connection		= (String) data.get("connection");
		final boolean isEmbedded 	= (Boolean)data.get("isEmbedded");
		
        return new ExecutionPointImpl(targetMarket,connection,isEmbedded,isActive,null);
	}
	
/*	
	@Override
	public CompositeData toCompositeData(CompositeType ct) {
		 try {
             final String[] itemNames ={"isActive", "targetMarket","connection","isEmbedded"};
             final String[] itemDescriptions = {"isActive", "targetMarket","connection","isEmbedded"};
             final OpenType<?>[] itemTypes ={SimpleType.BOOLEAN,SimpleType.STRING,SimpleType.STRING,SimpleType.BOOLEAN};
            
             
             CompositeType xct =
                 new CompositeType(ct.getTypeName(),
                                   ct.getDescription(),
                                   itemNames,
                                   itemDescriptions,
                                   itemTypes);
             CompositeData cd =
                 new CompositeDataSupport(xct,
                                          new String[] {"isActive", "targetMarket","connection","isEmbedded"},
                                          new Object[] {isActive,targetMarket,connection,isEmbedded});
             assert ct.isValue(cd);  // check we've done it right
             return cd;
         } catch (RuntimeException x) {
             throw x;
         } catch (Exception x) {
             throw new IllegalArgumentException(ct.getTypeName(), x);
         }
	}*/
}

