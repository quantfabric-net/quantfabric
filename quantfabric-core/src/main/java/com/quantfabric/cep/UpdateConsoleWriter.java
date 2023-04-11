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
package com.quantfabric.cep;

import com.quantfabric.persistence.esper.AsynchronEsperEventPersister;
import com.quantfabric.persistence.esper.EsperConsoleStorageProvider;
import com.quantfabric.persistence.esper.PersistingUpdateListener;

public class UpdateConsoleWriter extends PersistingUpdateListener
{
    public UpdateConsoleWriter()
	{
		super("Console", new AsynchronEsperEventPersister(new EsperConsoleStorageProvider()));
	}
}