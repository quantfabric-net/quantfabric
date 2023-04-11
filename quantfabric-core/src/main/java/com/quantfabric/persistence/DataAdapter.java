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
package com.quantfabric.persistence;

public interface DataAdapter
{
	class AdaptationException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5287514697707900622L;	
		
		public AdaptationException(String message) 
		{
			super(message);
		}
		public AdaptationException() 
		{
			super();
		}
		
		public AdaptationException(String message, Exception innerException)
		{			
			super(message, innerException);
		}
		
		public AdaptationException(Exception innerException)
		{			
			super(innerException);
		}
	}
	
	Object adapt(Object object) throws AdaptationException;
}
