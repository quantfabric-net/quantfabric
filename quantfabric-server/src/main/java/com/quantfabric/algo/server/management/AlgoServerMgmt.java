/////*
// * Copyright 2022-2023 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.quantfabric.algo.server.management;
//TODO remove or uncom
//
//import java.io.IOException;
//import java.util.concurrent.CountDownLatch;
//
//import javax.management.ObjectName;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.quantfabric.algo.server.AlgoServer;
//
//public class AlgoServerMgmt  implements AlgoServerMgmtMBean {
//
//
//	public AlgoServerMgmt(AlgoServer server, CountDownLatch shutdownLatch)
//    {
//        this.shutdownLatch = shutdownLatch;
//        algoServer = server;
//    }
//
//	public AlgoServerMgmt(AlgoServer server){
//
//		algoServer = server;
//
//
//	}
//
//	@Override
//	public synchronized void shutdown()
//    {
//        log.info("Shutdown action received through JMX");
//        if(shutdownLatch != null)
//        {
//            log.info("Indicating shutdown action");
//            shutdownLatch.countDown();
//        }
//    }
//
//	public synchronized void start() {
//		if(! algoServer.isServerStarted())
//			algoServer.start();
//		else throw new IllegalStateException("Server is started");
//	}
//
//	public synchronized void stop() {
//		if( algoServer.isServerStarted())
//			algoServer.stop();
//		else throw new IllegalStateException("Server is stopped");
//
//	}
//
//
//
//	@Override
//	public synchronized boolean isServerStarted() throws IOException {
//		return algoServer.isServerStarted();
//	}
//
//	@Override
//	public ObjectName[] getServices() throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ObjectName getService(String name) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void addService(ObjectName service) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void removeService(ObjectName service) {
//		// TODO Auto-generated method stub
//
//	}
//
//	private AlgoServer algoServer;
//    private static final Logger log = LoggerFactory.getLogger(AlgoServerMgmt.class);
//    private CountDownLatch shutdownLatch;
//}
