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
package com.quantfabric.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpServer extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
	private final RequestHandler requestHandler;
	private final int port;
	
	public TcpServer(int severPort, RequestHandler requestHandler)
	{
		super(String.format("TcpServer(%d)", severPort));
		this.requestHandler = requestHandler;
		this.port = severPort;
	}

	@Override
	public void run() {

		try {
			
			ServerSocket serverSocket = new ServerSocket(port);
			
			try {
				serverSocket.setReuseAddress(true);

				while (!isInterrupted()) {
					logger.info("Listening...");
					Socket connectionSocket = serverSocket.accept();
					logger.info("Client connected (" + connectionSocket.toString() + ").");
					if (!requestHandler.handle(connectionSocket.getInputStream(), connectionSocket.getOutputStream()))
						logger.error("Request does not handled");

					connectionSocket.shutdownInput();
					connectionSocket.shutdownOutput();
					connectionSocket.close();
					logger.info("Session closed.");
				}
			}
			
			finally {
				serverSocket.close();
			}
		}
		catch (IOException e) {
			logger.error("Server crashed.", e);
		}
	}
}
