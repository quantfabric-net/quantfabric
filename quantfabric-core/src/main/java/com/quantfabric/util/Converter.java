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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Converter
{
	public static final Map<Class<?>, Class<? extends Object>> prmitiveTypeMappingToReferenceType =
		new HashMap<>();
	static
	{
		prmitiveTypeMappingToReferenceType.put(Byte.TYPE, Byte.class);
		prmitiveTypeMappingToReferenceType.put(Short.TYPE, Short.class);
		prmitiveTypeMappingToReferenceType.put(Integer.TYPE, Integer.class);
		prmitiveTypeMappingToReferenceType.put(Long.TYPE, Long.class);
		prmitiveTypeMappingToReferenceType.put(Float.TYPE, Float.class);
		prmitiveTypeMappingToReferenceType.put(Double.TYPE, Double.class);
		prmitiveTypeMappingToReferenceType.put(Character.TYPE, Character.class);
		prmitiveTypeMappingToReferenceType.put(Boolean.TYPE, Boolean.class);
	}

	private Converter(){}
	public static String toString(Object obj)
	{
		if (obj == null)
			return "null";
		
		Method[] methods = obj.getClass().getMethods();
		StringBuilder sb = new StringBuilder(obj.getClass().getSimpleName() + ": ");
		for (Method method : methods)
			try {
				if (!method.getName().equals("getClass")) {
					if (method.getName().startsWith("get")) {
						Object v = method.invoke(obj);
						if (v == null)
							v = "null";
						sb.append(method.getName().substring(3)).append("=").append(v).append("; ");
					}

					if (method.getName().startsWith("is")) {
						Object v = method.invoke(obj);
						if (v == null)
							v = "null";

						sb.append(method.getName()).append("=").append(v).append("; ");
					}
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(Converter.class).error("Can't convert to string : " + e.getMessage());
			}
		return sb.toString();
	}
	
	public static Properties mapToProperties(Map<String, String> map)
	{
		Properties p = new Properties();
		Set<Map.Entry<String, String>> set = map.entrySet();
		for (Map.Entry<String, String> entry : set)
		{
			p.put(entry.getKey(), entry.getValue());
		}
		return p;
	}
	
	public static Map<String, String> propertiesToMap(Properties props)
	{
		Map<String, String> mapProperties = new HashMap<>();
		
		for(Entry<Object, Object> x : props.entrySet()) 
		{
			mapProperties.put((String)x.getKey(), (String)x.getValue());
		}
		
		return mapProperties;
	}
	
	public static String domToString(Document document) throws TransformerException 
	{
	    TransformerFactory tFactory = TransformerFactory.newInstance();
	    Transformer transformer = tFactory.newTransformer();	    

	    DOMSource source = new DOMSource(document);
	    
	    StringWriter sw = new StringWriter();
	    StreamResult result = new StreamResult(sw);	    

	    transformer.setOutputProperty(OutputKeys.INDENT, "yes" );
	    transformer.transform(source, result);	    

	    return sw.toString();

	}

	public static Document stringToDom(String source) throws SAXException,
			ParserConfigurationException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(source)));
	}
}
