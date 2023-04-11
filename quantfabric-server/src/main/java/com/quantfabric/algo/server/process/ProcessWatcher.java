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
import java.io.InputStreamReader;


public class ProcessWatcher {
    private final Process process;
    private final Thread end;
    private final Thread out;
	private final Thread err;
    private int result;
    private boolean stopped=false;
    private final ProcessWatcherHandler handler;
    
    public Process getProcess() {
		return process;
	}
    public ProcessWatcher(Process theProcess, ProcessWatcherHandler theHandler) {
        this.process = theProcess;
        this.handler = theHandler;
        end=new Thread(){
            @Override
            public void run() {
                try {
                    result = process.waitFor();
                    handler.ended(process,result);
                } catch (Throwable e) {
                    handler.error(process,e);
                } finally{
                    stopped=true;
                }
            }
        };
        out=new Thread(){
            @Override
            public void run() {
                String read;
                BufferedReader in=new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (!stopped) {
                    try {
                        read = in.readLine();
                        if (read == null) {
                            break;
                        }
                        handler.stdout(process,read);
                    } catch (Throwable e) {
                        handler.error(process,e);
                        break;
                    }
                }
            }
        };
        err=new Thread(){
            @Override
            public void run() {
                String read;
                BufferedReader in=null;
                try {
                    in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while (!stopped) {
                        try {
                            read = in.readLine();
                            if (read == null) {
                                break;
                            }
                            handler.stderr(process,read);
                        } catch (Throwable e) {
                            handler.error(process,e);
                            break;
                        }
                    }
                } catch(Throwable e) {
                    handler.error(process,e);
                } finally {
                    if (in!=null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            handler.error(process,e);
                        }
                    }
                }
            }
        };
    }
    public void start(){
        handler.started(process);
        end.start();
        out.start();
        err.start();
    }

    public int waitfor(){
        while(!stopped){
            Thread.yield();
        }
        return result; 
    }
}
