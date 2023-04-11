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
package com.quantfabric.algo.trading.strategyrunner.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.quantfabric.algo.trading.strategy.DataSink;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalObjectNameUtil {

	public static final Logger log = LoggerFactory.getLogger(InternalObjectNameUtil.class);
 
    public static ObjectName toObjectNameStrategy(String domain, TradingStrategy strategy)
    											throws MalformedObjectNameException
	{
    	
	    String strategyName = strategy.getName();
	    return new ObjectName(domain +
                ":type=AlgoServer,group=services,serviceName=Trading,servicegroup=Strategies,strategyName=" +
                strategyName);
	}

	public static ObjectName toObjectNameDataSink(String domain,TradingStrategy strategy, DataSink sink){
	    String strategyName = strategy.getName();
	    try {
			return new ObjectName(domain +
                    ":type=AlgoServer,group=services,serviceName=Trading,servicegroup=Strategies,strategyName=" +
                    strategyName +
                    ",sink=" +
                    sink.getName());
		} catch (MalformedObjectNameException | NullPointerException e) {
			log.error(e.getMessage());
		}
		return null;
	}
}
