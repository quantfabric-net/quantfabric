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

public class QuantfabricException extends RuntimeException
{
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 2575682221576727230L;

	/**
     * ctor
     * @param message - error message
     */
    public QuantfabricException(final String message)
    {
        super(message);
    }

    /**
     * ctor for an inner exception and message.
     * @param message - error message
     * @param cause - inner exception
     */
    public QuantfabricException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * ctor - just an inner exception.
     * @param cause - inner exception
     */
    public QuantfabricException(final Throwable cause)
    {
        super(cause);
    }
}
