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
package com.quantfabric.persistence.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.quantfabric.persistence.StorageProviderSettings;

import static com.quantfabric.algo.configuration.QuantfabricConstants.*;

public class CacheStorageProviderSettings extends StorageProviderSettings
{
	public static class Credentials
	{
		private String username;
		private String password;

		public String getUsername()
		{
			return username;
		}

		public String getPassword()
		{
			return password;
		}
		
		private void setUsername(String username)
		{
			this.username = username;
		}

		private void setPassword(String password)
		{
			this.password = password;
		}

		public Credentials(String username, String password)
		{
			super();
			this.username = username;
			this.password = password;
		}
	}
	
	private String namespace;
	private final Credentials credentials;
	
	public String getNamespace()
	{
		return namespace;
	}
	
	private void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public Credentials getCredentials()
	{
		return credentials;
	}

	public CacheStorageProviderSettings()
	{
		this(new Credentials(null, null), null);
	}
	
	public CacheStorageProviderSettings(Credentials credentials, String namespace)
	{
		super();
		this.namespace = namespace;
		this.credentials = credentials;
	}

	@Override
	protected void initialize(Properties properties)
	{
		for (Map.Entry<Object, Object> property : properties.entrySet())
		{			
			if (property.getKey().equals(USERNAME))
				getCredentials().setUsername(String.valueOf(property.getValue()));
			else
				if (property.getKey().equals(PASSWORD))
					getCredentials().setPassword(String.valueOf(property.getValue()));
				else
					if (property.getKey().equals(NAMESPACE))
						setNamespace(String.valueOf(property.getValue()));
		}		
	}

	public Map<String, String> getSettingsInfo()
	{
		HashMap<String, String> settingsInfo = new HashMap<String, String>();
		settingsInfo.put("Credentials", "username=" + credentials.getUsername() + 
				"; password=" + credentials.getPassword());
		settingsInfo.put(NAMESPACE, namespace);
		return settingsInfo;
	}

	
}
