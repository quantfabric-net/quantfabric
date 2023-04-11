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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.quantfabric.persistence.DataAdapter;
import com.quantfabric.persistence.DataAdapter.AdaptationException;
//import com.quantfabric.persistence.csv.BaseCsvBean;
import com.quantfabric.persistence.csv.CsvMetadata;
import com.quantfabric.persistence.csv.CsvStorageProvider;
import com.quantfabric.persistence.csv.CsvStorageProviderSettings;
//import com.quantfabric.persistence.util.ObjectFlatter;
import com.quantfabric.persistence.util.PropertyWrapper;
import com.quantfabric.util.SessionId;

public class EsperCsvStorageProvider extends CsvStorageProvider implements ChangePersistingClassNameListener {
    public static class EsperCsvMetadata implements CsvMetadata {
        private final Map<String, Class<? extends Object>> fields = new HashMap<String, Class<? extends Object>>();

        private final Map<String, FlatCompositeObjectField> wrappers = new HashMap<String, FlatCompositeObjectField>();

        private final String name;

        public EsperCsvMetadata(String name) {
            this.name = name;
        }

        public void addField(String name, Class<? extends Object> type) {
            fields.put(name, type);
        }

        public void addField(String name, FlatCompositeObjectField flatCompositeObjectField) {
            addField(name, flatCompositeObjectField.getClass());
            wrappers.put(name, flatCompositeObjectField);
        }

        @Override
        public String getSchemaName() {
            return name;
        }

        @Override
        public Map<String, Class<? extends Object>> getFields() {
            return fields;
        }

        public FlatCompositeObjectField getFieldWrapper(String fieldName) {
            return wrappers.get(fieldName);
        }

    }

    public static class FlatCompositeObjectField {
        private final String rootName;
        private final PropertyWrapper propertyWrapper;

        public FlatCompositeObjectField(PropertyWrapper propertyWrapper, String rootName) {
            this.rootName = rootName;
            this.propertyWrapper = propertyWrapper;
        }

        public String getRootName() {
            return rootName;
        }

        public PropertyWrapper getPropertyWrapper() {
            return propertyWrapper;
        }

        @Override
        public String toString() {
            String sb = "[" + "rootName=" + rootName +
                    ", propertyWrapper.name=" + propertyWrapper.getName() +
                    ", propertyWrapper.valueType=" + propertyWrapper.getValueType().getName() +
                    "]";
            return sb;
        }
    }

    public static class EsperCsvBean {
        private final String persistingClassName;
        private final List<Object> values;
        private final int fieldsCount;

        public EsperCsvBean(String persistingClassName, EsperCsvMetadata metadata, EventBean bean) throws AdaptationException {
            this.persistingClassName = persistingClassName;
            this.values = getEventBeanValues(metadata, bean);
            this.fieldsCount = metadata.getFields().size();
            //createDefaultCellProcessors();

			/*Collection <Class<?>> fieldsTypes = metadata.getFields().values();
			int fieldIndex = 0;
			for (Class<?> type : fieldsTypes)
			{
				if (type == Data.class)
					setCellProcessor(fieldIndex, BaseCsvBean.DateCellProcessor);
				else
					if (type == Boolean.class)
						setCellProcessor(fieldIndex, BaseCsvBean.BoolCellProcessor);

				fieldIndex++;
			}*/
        }

        private static List<Object> getEventBeanValues(EsperCsvMetadata metadata, EventBean bean) throws AdaptationException {
            List<Object> values = new ArrayList<Object>();

            for (Map.Entry<String, Class<? extends Object>> field : metadata.getFields().entrySet()) {
                String fieldName = field.getKey();
                Class<? extends Object> fieldType = field.getValue();

                if (fieldName.equals("QuantfabricAlgoSessionId")) values.add(SessionId.getSessionID());
                else {
                    if (fieldType == FlatCompositeObjectField.class) {
                        FlatCompositeObjectField fcbField = metadata.getFieldWrapper(fieldName);

                        Object rootObject = EsperEventBeanUtils.getFieldValue(bean, fcbField.getRootName());

                        try {
                            values.add(fcbField.getPropertyWrapper().getValue(rootObject));
                        } catch (Exception e) {
                            throw new AdaptationException("Can't get value (fcbField=" + fcbField, e);
                        }
                    }
                }
            }

            return values;
        }

