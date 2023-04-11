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
package com.quantfabric.algo.cep.indicators.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventPropertyGetter;
import com.espertech.esper.client.EventType;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNumberSetWildcardMarker;

public class ViewAdditionalProps
{
    private final String[] additionalProps;
    private final ExprEvaluator[] additionalExpr;

    private ViewAdditionalProps(String[] additionalProps, ExprEvaluator[] additionalExpr)
    {
        this.additionalProps = additionalProps;
        this.additionalExpr = additionalExpr;
    }

    public String[] getAdditionalProps()
    {
        return additionalProps;
    }

    public ExprEvaluator[] getAdditionalExpr()
    {
        return additionalExpr;
    }

    public static ViewAdditionalProps make(ExprNode[] validated, int startIndex, EventType parentEventType) {
        if (validated.length <= startIndex) {
            return null;
        }

        List<String> additionalProps = new ArrayList<String>();
        List<ExprEvaluator> lastValueExpr = new ArrayList<ExprEvaluator>();
        boolean copyAllProperties = false;

        for (int i = startIndex; i < validated.length; i++) {

            if (validated[i] instanceof ExprNumberSetWildcardMarker) {
                copyAllProperties = true;
            }

            additionalProps.add(validated[i].toExpressionString());
            lastValueExpr.add(validated[i].getExprEvaluator());
        }

        if (copyAllProperties) {
            for (EventPropertyDescriptor propertyDescriptor : parentEventType.getPropertyDescriptors()) {
                if (propertyDescriptor.isFragment()) {
                    continue;
                }
                additionalProps.add(propertyDescriptor.getPropertyName());
                final EventPropertyGetter getter = parentEventType.getGetter(propertyDescriptor.getPropertyName());
                final Class<?> type = propertyDescriptor.getPropertyType();
                ExprEvaluator exprEvaluator = new ExprEvaluator() {
                    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext context) {
                        return getter.get(eventsPerStream[0]);
                    }

                    public Class<?> getType() {
                        return type;
                    }

                    public Map<String, Object> getEventType() {
                        return null;
                    }
                };
                lastValueExpr.add(exprEvaluator);
            }
        }

        String[] addPropsArr = additionalProps.toArray(new String[additionalProps.size()]);
        ExprEvaluator[] valueExprArr = lastValueExpr.toArray(new ExprEvaluator[lastValueExpr.size()]);
        return new ViewAdditionalProps(addPropsArr, valueExprArr);
    }

    public void addProperties(Map<String, Object> newDataMap, Object[] lastValuesEventNew)
    {
        if (lastValuesEventNew != null) {
            for (int i = 0; i < additionalProps.length; i++) {
                newDataMap.put(additionalProps[i], lastValuesEventNew[i]);
            }
        }
    }

    public static void addCheckDupProperties(Map<String, Object> target, ViewAdditionalProps addProps, String... builtin) {
        if (addProps == null) {
            return;
        }

        for (int i = 0; i < addProps.getAdditionalProps().length; i++) {
            String name = addProps.getAdditionalProps()[i];
            for (int j = 0; j < builtin.length; j++) {
                if ((name.equalsIgnoreCase(builtin[j]))) {
                    throw new IllegalArgumentException("The property by name '" + name + "' overlaps the property name that the view provides");
                }
            }
            target.put(name, addProps.getAdditionalExpr()[i].getType());
        }
    }
}