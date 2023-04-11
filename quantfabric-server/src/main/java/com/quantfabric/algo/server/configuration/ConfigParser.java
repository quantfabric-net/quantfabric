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
package com.quantfabric.algo.server.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.quantfabric.persistence.esper.PersistingUpdateListenerConfig;
import com.quantfabric.util.DOMElementIterator;


public class ConfigParser {
    private static final Logger log = LoggerFactory.getLogger(ConfigParser.class);

    protected static void doConfigure(ServiceHostConfiguration configuration, InputStream stream, String resourceName)throws Exception
    {
        Document document = getDocument(stream, resourceName);
        doConfigure(configuration, document);
    }

    protected static Document getDocument(InputStream stream, String resourceName) throws Exception
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
            throw new Exception("Could not get a DOM parser configuration: " + resourceName, ex);
        }
        catch (SAXException ex)
        {
            throw new Exception("Could not parse configuration: " + resourceName, ex);
        }
        catch (IOException ex)
        {
            throw new Exception("Could not read configuration: " + resourceName, ex);
        }
        finally {
            try {
                stream.close();
            }
            catch (IOException ioe) {
                ConfigParser.log.warn( "could not close input stream for: " + resourceName, ioe );
            }
        }

        return document;
    }

    /**
     * Parse the W3C DOM document.
     * @param configuration is the configuration object to populate
     * @param doc to parse
     */
    protected static void doConfigure(ServiceHostConfiguration configuration, Document doc)
    {
        Element root = doc.getDocumentElement();

        DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(root.getChildNodes());
        while (eventTypeNodeIterator.hasNext())
        {
            Element element = eventTypeNodeIterator.next();
            String nodeName = element.getNodeName();
            if (nodeName.equals("quantfabric-services"))
            {
            	handleQuantfabricServices(configuration, element);
            }
            else
            	if(nodeName.equals("quantfabric-default-persister-settings"))
            	{
            		handleQuantfabricDefaultPersisterSettings(configuration, element);
            	}
        }
    }

    private static void handleQuantfabricDefaultPersisterSettings(
			ServiceHostConfiguration configuration, Element parentElement)
	{
    	DOMElementIterator persisterSettingsNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
    	
    	Collection<PersistingUpdateListenerConfig> esperEventPersisterConfigs = 
			new ArrayList<PersistingUpdateListenerConfig>();
    	
    	while (persisterSettingsNodeIterator.hasNext())
    	{
    		Element persisterSettingsElement = persisterSettingsNodeIterator.next();    		
    		
    		if (persisterSettingsElement.getNodeName().equals("esperPersistingUpdateListener"))
    			esperEventPersisterConfigs.add(
    					PersistingUpdateListenerConfig.getFromXML(persisterSettingsElement));
    	}
		
    	if (!esperEventPersisterConfigs.isEmpty())
    		configuration.addContextProperty("esperEventPersisterConfigs", esperEventPersisterConfigs);
	}

	private static void handleQuantfabricServices(ServiceHostConfiguration configuration, Element parentElement)
    {
    	DOMElementIterator eventTypeNodeIterator = new DOMElementIterator(parentElement.getChildNodes());
    	while (eventTypeNodeIterator.hasNext())
    	{
    		Element element = eventTypeNodeIterator.next();
    		String nodeName = element.getNodeName();
    		if (nodeName.equals("quantfabric-service"))
    		{
    			
    			String srvName = element.getAttributes().getNamedItem("name").getTextContent();
    			String srvAlias = element.getAttributes().getNamedItem("alias").getTextContent();
    			String className = element.getAttributes().getNamedItem("class-name").getTextContent();
    			Properties properties = new Properties();
    			DOMElementIterator nodeIterator = new DOMElementIterator(element.getChildNodes());
    			while (nodeIterator.hasNext())
    			{
    				Element subElement = nodeIterator.next();
    				if (subElement.getNodeName().equals("init-arg"))
    				{
    					String name = subElement.getAttributes().getNamedItem("name").getTextContent();
    					String value = subElement.getAttributes().getNamedItem("value").getTextContent();
    					properties.put(name, value);
    				}
    			}
    			configuration.addQuantfabricService(srvName, srvAlias, className, properties);
    		}
    	}  
    }


}
