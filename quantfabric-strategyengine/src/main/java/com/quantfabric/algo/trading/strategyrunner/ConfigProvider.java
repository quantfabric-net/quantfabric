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
package com.quantfabric.algo.trading.strategyrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.algo.market.dataprovider.AbstractDataViewRequest;
import com.quantfabric.algo.market.dataprovider.HistoricalDataViewRequestImpl;
import com.quantfabric.algo.market.dataprovider.ProductRequest;
import com.quantfabric.algo.market.dataprovider.QueryDataViewRequestImpl;
import com.quantfabric.algo.runtime.QuantfabricRuntime;
import com.quantfabric.algo.trading.strategy.DataSink;
import com.quantfabric.algo.trading.strategy.DataSinkImpl;
import com.quantfabric.algo.trading.strategy.ExecutionPoint;
import com.quantfabric.algo.trading.strategy.ExecutionPointImpl;
import com.quantfabric.algo.trading.strategy.StrategyConfiguration;
import com.quantfabric.algo.trading.strategy.StrategyDefinition;
import com.quantfabric.algo.trading.strategy.StrategyEpStatement;
import com.quantfabric.algo.trading.strategy.TradingStrategy;
import com.quantfabric.algo.trading.strategy.settings.StrategySetting;
import com.quantfabric.algo.trading.strategy.settings.viewlayout.LayoutDefinition;
import com.quantfabric.algo.trading.strategy.settings.viewlayout.LayoutDocumentGenerator;
import com.quantfabric.algo.trading.strategy.settings.viewlayout.ParametersListView;
import com.quantfabric.algo.trading.strategy.settings.viewlayout.ParametersMatrixView;
import com.quantfabric.cep.StatementDefinitionImpl.PersistModes;
import com.quantfabric.cep.QuantfabricCEPException;
import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.ConfigurationException;
import com.quantfabric.util.NodeListIterator;
import com.quantfabric.util.PropertiesViewer.NotSpecifiedProperty;
import com.quantfabric.util.XMLConfigParser;

public class ConfigProvider
{
	private static final Logger log = LoggerFactory.getLogger(ConfigProvider.class);

	protected static void doConfigure(StrategyLoadRunner configuration,
			InputStream stream, String resourceName) throws Exception
	{
		Document document = getDocument(stream, resourceName);
		doConfigure(configuration, document);
	}

	protected static Document getDocument(InputStream stream,
			String resourceName) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		Document document = null;

