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
package com.quantfabric.algo.runtime;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.eventbus.BackTestingEventBus;
import com.quantfabric.util.RunId;
import com.quantfabric.util.SessionId;

public class QuantfabricRuntime implements RuntimeContext {

	private static final String DEFAULT_CONFIG_ROOT = "config";
	private static final Logger log = LoggerFactory.getLogger( QuantfabricRuntime.class );
	private static String rootPath;

	private static final List<ShutdownListener> shutdownListeners = new ArrayList<ShutdownListener>();
	
	private static final BackTestingEventBus globalBackTestingEventBus = new BackTestingEventBus();
	
	public static void addShutdownListener(ShutdownListener listener)
	{
		shutdownListeners.add(listener);
	}
	
	public static BackTestingEventBus getGlobalBackTestingEventBus()
	{
		return globalBackTestingEventBus;
	}
	
	private final Map<String, Object> quantfabricServices = new ConcurrentHashMap<String, Object>();
	private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
	
	
	public Object getProperty(String key) {
		if(properties.containsKey(key))
			return properties.get(key);
		return null;
	}
	
	public static void notifyShutdown()
	{
		for (ShutdownListener sl : shutdownListeners)
			sl.shutdown(SessionId.getSessionID());
	}
	
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	
	public Object getService(String klass) {
		if(quantfabricServices.containsKey(klass))
			return quantfabricServices.get(klass);
		return null;
	}

	
	public Object[] getServices() {
		return quantfabricServices.values().toArray();
	}
	
	
	public void addService(Object service)
	{
		if (service == null){
			throw new IllegalArgumentException("service can't be null");
		}
	    log.info(service.getClass().getName());
	    
	   	synchronized (service)
		{
			String key = service.getClass().getName();
			if(!quantfabricServices.containsKey(key)){
				quantfabricServices.put(key, service);
			}
			if(service instanceof QuantfabricRuntimeService){
				QuantfabricRuntimeService runtimeSrv= (QuantfabricRuntimeService)service;
		    	runtimeSrv.setRuntime(this);
	    	}
	   }
	}
	
	
	public void removeService(Object service)
	{
		if (service == null){
			throw new IllegalArgumentException("service can't be null");
		}
	    log.info(service.getClass().getName());
	    
	   	synchronized (service)
		{
			String key = service.getClass().getName();
			quantfabricServices.remove(key);
		}
	}


	/**
	 * @param rootPath the rootPath to set
	 */
	public static void setRootPath(String rootPath) {
		QuantfabricRuntime.rootPath = rootPath;
	}


	/**
	 * @return the rootPath
	 */
	public static String getRootPath() {
		
		try
		{		
			if(rootPath == null || rootPath.trim().length() == 0)
				return System.getProperty("com.quantfabric.algo.server.rootpath",
						new File(".").getCanonicalPath());			
		}
		catch (IOException e){}
		return rootPath;
	}	
		
	public static String getAbsolutePath(String relativePath)
	{		
		String absPath = null;
		if (relativePath != null)
		{		
			absPath = (getRootPath() + "/" + 
					relativePath).replace("%CONFIG_ROOT%", 
							System.getProperty("com.quantfabric.algo.server.config_root",
							DEFAULT_CONFIG_ROOT));
			
			absPath = absPath.replace("\\", "/").replace("//", "/"); 
		}
		return absPath;
	}
	public static String sabstituteVariableValues(String sourceString)
	{
		if(sourceString != null)
		{
			sourceString = sourceString.replace("%RUN_ID%", String.valueOf(RunId.getInstance().getRunId()));
			sourceString = sourceString.replace("%ROOT_PATH%", getRootPath());
		}			
		return sourceString;
	}
	
	public static String sabstituteVariableValues(String sourceString, Map<String, String> additionalDictionary)
	{
		if(sourceString != null)
		{
			sourceString = sabstituteVariableValues(sourceString);
			for (String varName : additionalDictionary.keySet())
			{
				sourceString = sourceString.replace(varName, additionalDictionary.get(varName));
			}
		}			
		return sourceString;
	}
	
	public static class ApplicationInfo
	{
		public final String appName;
		public final String appVersion;
		public final String appVendor;
		
		public ApplicationInfo(String appName, String appVersion,
				String appVendor)
		{
			super();
			this.appName = appName;
			this.appVersion = appVersion;
			this.appVendor = appVendor;
		}

		@Override
		public String toString()
		{
			return " appName=" + appName + "; appVersion=" + appVersion
					+ "; appVendor=" + appVendor + ";";
		}
	}
	
	private static ApplicationInfo appInfo = 
		new ApplicationInfo("UNKNOWN", "UNKNOWN", "UNKNOWN");
	
	public static ApplicationInfo getAppInfo()
	{
		return appInfo;
	}

	public static void setAppInfo(ApplicationInfo appInfo)
	{
		QuantfabricRuntime.appInfo = appInfo;
	}
}