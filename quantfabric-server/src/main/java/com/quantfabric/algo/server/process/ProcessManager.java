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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessManager {

	private class ProcessWatcherPrinter implements ProcessWatcherHandler {
        public void started(Process process) {
            System.out.println("Prcess started");
        }

        public void stdout(Process process, String line) {
            System.out.println(line);
        }

        public void stderr(Process process, String line) {
            System.err.println(line);
        }

        public void ended(Process process, int value) {
            System.out.println("Process Shutdown. Exit Value :" + value);
        }

        public void error(Process process, Throwable th) {
            System.err.println(th);
        }
    }

	private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
	private final HashMap<String,ProcessWatcher> processes = new HashMap<String,ProcessWatcher>();
	private final HashMap<Integer,ProcessLauncher> processLaunchers= new HashMap<Integer,ProcessLauncher>();

	private final String operatingSystem;
	private boolean isWindows = false;
	private boolean isUnix = false;
	
	public ProcessManager() {
		
		operatingSystem = System.getProperty("os.name").toLowerCase();
		if((operatingSystem.indexOf( "win" ) >= 0)){
			isWindows = true;
		} else {
			isUnix = true;
		}
	}
	
	public Set<String> getProcessNames()
	{
		return processes.keySet();
	}
	
	public void addProcessBuilder(int id, Properties config) {
		this.processLaunchers.put(id, createProcessBuilder(config));
	}
	public void removeProcessBuilder(int id) {
		this.processLaunchers.remove(id);
	}
	public void startProcess(String name,int configId) {
		ProcessLauncher launcher = processLaunchers.get(configId);
		try {
			
			createProcess(name, launcher);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	public void killProcess(String name) {
		if(processes.containsKey(name)) {
			killProcess(processes.get(name).getProcess());
		}
	}
	
	public long getProcessId(String name)
	{
		if(processes.containsKey(name)) 
		{
			return getProcessId(processes.get(name).getProcess());
		}
		
		return 0;
	}
    
	private ProcessLauncher createProcessBuilder(Properties configuration) {
		
		return new ProcessLauncher(configuration.getProperty(ProcessLauncher.CLASS_NAME),
				configuration.getProperty(ProcessLauncher.CLASS_PATH),configuration);
	}
	
	

    private void createProcess(String name,ProcessLauncher launcher) throws IOException {
		InputStream errorStream = null;
		BufferedReader reader = null;
		
		Process subProcess = null;
		try {
			subProcess = launcher.startSubProcess();
			
			/*
			 * Unix oriented command
			 */
			if((operatingSystem.indexOf( "win" ) < 0)){
				reNicePID(subProcess);
			}	
			
			ProcessWatcher w = new ProcessWatcher(subProcess, new ProcessWatcherPrinter());
			w.start();
			

			processes.put(name,w);

		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		}	catch (Exception e) {
				e.printStackTrace();
		} finally {
			if (errorStream != null) {
				errorStream.close();
			}
			if (reader != null) {
				reader.close();
			}
		}

	}
	private void killProcess(Process process) {
		
		try {
			process.destroy();
			checkProcess(process);
		} catch (IllegalThreadStateException itse) {
			logger.error("Process with PID =  {}  did not exit ==> Issuing kill command", getProcessId(process));
			String forceKillCommand;
			if(isWindows()){
				forceKillCommand = "taskkill /f /pid " + getProcessId(process) + " /t";
			}else{
				forceKillCommand = "kill -9 " + getProcessId(process);
			}
			try {
				Runtime.getRuntime().exec(forceKillCommand);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
 	private long getProcessId(Process inProcess) {
		long pPID = -1;
		Field pidField;
		try {
			if(isWindows()){
				pidField = inProcess.getClass().getDeclaredField("handle");
			}else{
				pidField = inProcess.getClass().getDeclaredField("pid");
			}
			pidField.setAccessible(true);

			pPID = pidField.getLong(inProcess);
			
			if (isWindows())				
			{
				Kernel32 kernel = Kernel32.INSTANCE;
			    HANDLE handle = new HANDLE();
			    handle.setPointer(Pointer.createConstant(pPID));
			    long pid = kernel.GetProcessId(handle);
			    return pid;
			}
			
			return pPID;
		} catch (SecurityException e) {
			logger.error(e.getMessage());
		} catch (NoSuchFieldException e) {
			logger.error(e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}
		return pPID;
	}

	private void checkProcess(Process p) {
		if (p.exitValue() == 0) {
			logger.info("Server with PID ==> {}  is DOWN and exited normally.", this.getProcessId(p));
		} else {
			logger.info("Server with PID ==> {}  is DOWN and DID NOT exit normally.", this.getProcessId(p));
		}
	}

	private void reNicePID(Process server) {
		long serverPid = getProcessId(server);
		try {
			Runtime.getRuntime().exec("/usr/bin/renice 0 -p " + serverPid);
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		}
	}

	public boolean isWindows() {
		return isWindows;
	}

	public boolean isUnix() {
		return isUnix;
	}

	public void finalize() throws Throwable
	{		
		for (ProcessWatcher pw : processes.values())
			killProcess(pw.getProcess());
		super.finalize();
	}
}
