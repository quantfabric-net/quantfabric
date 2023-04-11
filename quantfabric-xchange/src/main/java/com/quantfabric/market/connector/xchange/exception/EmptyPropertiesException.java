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
package com.quantfabric.market.connector.xchange.exception;

public class EmptyPropertiesException extends RuntimeException{

    public EmptyPropertiesException() {
        super("Adapter setting was not configured correctly: " +
                "please add subscribeType with polling\\pushing value to the .xml file. " +
                "The type is set to polling by default.");
    }

    public EmptyPropertiesException(String s) {
        super(s);
    }
}
