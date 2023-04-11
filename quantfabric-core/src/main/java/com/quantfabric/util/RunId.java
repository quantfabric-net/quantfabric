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
package com.quantfabric.util;
import java.util.prefs.Preferences;

import org.slf4j.LoggerFactory;

import com.quantfabric.algo.backtesting.eventbus.BackTestingEventAdapter;
import com.quantfabric.algo.backtesting.eventbus.events.BackTestingEvent;
import com.quantfabric.algo.runtime.QuantfabricRuntime;

public class RunId {
	private static volatile RunId instance = null;
	private static final String RUN_ID_KEY = "RunId";
	private static final int RUN_ID_INIT = 0;
	private int currentRunId = RUN_ID_INIT;
	 
    private RunId() 
    {   
    	QuantfabricRuntime.getGlobalBackTestingEventBus().attachListener(new BackTestingEventAdapter() {
    		@Override
			public void updateRunId(BackTestingEvent event)
			{
				RunId.this.updateRunId();
			}
		});
    	
    	updateRunId();
    }

    public static RunId getInstance() {
        if (instance == null) {
            synchronized (RunId.class){
                if (instance == null) {
                        instance = new RunId();
                }
          }
        }
        return instance;
    }
    public int getRunId()
    {
    	return currentRunId;
    }
    
    private void updateRunId(){
    	try {
    		Preferences prefs = Preferences.userNodeForPackage(RunId.class);
        	currentRunId = prefs.getInt(RUN_ID_KEY, RUN_ID_INIT);
        	prefs.putInt(RUN_ID_KEY, ++currentRunId);
    		prefs.flush();
		} catch (Exception e) {
			LoggerFactory.getLogger(RunId.class).error("Error during operating with RunId key.", e);
		}
    }
}
