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
package com.quantfabric.algo.trading.strategy.settings.viewlayout;

import java.util.ArrayList;
import java.util.HashMap;

public class LayoutDefinition {
	
	private final HashMap<String, ParametersView> parametersViewsMap = new HashMap<String, ParametersView>();
	private final ArrayList<ParametersView> parametersViews = new  ArrayList<ParametersView>();
	
	public void addParameterView(String viewId, ParametersView view)
	{
		parametersViewsMap.put(viewId, view);
		parametersViews.add(view);
	}
	public ParametersView getParametersView(String viewId)
	{
		if(parametersViewsMap.containsKey(viewId))
			return parametersViewsMap.get(viewId);
		else
			return null;
	}
	public boolean containsParametersView(String viewId)
	{
        return parametersViewsMap.containsKey(viewId);
	}
	public ArrayList<ParametersView> getParametersViews()
	{
		return  parametersViews;
	}
}
