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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class ServiceHostConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(ServiceHostConfiguration.class );
    private final Map<String, ServiceConfiguration> quantfabricServices= new HashMap<String, ServiceConfiguration>();
    private final Map<String, Object> quantfabricContextProperties = new HashMap<String, Object>();
   
    public Map<String, ServiceConfiguration> getServicesConfigurations()
    {
        return quantfabricServices;
    }
    
    public Map<String, Object> getContextProperties()
	{
		return quantfabricContextProperties;
	}
    
    public void addContextProperty(String name, Object value)
    {
    	quantfabricContextProperties.put(name, value);
    }

	public void addQuantfabricService(String name,String alias,String className,Properties props)
    {
    	quantfabricServices.put(name, new ServiceConfiguration(name,alias,className,props));
    }
    public boolean contains(String className){
    	for (ServiceConfiguration service : quantfabricServices.values()) 
			if(service.getClassName().equalsIgnoreCase(className))
				return true;
    	return false;
    }

    public ServiceHostConfiguration configure(String resource) throws Exception
    {
        if (log.isDebugEnabled())
        {
            log.debug( "Configuring from resource: " + resource );
        }
        InputStream stream = getResourceAsStream(resource);
        ConfigParser.doConfigure(this, stream, resource );
        return this;
    }
	public ServiceHostConfiguration configure(URL url) throws Exception
    {
        if (url == null)
        	throw new Exception("configuration URL is NULL");
        	
		if (log.isDebugEnabled())
        {
            log.debug( "configuring from url: " + url);
        }
        try {
            ConfigParser.doConfigure(this, url.openStream(), url.toString());
            return this;
		}
		catch (IOException ioe) {
			throw new Exception("could not configure from URL: " + url, ioe );
		}
	}
	public ServiceHostConfiguration configure(File configFile) throws Exception
    {
        if (log.isDebugEnabled())
        {
            log.debug( "configuring from file: " + configFile.getName() );
        }
        try {
            ConfigParser.doConfigure(this, new FileInputStream(configFile), configFile.toString());
		}
		catch (FileNotFoundException fnfe) {
			throw new Exception( "could not find file: " + configFile, fnfe );
		}
        return this;
    }
	public ServiceHostConfiguration configure(Document document) throws Exception
    {
        if (log.isDebugEnabled())
        {
		    log.debug( "configuring from XML document" );
        }
        ConfigParser.doConfigure(this, document);
        return this;
    }    
    private static InputStream getResourceAsStream(String resource)throws Exception
    {
        String stripped = resource.startsWith("/") ?
                resource.substring(1) : resource;

        InputStream stream = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader!=null) {
            stream = classLoader.getResourceAsStream( stripped );
        }
        if ( stream == null ) {
            stream = ServiceHostConfiguration.class.getResourceAsStream( resource );
        }
        if ( stream == null ) {
            stream = ServiceHostConfiguration.class.getClassLoader().getResourceAsStream( stripped );
        }
        if ( stream == null ) {
            throw new Exception( resource + " not found" );
        }
        return stream;
    }

}
