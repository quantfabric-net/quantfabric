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

import java.util.List;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.epl.expression.ExprNodeUtility;
import com.espertech.esper.view.DataWindowViewFactory;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;
import com.espertech.esper.view.window.RandomAccessByIndexGetter;

public class UniqueByPropertyWindowViewFactory implements DataWindowViewFactory
{
	 /**
     * View parameters.
     */
    protected List<ExprNode> viewParameters;
    /**
     * List of criteria expressions.
     */
    protected ExprNode[] criteriaExpressions;

    private EventType eventType;
    /**
     * The access into the data window.
     */
    protected RandomAccessByIndexGetter randomAccessGetterImpl;
    /**
     * Flag to indicate that the view must handle the removed events from a parent view.
     */
    protected boolean isRemoveStreamHandling;

    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> expressionParameters) throws ViewParameterException
    {
        this.viewParameters = expressionParameters;
    }

    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories) throws ViewParameterException
    {
        criteriaExpressions = ViewFactorySupport.validate("Unique-by view", parentEventType, statementContext, viewParameters, false);

        if (criteriaExpressions.length == 0)
        {
            String errorMessage = "Unique-by view requires a one or more expressions provinding unique values as parameters";
            throw new ViewParameterException(errorMessage);
        }
        
        this.eventType = parentEventType;
    }

    
    /*public boolean canProvideCapability(ViewCapability viewCapability)
    {
        if (viewCapability instanceof ViewCapDataWindowAccess)
        {
            return true;
        }
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            return true;
        }
        else
        {
            return false;
        }
    }*/

   /* public void setProvideCapability(ViewCapability viewCapability, ViewResourceCallback resourceCallback)
    {
        if (!canProvideCapability(viewCapability))
        {
            throw new UnsupportedOperationException("View capability " + viewCapability.getClass().getSimpleName() + " not supported");
        }
        if (viewCapability instanceof RemoveStreamViewCapability)
        {
            isRemoveStreamHandling = true;
            return;
        }
        if (randomAccessGetterImpl == null)
        {
            randomAccessGetterImpl = new RandomAccessByIndexGetter();
        }
        resourceCallback.setViewResource(randomAccessGetterImpl);
    }*/

    public View makeView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext)
    {
        return new UniqueByPropertyWindowView(agentInstanceViewFactoryContext.getStatementContext() , criteriaExpressions, ExprNodeUtility.getEvaluators(criteriaExpressions));
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public boolean canReuse(View view)
    {
        if (!(view instanceof UniqueByPropertyWindowView))
        {
            return false;
        }

        if (randomAccessGetterImpl != null)
        {
            return false;
        }

        UniqueByPropertyWindowView myView = (UniqueByPropertyWindowView) view;
        if(!ExprNodeUtility.deepEquals(criteriaExpressions, myView.getCriteriaExpressions()))
        {
            return false;
        }
        return myView.isEmpty();
    }
    
}
