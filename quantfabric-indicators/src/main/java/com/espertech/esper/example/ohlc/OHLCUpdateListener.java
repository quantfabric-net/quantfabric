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
package com.espertech.esper.example.ohlc;

import java.util.Date;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OHLCUpdateListener implements StatementAwareUpdateListener
{
    private static final Logger log = LoggerFactory.getLogger(OHLCUpdateListener.class);

    public void update(EventBean[] newData, EventBean[] oldData, EPStatement epStatement, EPServiceProvider epServiceProvider)
    {
        for (EventBean newDatum : newData) {
            if (log.isInfoEnabled()) {
                log.info("Statement {} produced: {}", epStatement.getName(), getProperties(newDatum));
            }
        }
    }

    private String getProperties(EventBean event)
    {
        StringBuilder buf = new StringBuilder();

        for (String name : event.getEventType().getPropertyNames())
        {
            Object value = event.get(name);
            buf.append(name);
            buf.append("=");

            if (name.contains("timestamp"))
            {
                buf.append(new Date((Long) value));
            }
            else
            {
                buf.append(value);
            }
            buf.append(" ");
        }
        return buf.toString();
    }
}
