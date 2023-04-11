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
package com.quantfabric.messaging;


public interface Publisher<TS extends Subscriber<TD>,
					T extends TargetSubject,
					TD>  {

	class PublisherException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2214702170704868808L;

		public PublisherException()
		{
			super();
		}

		public PublisherException(String message, Throwable cause)	{
			super(message, cause);
		}

		public PublisherException(String message)	{
			super(message);
		}

		public PublisherException(Throwable cause)	{
			super(cause);
		}
	}
	
	void registerSubscriber(TS subscriber);
	void unregisterSubscriber(TS subscriber);
	void subscribe(TS subscriber, T subject) throws PublisherException;
	void subscribe(int subscriberId, T subject) throws PublisherException;
	void unSubscribe(TS subscriber, T subject);
	void unSubscribe(int subscriberId, T subject);
	
	void publish(TD data) throws PublisherException;
}