		try
		{
			builder = factory.newDocumentBuilder();
			document = builder.parse(stream);
		}
		catch (ParserConfigurationException ex)
		{
			throw new Exception("Could not get a DOM parser configuration: "
					+ resourceName, ex);
		}
		catch (SAXException ex)
		{
			throw new Exception("Could not parse configuration: "
					+ resourceName, ex);
		}
		catch (IOException ex)
		{
			throw new Exception(
					"Could not read configuration: " + resourceName, ex);
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException ioe)
			{
				ConfigProvider.log.warn("could not close input stream for: "
						+ resourceName, ioe);
			}
		}
		
		return document;
	}

	/**
	 * Parse the W3C DOM document.
	 * 
	 * @param configuration
	 *            is the configuration object to populate
	 * @param doc
	 *            to parse
	 */
	protected static void doConfigure(StrategyLoadRunner configuration,
			Document doc)
	{
		Element root = doc.getDocumentElement();

		DOMElementIterator strategyRunnerNodeIterator = new DOMElementIterator(
				root.getChildNodes());
		while (strategyRunnerNodeIterator.hasNext())
		{
			Element element = strategyRunnerNodeIterator.next();
			String nodeName = element.getNodeName();

			if (nodeName.equals("global_settings"))
			{
				handleGlobalSettings(configuration, element);
			}
			else if (nodeName.equals("trading_strategies"))
			{
				handleTradingStrategies(configuration, element);
			}
			else if(nodeName.equals("default-persister-settings")) 
            {
            	for (PersistingUpdateListenerConfig persisterConfig : 
            		XMLConfigParser.parseEsperPersisterSettings(element))
            	{
            		configuration.addDefaultCepPersisterConfig(persisterConfig);
            	}
            }
		}
	}

	private static void handleTradingStrategies(
			StrategyLoadRunner configuration, Element parentElement)
	{

		DOMElementIterator strategyNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (strategyNodeIterator.hasNext())
		{

			Element element = strategyNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("trading_strategy"))
			{
				boolean isAutoRun = StrategyDefinition.DEFAULT_ENABLE;
				
				Node autoStartNode = element.getAttributes().getNamedItem("autoRun");
				if (autoStartNode !=null)
					isAutoRun = Boolean.parseBoolean(autoStartNode.getTextContent());
				
				int pushStrategyDataOnPort = 0;
				Node pushStrategyDataOnPortNode = element.getAttributes().getNamedItem("pushStrategyDataOnPort");
				if (pushStrategyDataOnPortNode != null)
					pushStrategyDataOnPort = Integer.parseInt(pushStrategyDataOnPortNode.getTextContent());
				
				String[] persisterNames = new String[]{};
				Node persisterNamesNode = element.getAttributes().getNamedItem("persisters");
				if (persisterNamesNode != null)
				{
					String persistersStr =  persisterNamesNode.getTextContent();
					persisterNames = persistersStr.split("; |;");
				}
				
				Node stategyNode = element.getAttributes().getNamedItem("file");
				if (stategyNode != null)
				{					
					String configFile = stategyNode.getTextContent();
					if (configFile != "")
						try
						{
							configureStrategy(configuration, configFile, isAutoRun, pushStrategyDataOnPort, persisterNames);
						}
						catch (FileNotFoundException e)
						{
							log.error("Can't find strategy configuration file:"
									+ configFile, e);
						}
						catch (Exception e)
						{
							log.error("error load strategy configuration", e);							
						}
				}
				else
					try
					{
						handleTradingStrategy(configuration, element, isAutoRun, pushStrategyDataOnPort, persisterNames);
					}
					catch (Throwable e)
					{
						log.error("error load strategy configuration", e);	
					}
			}
		}
	}

	public static TradingStrategy configureStrategy(StrategyLoadRunner configuration,
			String configFile, boolean isAutoStart, int pushStrategyDataOnPort, String[] persisterNames) throws Exception
	{
		if (!new File(QuantfabricRuntime.getAbsolutePath(configFile)).exists())
			throw new FileNotFoundException("Can't find strategy file (" + configFile + ")");
		
		Document document = getDocument(
				new FileInputStream(QuantfabricRuntime.getAbsolutePath(configFile)),
				configFile);
		Element root = document.getDocumentElement();
		
				
		TradingStrategy tradingStrategy =
				handleTradingStrategy(configuration, root, isAutoStart, pushStrategyDataOnPort, persisterNames);
		
		StrategyLoadContext strategyLoadContext= new StrategyLoadContext(
				tradingStrategy.getName(),
				configFile,
				isAutoStart,
				pushStrategyDataOnPort,
				persisterNames);
		
		configuration.setStrategyLoadContext(tradingStrategy.getName(), strategyLoadContext);
		
		return tradingStrategy;
	}

	private static class StrategyContent
	{
		private Configuration config = new Configuration();
		private LayoutDefinition layoutDefinition = new LayoutDefinition();
		
		public Configuration getConfig()
		{
			return config;
		}
		public void setConfig(Configuration config)
		{
			this.config = config;
		}
		public LayoutDefinition getLayoutDefinition()
		{
			return layoutDefinition;
		}
		public void setLayoutDefinition(LayoutDefinition layoutDefinition)
		{
			this.layoutDefinition = layoutDefinition;
		}
	}
	
	private static TradingStrategy handleTradingStrategy(StrategyLoadRunner configuration,
			Element element, boolean isAutoRun, int pushStrategyDataOnPort, String[] persisterNames) throws Exception
	{
		
		Collection<PersistingUpdateListenerConfig> configs = configuration.getDefaultCepPersisterConfigs();
		
		boolean persistersExists = false;
		
		for (int i = 0; i < persisterNames.length; i++) 
			for (PersistingUpdateListenerConfig config : configs) {
				if (config.getName().equals(persisterNames[i])) {
					persistersExists = true;
					break;
				}
			}
		
		if (!persistersExists)
			throw new Exception("Can't load persisters (" + ArrayUtils.toString(persisterNames) + ")");

		String name = QuantfabricRuntime.sabstituteVariableValues(element.getAttributes().getNamedItem("name")
				.getTextContent());
		boolean enabled = Boolean.parseBoolean(element.getAttributes()
				.getNamedItem("enabled").getTextContent());
		String klass = element.getAttributes().getNamedItem("class")
				.getTextContent();

		LinkedList<String[]> settings = new LinkedList<String[]>();
		List<DataSink> sinks = new ArrayList<DataSink>();
		List<ExecutionPoint> ex_points = new ArrayList<ExecutionPoint>();
		List<StrategyEpStatement> stmts = new ArrayList<StrategyEpStatement>();

		StrategyContent strategyContent = new StrategyContent();		
		handleStategyContent(element, settings, sinks, ex_points, stmts, strategyContent);
				
		if (pushStrategyDataOnPort != 0)
			configuration.setPortForStrategyData(name, pushStrategyDataOnPort);
		
		for (String persisterName : persisterNames)
			configuration.addPersisterNameForStrategy(name, persisterName);		
		
		StrategyConfiguration strategy = (StrategyConfiguration) StrategyFactory
				.create(name, configuration, klass, strategyContent.getConfig());
		
		if (strategy == null)
			throw new Exception("Strtategy \"" + name + "\" isn't created.");
		
		for (String[] setProp : settings)
		{
			strategy.addSetting(setProp[0], setProp[1], setProp[2], setProp[3], setProp[4], setProp[5], setProp[6], setProp[7], setProp[8]);
		}
		for (DataSink sink : sinks)
		{
			strategy.addDataSink(sink);
		}
		for (ExecutionPoint pont : ex_points)
		{
			strategy.addExecutionPoint(pont);
		}
		for (StrategyEpStatement stmt : stmts)
		{
			try
			{
				strategy.addStatement(stmt);
			}
			catch (QuantfabricCEPException e)
			{
				log.error("Add statement failed.", e);
			}
		}
		
		TradingStrategy tradingStrategy = (TradingStrategy) strategy;
		LayoutDocumentGenerator docGenerator = new LayoutDocumentGenerator(strategyContent.getLayoutDefinition(), tradingStrategy.getSettings());
		tradingStrategy.setLayoutDefinitionProvider(docGenerator);
		configuration.loadStrategy(tradingStrategy, enabled);
		configuration.setAutoRunForStrategy(tradingStrategy.getName(), isAutoRun);
		
		return tradingStrategy;
	}

	private static void handleStategyContent(Element element, LinkedList<String[]> settings,
			List<DataSink> sinks, List<ExecutionPoint> ex_points,
			List<StrategyEpStatement> stmts, StrategyContent strategyContent) throws Exception
	{
		DOMElementIterator nodeIterator = new DOMElementIterator(
				element.getChildNodes());

		while (nodeIterator.hasNext())
		{
			Element subElement = nodeIterator.next();
			String nodeName = subElement.getNodeName();

			if (nodeName.equals("external"))
			{
				String externalFile = subElement.getAttributes().getNamedItem("file").getTextContent();				
				Document document = getDocument(
						new FileInputStream(QuantfabricRuntime.getAbsolutePath(externalFile)),
						externalFile);
				Element root = document.getDocumentElement();
				
				handleStategyContent(root, settings, sinks, ex_points, stmts, strategyContent);	
				
				log.info(String.format("External file(%s) handled", externalFile));
			}
			
			if (nodeName.equals("strategy_settings"))
			{
				handleSettings(settings, subElement, 
						StrategySetting.DEFAULT_REGION_NAME, 
						StrategySetting.DEFAULT_PARAMETERS_VIEW_ID, 
						StrategySetting.DEFAULT_GROUP_ID);
			}
			else if (nodeName.equals("data_sinks"))
			{
				handleDataSinks(sinks, subElement);
			}
			else if (nodeName.equals("execution_points"))
			{
				handleExecutionPoints(ex_points, subElement);
			}
			else if (nodeName.equals("ep_statements"))
			{
				handleEpStatements(stmts, subElement);
			}
			else if (nodeName.equals("esper-configuration"))
			{
				// Create new xml document from the element
				DocumentBuilder docBuilder = DocumentBuilderFactory
						.newInstance().newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				Node nd = doc.importNode(subElement, true);
				doc.appendChild(nd);
				// load cep configuration
				strategyContent.setConfig(new Configuration().configure(doc));
			}
			else if (nodeName.equals("settingsGUILayout"))
			{
				strategyContent.setLayoutDefinition(getParsedSettingsLayout(subElement));
			}
		}		
	}

	private static LayoutDefinition getParsedSettingsLayout(Element parentElement) {
		
		LayoutDefinition layoutDefinition = new LayoutDefinition();
		
		DOMElementIterator parametersViewNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (parametersViewNodeIterator.hasNext())
		{
			Element element = parametersViewNodeIterator.next();
			String nodeName = element.getNodeName();
			if(nodeName == "parametersMatrix")
			{
				String viewId = element.getAttribute("id");
				if(!layoutDefinition.containsParametersView(viewId))
				{
					String viewName = element.getAttribute("name"); 
					ParametersMatrixView view = new ParametersMatrixView(viewName, viewId, "parametersMatrix");
					DOMElementIterator parametersMatrixNodeIterator = new DOMElementIterator(
							element.getChildNodes());
					while (parametersMatrixNodeIterator.hasNext())
					{
						Element matrixElement = parametersMatrixNodeIterator.next();
						String matrixElementNodeName = matrixElement.getNodeName();
						if(matrixElementNodeName == "columns")
						{
							DOMElementIterator columnsMatrixNodeIterator = new DOMElementIterator(
									matrixElement.getChildNodes());
							while (columnsMatrixNodeIterator.hasNext())
							{
								Element columnElement = columnsMatrixNodeIterator.next();
								String columnElementName = columnElement.getNodeName();
								if(columnElementName == "column")
								{
									String groupId = columnElement.getAttribute("groupId");
									String columnName = columnElement.getAttribute("header");
									view.addGroup(groupId, columnName);
								}
							}
						}
					}
					layoutDefinition.addParameterView(viewId, view);
				}
			}
			if(nodeName == "parametersList")
			{
				String viewId = element.getAttribute("id");
				if(!layoutDefinition.containsParametersView(viewId))
				{
					String viewName = element.getAttribute("name");
					String columnName = element.getAttribute("valueColumnHeader");
					ParametersListView view = new ParametersListView(viewName, viewId, columnName, "parametersList");
					layoutDefinition.addParameterView(viewId, view);
				}
			}
		}
		return layoutDefinition; 
	}

	private static void handleGlobalSettings(StrategyLoadRunner configuration,
			Element parentElement)
	{
		Map<String, String> globalSettings = new HashMap<String, String>();
		DOMElementIterator settingsNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (settingsNodeIterator.hasNext())
		{
			Element element = settingsNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("setting"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String value = element.getAttributes().getNamedItem("value")
						.getTextContent();
				globalSettings.put(name, value);
			}
		}

		configuration.setGlobalSettings(globalSettings);
	}

	private static void handleDataSinks(List<DataSink> sinks,
			Element parentElement)
	{
		DOMElementIterator dataSinkNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (dataSinkNodeIterator.hasNext())
		{
			Element element = dataSinkNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("data_sink"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String pipeline = element.getAttributes()
						.getNamedItem("pipeline").getTextContent();
				
				DataSink datasink = new DataSinkImpl(name, pipeline, null);

				DOMElementIterator nodeIterator = new DOMElementIterator(element.getChildNodes());

				while (nodeIterator.hasNext())
				{
					Element subElement = nodeIterator.next();
					
					if (subElement.getNodeName().equals("observation"))
					{
						String type = subElement.getAttributes().getNamedItem("type")
								.getTextContent();
						
						NodeListIterator observationNodeIterator = new NodeListIterator(subElement.getChildNodes());
						
						Set<String> dependences = new HashSet<String>();
						Map<String, String> parameters = new HashMap<String, String>();
						
						while (observationNodeIterator.hasNext())
						{							
							Node observationSubNode = observationNodeIterator.next();
						
							if (observationSubNode.getNodeName().equals("dependences"))
								AbstractDataViewRequest.parseDependences(dependences, observationSubNode);
							else
								if (observationSubNode.getNodeName().equals("parameters"))
									AbstractDataViewRequest.parseValuedParameters(
											parameters, observationSubNode);
						}
																		
						if (type.equals("QueryDataViewRequest"))
						{
							datasink.setObservation(
									QueryDataViewRequestImpl.fromXML(
											subElement, dependences, parameters));
						}
						
						if (type.equals("HistoricalDataViewRequest"))
						{
							datasink.setObservation(
									HistoricalDataViewRequestImpl.fromXML(
											subElement, dependences,parameters));
						}
						
						if (type.equals("ProductRequest")) {
							try {
								datasink.setObservation(ProductRequest.fromXML(subElement, dependences, parameters));
							}
							catch (NotSpecifiedProperty e) {
								throw new ConfigurationException("Error creating ProductRequest.", e);
							}
						}
					}
				}

				log.debug("Parsed DataViewRequest - " + datasink.getObservation());
				
				sinks.add(datasink);
			}
		}
	}
	
	private static void handleExecutionPoints(List<ExecutionPoint> ex_points,
			Element parentElement)
	{
		DOMElementIterator executionPointNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (executionPointNodeIterator.hasNext())
		{
			Element element = executionPointNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("execution_point"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String connection = element.getAttributes()
						.getNamedItem("connection").getTextContent();
				
				
				Map<String, String> executionSettings = new HashMap<String, String>(); 
				String fillCheck = null;
				
				Node node = element.getAttributes().getNamedItem("fillCheck");
				
				if (node != null)
					fillCheck = node.getTextContent();
				
				executionSettings.put("fillCheck", fillCheck);
				ex_points.add(new ExecutionPointImpl(name, connection, executionSettings));
			}
		}
	}

		
	private static void handleSettings(List<String[]> settings,
			Element parentElement, String regionName, String defaultParemetersViewId, String defaultGroupId) throws Exception
	{
		String lastParsedRegion = regionName;
		
		DOMElementIterator settingsNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (settingsNodeIterator.hasNext())
		{				
			Element element = settingsNodeIterator.next();
			
			String paremetersViewId = defaultParemetersViewId;
			String groupId = defaultGroupId;	
			
			Node paremetersViewIdNode = element.getAttributes().getNamedItem("parametersViewId");
			if (paremetersViewIdNode != null)
				paremetersViewId = paremetersViewIdNode.getTextContent();
			
			Node groupIdNode = element.getAttributes().getNamedItem("groupId");
			if (groupIdNode != null)
				groupId = groupIdNode.getTextContent();
			
			String nodeName = element.getNodeName();
			if (nodeName.equals("setting"))
			{			
				String[] set = new String[9];
				// String name
				set[0] = element.getAttributes().getNamedItem("name")
						.getTextContent();
				
				String dispayName = set[0];
				
				Node displayNameNode = element.getAttributes().getNamedItem("displayName");
				if (displayNameNode != null)
					dispayName = displayNameNode.getTextContent();
				
				// String value
				set[1] = element.getAttributes().getNamedItem("value")
						.getTextContent();
				// String type =
				set[2] = element.getAttributes().getNamedItem("type")
						.getTextContent();
				
				Node settingScope = element.getAttributes().getNamedItem("scope");
				if (settingScope != null)
				{
					set[3] = settingScope.getTextContent().toLowerCase();
				}
				else
					set[3] = "public";
				
				Node settingModificationMode = element.getAttributes().getNamedItem("modificationMode");
				if (settingModificationMode != null)
				{
					set[4] = settingModificationMode.getTextContent().toLowerCase();
				}
				else
					set[4] = "any";
				
				set[5] = regionName;
				set[6] = dispayName;
								
				set[7] = paremetersViewId;
				set[8] = groupId;
				
				settings.add(set);
				
				
			}
			else
				if (nodeName.equals("region"))
				{					
					Node subRegionNameNode = element.getAttributes().getNamedItem("name");

					if (subRegionNameNode != null)
					{
						lastParsedRegion = subRegionNameNode.getTextContent();
						handleSettings(settings, element, lastParsedRegion, paremetersViewId, groupId);
					}
					else
						throw new Exception("'region' node must have attribute 'name', last succeeded region - " + lastParsedRegion);						
				}
		}
	}

	private static void handleEpStatements(List<StrategyEpStatement> stmts,
			Element parentElement)
	{
		DOMElementIterator statementNodeIterator = new DOMElementIterator(
				parentElement.getChildNodes());
		while (statementNodeIterator.hasNext())
		{
			Element element = statementNodeIterator.next();
			String nodeName = element.getNodeName();
			if (nodeName.equals("statement"))
			{
				String name = element.getAttributes().getNamedItem("name")
						.getTextContent();
				String statement = element.getAttributes()
						.getNamedItem("statement").getTextContent();
				boolean debugMode = Boolean.parseBoolean(element
						.getAttributes().getNamedItem("debugMode")
						.getTextContent());
				boolean execInvoker = Boolean.parseBoolean(element
						.getAttributes().getNamedItem("execInvoker")
						.getTextContent());
				
				boolean contextCreator = false;
				if (element.getAttributes().getNamedItem("contextCreator") != null)
					contextCreator = Boolean.parseBoolean(element.getAttributes().getNamedItem("contextCreator").getTextContent());

				Node persistModeNode = element.getAttributes().getNamedItem(
						"persistMode");

				PersistModes persistMode = PersistModes.NONE;

				if (persistModeNode != null)
				{
					persistMode = PersistModes.valueOf(persistModeNode
							.getTextContent().toUpperCase());
				}

				StrategyEpStatement epStmt = new StrategyEpStatement(name,
						statement, persistMode, execInvoker, debugMode, contextCreator);

				stmts.add(epStmt);
			}
			else
				if (nodeName.equals("region"))
				{
					handleEpStatements(stmts, element);
				}
		}
	}

	
	public static void createPersistersFromXML(StrategyLoadRunner strategyRunner, String filePath) throws Exception {
		
		Document doc = getDocument(new FileInputStream(QuantfabricRuntime.getAbsolutePath(filePath)),
				filePath);
		
		Element root = doc.getDocumentElement();

		DOMElementIterator strategyRunnerNodeIterator = new DOMElementIterator(
				root.getChildNodes());
		
		StringBuilder persisters = new StringBuilder();
		
		while (strategyRunnerNodeIterator.hasNext())
		{
			Element element = strategyRunnerNodeIterator.next();
			String nodeName = element.getNodeName();
			
			if(nodeName.equals("persister-settings")) 
            {
            	for (PersistingUpdateListenerConfig persisterConfig : 
            		XMLConfigParser.parseEsperPersisterSettings(element))
            	{
            		strategyRunner.addDefaultCepPersisterConfig(persisterConfig);
            		persisters.append(persisterConfig.getName() + ";");
            	}
            }
		}
		
		if(';'== persisters.charAt(persisters.length() - 1) )
			persisters.deleteCharAt(persisters.length() - 1);
		
		log.info("created persisters [" + persisters + "]");
	}
}
