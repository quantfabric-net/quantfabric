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
package com.quantfabric.algo.market.gateway.access.product.publisher;

import java.io.Serializable;

import com.quantfabric.algo.market.gateway.access.product.ContentType;

public class PublisherAddress implements Serializable
{
	private static final long serialVersionUID = 8551904638258866571L;
	
	private final String host;
	private final int port;
	private final ContentType contentType;
		
	public PublisherAddress(String host, int port, ContentType contentType)
	{
		super();
		this.host = host;
		this.port = port;
		this.contentType = contentType;
	}
	
	public String getHost()
	{
		return host;
	}
	public int getPort()
	{
		return port;
	}
	public ContentType getContentType()
	{
		return contentType;
	}		
}
