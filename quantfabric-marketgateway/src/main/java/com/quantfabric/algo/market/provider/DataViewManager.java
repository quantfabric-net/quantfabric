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
package com.quantfabric.algo.market.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quantfabric.algo.market.dataprovider.DataView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quantfabric.cep.StatementDefinition;
import com.quantfabric.cep.StatementDefinitionImpl;

class DataViewManager {
	
	private static class DataViewInstance {
		
		private final DataView definition;
		private final Map<Integer,Integer> instances= new  HashMap<Integer,Integer>();
		
		public DataViewInstance(DataView desc){
			definition=desc;
		}
		
		public DataView getDefinition() {
			return definition;
		}
		public int AddRef(Map<String,String> context){
			int key =context.hashCode();
			int count = 0;
			if(instances.containsKey(key))
				 count = instances.get(key);
			count++;
			instances.put(key, count);
			return count;
		}
		public int removeRef(Map<String,String> context){
			int key =context.hashCode();
			int count = 0;
			if(instances.containsKey(key))
				 count = instances.get(key);
			if(count > 0)count--;
			instances.put(key, count);
			return count;
			
		}
		public List<StatementDefinitionImpl> getStatements(Map<String,String> context){
			List<StatementDefinitionImpl> stmts = new ArrayList<StatementDefinitionImpl>();
			
			for(StatementDefinition stmt : getDefinition().getStatements()){
				stmts.add(new StatementDefinitionImpl(
						getPrefix(context).concat(stmt.getName()), 
						replaceParams(stmt.getStatement(), context), 
						stmt.getPersistMode(), 
						stmt.isDebugMode()));
			}
			return stmts;
		}

		private String getPrefix(Map<String,String> context){
			String pref= definition.getPrefix();
			if(pref=="" || !context.containsKey(pref))return "";
			return converToFriendlyName(context.get(pref));
		}
		
		
 		private String replaceParams(final String text, final Map<String,String> params){
	        final Matcher      matcher = Pattern.compile("(\\$\\$(\\p{L}\\w+)\\$\\$)|(@@(\\p{L}\\w+)@@)").matcher(text);
	        final StringBuffer result  = new StringBuffer();
	        while(matcher.find()) {
	            if (matcher.group(2) != null)
	            {
	            	final String match       = matcher.group(2);
	            	final String replacement = converToFriendlyName(params.get(match));
	            	matcher.appendReplacement(result, replacement);
	            }
	            if (matcher.group(4) != null)
	            {
	            	final String match       = matcher.group(4);
	            	final String replacement = params.get(match);
	            	matcher.appendReplacement(result, replacement);
	            }
	        }
	        return matcher.appendTail(result).toString();
		}

		private String converToFriendlyName(String string)
		{
			return string.replaceAll("\\W", "_");
		}
	}

	private static final Logger log = LoggerFactory.getLogger(DataViewManager.class);
	private StatementProvider statementManager;
	private final Map<String,DataViewInstance> dataViewsInstances = new HashMap<String,DataViewInstance>();

	public DataViewManager(){}

	public DataViewManager(StatementProvider provider){
		statementManager = provider;
	}
	public void setStatementProvider(StatementProvider provider){
		statementManager = provider;
	}
	public void addDescription(DataView description) throws Exception{
		
		DataViewInstance view = new DataViewInstance(description);
		dataViewsInstances.put(description.getName(), view);
		if(description.isStatic()) {
			activateView(view,new HashMap<String,String>());
		}
	}
	public void activateViews(Set<String> views, Map<String,String> context) throws Exception{
		for (String viewName : views) {
			activateView(viewName,context);
		}
	}
	public void activateView(String name,Map<String,String> context) throws Exception{
		if (statementManager == null)
			throw new Exception("Can not activate a data view. StatementProvider is not set");
		DataViewInstance instance = dataViewsInstances.get(name);
		activateView(instance,context);
	}
	private void activateView(DataViewInstance instance,Map<String,String> context) throws Exception{
		log.debug("*******************");
		log.debug("try activating view:" + instance.definition.getName() + "**" + dumpCtx(context));
		if(!instance.definition.isStatic() || instance.instances.size()==0) {
			int instCount=instance.AddRef(context);
			if(instCount == 1){
				log.debug("************ activating view:" + instance.definition.getName()+ " count:"+instCount+ " context: " + dumpCtx(context));
				//activate dependences views
				for (String view : instance.getDefinition().getDependences()) {
					activateView(view,context);
				}
				//activate the view statement
				for(StatementDefinition stmt : instance.getStatements(context)){
					log.debug("reg statement view:" + instance.definition.getName() + " stmt name:" + stmt.getName()+" statement:" + stmt.getStatement());
					statementManager.registerStatement(	stmt);
				}
			}
		}
	}
	private final String dumpCtx(Map<String,String> context) {
		StringBuilder sb = new StringBuilder();
		for (String key : context.keySet()) {
			sb.append(key + ":");
			sb.append(context.get(key) +"; ");
		}
		return sb.toString();
	}
	
}
