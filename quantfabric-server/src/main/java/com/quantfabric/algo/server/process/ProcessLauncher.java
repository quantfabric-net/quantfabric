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
package com.quantfabric.algo.server.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class ProcessLauncher {
	public static final String CLASS_NAME ="mainClassName";
	public static final String CLASS_PATH="classPath";
	/**
	 * Optional Parameters
	 */
	public static final String JVM_MIN_HEAP = "jvmMinHeap";
	public static final String JVM_MAX_HEAP = "jvmMaxHeap";
	public static final String JVM_GC = "jvmGC";
	public static final String JVM_GC_THREADS = "jvmGCThreads";
	public static final String JVM_MAX_PERM_SIZE = "jvmMaxPermSize";
	
	private ProcessBuilder pb;
	
	
	public ProcessLauncher(String serverClassName,String serverClassPath,Properties configuration) {
		String javaCommand = null;
		if (System.getenv().containsKey("JAVA_HOME")) {
			javaCommand = System.getenv("JAVA_HOME") + "/bin/java";
		} else {
			System.out.println("--------------------------------\nValue: " + "JAVA_HOME" + " is not set in the environment\n--------------------------------\n");
			return;
		}
		// prepare command
		List<String> command = new ArrayList<String>();
		command.add(javaCommand);
		
		for (Object key : configuration.keySet())
		{
			String propName = key.toString();
			String value = convertConfigPropertyToCmdArg(configuration, propName);
			
			if (value != null)
				command.add(value);
		}
		
		command.add(serverClassName);		
				
		// create builder
		System.out.println("CREATE BUILDER : " + StringUtils.join(command, ", "));
		pb = new ProcessBuilder(command);
		// Combine the error and input streams
		pb.redirectErrorStream(true);
		// Put the serverPort and classpath in the runtime environment for
		// the ThreadedTaskServer
		
		String classpath = System.getProperty("java.class.path");
		if(null != serverClassPath && !serverClassPath.isEmpty())
			pb.environment().put("CLASSPATH", serverClassPath);
		else
			pb.environment().put("CLASSPATH", classpath);
	}

	public Process startSubProcess() throws IOException {
		return pb.start();
	}

	private String convertConfigPropertyToCmdArg(Properties configuration, String property)
	{
		String value = configuration.getProperty(property);
		if (value != null && !value.equals(""))
		{
			if (property.equals(JVM_MIN_HEAP) ||
				property.equals(JVM_MAX_HEAP) ||
				property.equals(JVM_MAX_PERM_SIZE) ||
				property.equals(JVM_GC) ||
				property.equals(JVM_GC_THREADS))
			{
				return value;
			}
			else
			{
				if (!property.startsWith("-"))
					return String.format("-D%s=%s", property, value);
				else
					return String.format("%s=%s", property, value);
			}
		}
		else
			if (!property.startsWith("-"))
				return String.format("-D%s", property);
			else
				return property;
	}
}