        public String getPersistingClassName() {
            return persistingClassName;
        }


        public List<Object> getValues() {
            return values;
        }


        public int getFieldsCount() {
            return fieldsCount;
        }
    }

    public static class EsperCsvBeanDataAdapter implements DataAdapter {
        private EsperCsvMetadata metadata;
        private String persistingClassName;
        private ChangePersistingClassNameListener changePersistingClassNameListener;
        private final List<Class<?>> persistableClasses = new LinkedList<Class<?>>();

//        public EsperCsvBeanDataAdapter(
//                EPStatement statement,
//                ChangePersistingClassNameListener changePersistingClassNameListener) {
//
//            persistingClassName = EsperJdbcDataAdapter.getPersistingClassName(statement);
//            this.changePersistingClassNameListener = changePersistingClassNameListener;
//
//            addPersitableClass(java.lang.String.class);
//            addPersitableClass(java.lang.Integer.class);
//            addPersitableClass(java.lang.Long.class);
//            addPersitableClass(java.lang.Float.class);
//            addPersitableClass(java.lang.Double.class);
//            addPersitableClass(java.math.BigDecimal.class);
//            addPersitableClass(java.lang.Boolean.class);
//            addPersitableClass(java.lang.Short.class);
//            addPersitableClass(java.util.Date.class);
//
//            metadata = createMetadata(statement, persistableClasses);
//        }

        protected void addPersitableClass(Class<?> clazz) {
            persistableClasses.add(clazz);
        }

//        private static EsperCsvMetadata createMetadata(EPStatement statement, List<Class<?>> persistableClasses) {
//            EsperCsvMetadata metadata =
//                    new EsperCsvMetadata(EsperJdbcDataAdapter.getNamePersisting(statement));
//
//            metadata.addField("QuantfabricAlgoSessionId", String.class);
//
//            Map<String, Class<?>> properties = EsperEventBeanUtils.getProperties(statement.getEventType());
//
//            for (Map.Entry<String, Class<?>> property : properties.entrySet()) {
//                String propertyName = property.getKey();
//
//                if (ObjectFlatter.typeIsPersitable(persistableClasses, property.getValue())) {
//                    metadata.addField(
//                            EsperJdbcDataAdapter.convertToPersisterFieldName(propertyName),
//                            property.getValue());
//                } else {
//                    Collection<PropertyWrapper> sub_properties =
//                            ObjectFlatter.getFlatStructure(property.getValue(), persistableClasses);
//
//                    for (PropertyWrapper sub_property : sub_properties) {
//                        metadata.addField(
//                                propertyName + "." + sub_property.getName(),
//                                new FlatCompositeObjectField(sub_property, propertyName));
//                    }
//                }
//            }
//
//            return metadata;
//        }

        public EsperCsvMetadata getMetadata() {
            return metadata;
        }

        public String getPersistingClassName() {
            return persistingClassName;
        }

        @Override
        public Object adapt(Object object) throws AdaptationException {
//            if (object instanceof EventBean) {
//                EventBean bean = (EventBean) object;
//
//                String currentPersistingClassName =
//                        EsperJdbcDataAdapter.getPersistingClassName(bean.getEventType());
//
//                if (!currentPersistingClassName.equals(this.persistingClassName)) {
//                    if (changePersistingClassNameListener != null)
//                        changePersistingClassNameListener.
//                                changedPersistingClassName(this.persistingClassName, currentPersistingClassName);
//
//                    this.persistingClassName = currentPersistingClassName;
//                }
//
//                return new EsperCsvBean(persistingClassName, metadata, bean);
//            }
//            throw new AdaptationException("Not support type " + object.getClass().getName());
            return null;
        }
    }

    private EsperCsvBeanDataAdapter dataAdapter;

    public EsperCsvStorageProvider(CsvStorageProviderSettings settings, EPStatement statement) throws StoragingException {
        super(settings);
//        dataAdapter = new EsperCsvBeanDataAdapter(statement, this);
//
//        importMetadata(dataAdapter.getPersistingClassName(), dataAdapter.getMetadata());
    }

    @Override
    public DataAdapter getDataApdapter() {
        return dataAdapter;
    }

    @Override
    public void changedPersistingClassName(String origianlPersistingClassName, String newPersistingClassName) {
        this.newPersistingClassName(origianlPersistingClassName, newPersistingClassName);
    }

}
