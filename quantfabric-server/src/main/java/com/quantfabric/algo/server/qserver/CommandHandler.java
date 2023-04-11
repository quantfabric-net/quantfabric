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
package com.quantfabric.algo.server.qserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quickserver.net.server.ClientCommandHandler;
import org.quickserver.net.server.ClientHandler;
import org.quickserver.net.server.DataMode;
import org.quickserver.net.server.DataType;

import com.quantfabric.algo.server.AlgoServer;
import com.quantfabric.algo.server.qserver.commands.ClientCommand;
import com.quantfabric.algo.server.qserver.commands.StrategyDataStreamingRequest;
import com.quantfabric.net.rpc.RpcServer;

public class CommandHandler implements ClientCommandHandler
{

	private static class CommandFactory
	{
		public static boolean isCommand(String command)
		{
			return command.startsWith("StrategyDataStreamingRequest");
		}
		public static ClientCommand create(String command)
		{
			if (command.startsWith("StrategyDataStreamingRequest"))
			{

				try (Scanner scanner = new Scanner(command)) {

					String unitName = scanner.findInLine("\".*\",").replace('\"', ' ').replace(',', ' ').trim();
					String strategyName = scanner.findInLine("\".*\"").replace('\"', ' ').replace(',', ' ').trim();

					return new StrategyDataStreamingRequest(unitName, strategyName);
				}
			}
			
			return null;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class.getName());
	private final RpcServer rpcServer;
	private final CommandExecutor commandExecutor;
	
	public CommandHandler() 
	{
		rpcServer = AlgoServer.getRpcServer();
		commandExecutor = AlgoServer.getCommandExecutor();
	}	
	
	@Override
	public void gotConnected(ClientHandler handler)
			throws IOException
	{
		logger.info("Connection opened: "+handler.getHostAddress());
		handler.setDataMode(DataMode.STRING, DataType.OUT);
	}

	@Override
	public void lostConnection(ClientHandler handler) throws IOException {
		logger.info("Connection lost: "+handler.getHostAddress());
	}

	@Override
	public void closingConnection(ClientHandler handler) throws IOException {
		logger.info("Connection closed: "+handler.getHostAddress());
	}
	
	@Override
	public void handleCommand(ClientHandler handler, String command)
			throws IOException
	{
		if (CommandFactory.isCommand(command))
		{
			ClientCommand clientCommand = CommandFactory.create(command);
			
			if (clientCommand != null)
			{		
				clientCommand.setClientHandler(handler);
				
				if (!commandExecutor.execute(clientCommand))
					handler.closeConnection();
			}
		}
		else
		{
			InputStream input = new ByteArrayInputStream(command.getBytes());
			rpcServer.handle(input, handler.getOutputStream());	
			input.close();
			handler.closeConnection();
		}
	}
}
