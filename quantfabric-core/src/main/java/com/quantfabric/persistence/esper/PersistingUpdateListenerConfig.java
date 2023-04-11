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
package com.quantfabric.persistence.esper;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.espertech.esper.util.DOMElementIterator;
import com.quantfabric.persistence.StorageProvider;
import com.quantfabric.persistence.StorageProviderSettings;

public class PersistingUpdateListenerConfig {
    private String name;
    private String type;
    private Class<? extends StorageProvider> storageProviderClass;
    private Class<? extends StorageProviderSettings> storageProviderSettingsClass;

    private Map<String, Object> settingBlocks;

    public PersistingUpdateListenerConfig(String name, String type) {
        this(name, type, new HashMap<String, Object>());
    }

    public PersistingUpdateListenerConfig(String name, String type, Map<String, Object> settingBlocks) {
        setName(name);
        setType(type);
        setSettingBlocks(settingBlocks);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type.toLowerCase();
        determineClasses();
    }

    public Class<? extends StorageProvider> getStorageProviderClass() {
        return storageProviderClass;
    }

    public Class<? extends StorageProviderSettings> getStorageProviderSettingsClass() {
        return storageProviderSettingsClass;
    }

    private void determineClasses() {
        if (getType().equals("cache")) {
//				storageProviderClass = EsperCacheStorageProvider.class;
//				storageProviderSettingsClass = CacheStorageProviderSettings.class;
            throw new UnsupportedOperationException();
        } else {
            storageProviderClass = null;
            storageProviderSettingsClass = null;
        }
    }

    public Map<String, Object> getSettingBlocks() {
        return settingBlocks;
    }

    public Object getSettingBlock(String blockName) {
        return settingBlocks.get(blockName);
    }

    private void setSettingBlocks(Map<String, Object> settingBlocks) {
        this.settingBlocks = settingBlocks;
    }

    public void addSettingsBlock(String blockName, Object settingsBlock) {
        settingBlocks.put(blockName, settingsBlock);
    }

    public void removeSettingsBlock(String blockName) {
        settingBlocks.remove(blockName);
    }

    public static PersistingUpdateListenerConfig getFromXML(Node persistingUpdateListenerConfigNode) {
        if (persistingUpdateListenerConfigNode != null && persistingUpdateListenerConfigNode.getNodeName().equals("esperPersistingUpdateListener")) {
            String name = persistingUpdateListenerConfigNode.getAttributes().getNamedItem("name").getTextContent();
            String type = persistingUpdateListenerConfigNode.getAttributes().getNamedItem("type").getTextContent();

            PersistingUpdateListenerConfig config = new PersistingUpdateListenerConfig(name, type.toLowerCase());

            DOMElementIterator settingsElementIterator = new DOMElementIterator(persistingUpdateListenerConfigNode.getChildNodes());

            while (settingsElementIterator.hasNext()) {
                Element settingsBlockElement = settingsElementIterator.next();

                if (settingsBlockElement.getNodeName().equals("storageProvider-settings"))
                    config.addSettingsBlock("StorageProviderSettings", StorageProviderSettings.getFromXML(config.getStorageProviderSettingsClass(), settingsBlockElement));
            }
            return config;
        }
        return null;
    }

}
