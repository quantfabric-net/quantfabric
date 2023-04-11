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
package com.quantfabric.algo.market.gateway.test;

import java.util.Properties;

import com.quantfabric.algo.market.gate.MarketGatewayService;
import org.apache.log4j.PropertyConfigurator;

import static com.quantfabric.algo.configuration.QuantfabricConstants.CONFIG_URL;

public class ConfigProviderTest
{
	public static void main(String[] args) throws Exception
	{
		Properties log4jProperties = new Properties();
		log4jProperties.setProperty("log4j.rootLogger", "DEBUG, myConsoleAppender");
		log4jProperties.setProperty("log4j.appender.myConsoleAppender", "org.apache.log4j.ConsoleAppender");
		log4jProperties.setProperty("log4j.appender.Target", "System.out");
		log4jProperties.setProperty("log4j.appender.myConsoleAppender.layout", "org.apache.log4j.PatternLayout");		
		log4jProperties.setProperty("log4j.appender.myConsoleAppender.layout.ConversionPattern", 
				"%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
		
		PropertyConfigurator.configure(log4jProperties);
		
		MarketGatewayService gatewayService = new MarketGatewayService();
		
		System.setProperty("com.quantfabric.algo.server.config_root", "/config/vit/sep_gw/gateway/");
		
		Properties properties = new Properties();
		properties.setProperty(CONFIG_URL, "/config/vit/sep_gw/gateway/quantfabric.gateway.cfg.xml");
		
		gatewayService.configure(properties);
		
		System.in.read();
	}
}
