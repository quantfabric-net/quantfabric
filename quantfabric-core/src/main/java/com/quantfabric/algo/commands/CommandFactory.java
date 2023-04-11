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
package com.quantfabric.algo.commands;


public interface CommandFactory
{
	class CommandFactoryException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7167696385336482441L;
		
		public CommandFactoryException(String message)
		{
			super(message);
		}
		
		public CommandFactoryException(String message, Exception innerException)
		{
			super(message, innerException);
		}		
	}
	
	class NotSupportCommandException extends CommandFactoryException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8508769226746809819L;

		public static String generateMessage(Command command)
		{
			return generateMessage(command.getClass().getSimpleName());			
		}
		
		public static String generateMessage(String commandName)
		{
			return "not support command - " + commandName;			
		}
		
		public NotSupportCommandException(Command command)
		{
			super(generateMessage(command));
		}
		
		public NotSupportCommandException(Command command, Exception innerException)
		{
			super(generateMessage(command), innerException);
		}
		
		public NotSupportCommandException(String message)
		{
			super(message);
		}
		
		public NotSupportCommandException(String message, Exception innerException)
		{
			super(generateMessage(message), innerException);
		}
	}

	ConcreteCommand create(Command command) throws CommandFactoryException;
}
