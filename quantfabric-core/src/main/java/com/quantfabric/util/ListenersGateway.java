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
package com.quantfabric.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ListenersGateway<T> implements InvocationHandler
{
	private final Set<T> listeners = new HashSet<T>();
	
	public void attachListener(T listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}
	
	public void detachListener(T listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	public int getListenersCount()
	{
		synchronized (listeners)
		{
			return listeners.size();
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable
	{	
		if (method.getName().equals("toString"))
			return this.toString();
		else
			if (method.getName().equals("equals"))
				return this.equals(args[0]);
			else
				if (method.getName().equals("hashCode"))
					return this.hashCode();
		
		synchronized (listeners)
		{	
			for (T listener : new ArrayList<T>(listeners))
				method.invoke(listener, args);				
		}		
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getGatewayProxy(ListenersGateway<T> gateway, Class<T> listenerInteface) throws IllegalArgumentException
	{
		for (Method m : listenerInteface.getDeclaredMethods())
			if (m.getReturnType() == Void.class)
				throw new IllegalArgumentException("interface must have only void method");

		return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class<?>[]{listenerInteface}, gateway);
	}

	@Override
	public String toString()
	{
		return super.toString() + "[listeners=" + getListenersCount() +"]";
	}
}
