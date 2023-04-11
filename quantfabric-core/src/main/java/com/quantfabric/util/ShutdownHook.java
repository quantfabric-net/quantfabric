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

import java.util.concurrent.CountDownLatch;

public class ShutdownHook extends Thread{
	
	
	 public ShutdownHook(CountDownLatch shutdownLatch)
	    {
	        setDaemon(true);
	        this.shutdownLatch = shutdownLatch;
	    }

	    public void run()
	    {
	        if(shutdown)
	            return;
	        //log.info("Server shutdown requested");
	        shutdownLatch.countDown();
	        try
	        {
	            Thread.sleep(30000L);
	        }
	        catch(InterruptedException e)
	        {
	            e.printStackTrace();
	        }
	    }

	    public void setShutdown(boolean shutdown)
	    {
	        this.shutdown = shutdown;
	    }

	    private final CountDownLatch shutdownLatch;
	    private boolean shutdown;

}
