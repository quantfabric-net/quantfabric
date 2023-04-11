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
package com.quantfabric.cep.view;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.OneEventCollection;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.util.ExecutionPathDebugLog;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

public class UniqueByPropertyWindowView extends ViewSupport implements DataWindowView, CloneableView
{
	private final ExprNode[] criteriaExpressions;
	private final ExprEvaluator[] criteriaEvaluators;
	private final StatementContext statementContext;
	
	
	private final String[] propertyNames;
    private final int numKeys;
    private final ArrayDeque<EventBean> events = new ArrayDeque<EventBean>();
    private MultiKey<Object> lastKey = new MultiKey<Object>(new Object[] {null});
    private static final Logger log = LoggerFactory.getLogger(UniqueByPropertyWindowView.class);

    /**
     * Constructor.
     * @param criteriaExpressions is the expressions from which to pull the unique value
     * @param exprEvaluatorContext context for expression evaluation
     */
    public UniqueByPropertyWindowView(StatementContext statementContext, 
    									ExprNode[] criteriaExpressions, ExprEvaluator[] criteriaEvaluators)
    {
    	this.statementContext = statementContext;
        this.criteriaExpressions = criteriaExpressions;
        this.criteriaEvaluators = criteriaEvaluators;

        propertyNames = new String[criteriaExpressions.length];
        for (int i = 0; i < criteriaExpressions.length; i++)
        {
            propertyNames[i] = criteriaExpressions[i].toExpressionString();
        }
        numKeys = criteriaExpressions.length;
    }

    public View cloneView(StatementContext statementContext)
    {
        return new UniqueByPropertyWindowView(statementContext, criteriaExpressions, criteriaEvaluators);
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return events.isEmpty();
    }

    public final EventType getEventType()
    {
        // The event type is the parent view's event type
        return parent.getEventType();
    }
    
    /**
     * Returns the name of the field supplying the unique value to keep the most recent record for.
     * @return expressions for unique value
     */
    public final ExprNode[] getCriteriaExpressions()
    {
        return criteriaExpressions;
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        if ((ExecutionPathDebugLog.isDebugEnabled) && (log.isDebugEnabled()))
        {
            log.debug(".update Updating view");
            dumpUpdateParams("UniqueByPropertyWindowView", newData, oldData);
        }

        
        OneEventCollection postNewData =  new OneEventCollection();
        OneEventCollection postOldData =  new OneEventCollection();
        
        if (newData != null)
        {
        	MultiKey<Object> newKey = getUniqueKey(newData[newData.length-1]);
        	if(!lastKey.equals(newKey)) {
        		//remove old events
        		int expiredCount = events.size();
        		 for (int i = 0; i < expiredCount ; i++)
                 {
        			 postOldData.add(events.removeFirst());
                 }
        	}
        	 
        	for(int j = 0 ; j< newData.length ; j++) {
        		EventBean newBean = newData[j];
        		MultiKey<Object> key = getUniqueKey(newBean);
        		if(newKey.equals(key)) {
        			events.add(newBean);
        			postNewData.add(newBean);
        		}
        	}
        	lastKey = newKey;
        	
        }
        if (oldData != null)
        {
            for (int i = 0; i < oldData.length; i++)
            {
                // Obtain unique value
                MultiKey<Object> key = getUniqueKey(oldData[i]);

                if(lastKey != key) {
                     //postOldData.add(lastValue);

                }
            }
        }	
        // If there are child views, fireStatementStopped update method
        if (this.hasViews())
        {
            if (postOldData.isEmpty())
            {
                updateChildren(postNewData.toArray(), null);
            }
            else
            {
                updateChildren(postNewData.toArray(), postOldData.toArray());
            }
        }
    }
    private MultiKey<Object> getUniqueKey(EventBean event)
    {
        Object[] values = new Object[numKeys];
        for (int i = 0; i < numKeys; i++)
        {
            values[i] = event.get(propertyNames[i]);
        }
        return new MultiKey<Object>(values);
    }


    public final Iterator<EventBean> iterator()
    {
        return events.iterator();
    }

    public final String toString()
    {
    	return this.getClass().getName() + " uniqueFieldNames=" + Arrays.toString(criteriaExpressions);
    }

	@Override
	public View cloneView()
	{
		return null;
	}

	public StatementContext getStatementContext()
	{
		return statementContext;
	}
}

