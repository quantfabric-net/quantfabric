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
package com.quantfabric.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BasePublisher<TS extends Subscriber<TD>,
							T extends TargetSubject,
							TD> implements Publisher<TS,T,TD>{

	private static final Logger log = LoggerFactory.getLogger(BasePublisher.class);
	 
	private final LinkedHashMap<Integer,TS> subscribers = new LinkedHashMap<>();
	private final Map<Integer,ArrayList<T>> subscriptions = new HashMap<>();
	private final Map<Long, ArrayList<TS>> subscribersByTargetObject =
			new HashMap<>(); //T.getId(), Array of subscribers

	//region Interface Publisher
	@Override
	public void registerSubscriber(TS subscriber) {
		synchronized(subscribers){
			int key = subscriber.hashCode();
			if(!subscribers.containsKey(key)){
				subscribers.put(key,subscriber);
				subscriptions.put(key, new ArrayList<>());
			}
			else
				log.warn("Subscriber is already registered");
		}
	}
	@Override
	public void unregisterSubscriber(TS subscriber) {
		synchronized(subscribers){
			int key = subscriber.hashCode();
			if(subscribers.containsKey(key))
				subscribers.remove(key);
			else {
				log.warn("Subscriber isn't registered");
			}
		}
		//TODO : remove all subscriptions of this subscriber
	}
	@Override
	public void subscribe(TS subscriber, T subject) throws PublisherException {
		try {
			registerSubscriber(subscriber);
			int id = subscriber.hashCode();
			subscribe(id,subject);
		}
		catch(PublisherException e) {
			throw e;
		}
		catch(Exception ex) {
			throw new PublisherException(ex);
		}
		
	}
	@Override
	public void subscribe(int subscriberId, T subject) throws PublisherException {
		try {
			if(subscriptions.containsKey(subscriberId)){
				subscriptions.get(subscriberId).add(subject);

				TS subscriber = subscribers.get(subscriberId);

				registerSubject(subject);
				subscribersByTargetObject.get(subject.getId()).add(subscriber);

				initSubscription(subscriber, subject);
			}
		}
		catch(Exception ex) {
			throw new PublisherException(ex);
		}
	}
	@Override
	public void unSubscribe(TS subscriber, T subject) {
		int id = subscriber.hashCode();
		unSubscribe(id,subject);
	}
	@Override
	public void unSubscribe(int subscriberId, T subject) {
		if(subscriptions.containsKey(subscriberId)){
			subscriptions.get(subscriberId).remove(subject);

			TS subscriber = subscribers.get(subscriberId);

			subscribersByTargetObject.get(subject.getId()).remove(subscriber);

			destroySubscription(subscriber, subject);
		}
	}
	@Override
	public void publish(TD data) throws PublisherException {
		throw new NotImplementedException();
	}
	//endregion Interface Publisher
	private void registerSubject(T subject)
	{
		if (!subscribersByTargetObject.containsKey(subject.getId()))
			subscribersByTargetObject.put(subject.getId(), new ArrayList<>());
	}
	protected void initSubscription(TS subscriber, T subject) throws Exception{}
	protected void destroySubscription(TS subscriber, T subject){}
	protected  Collection<TS> getSubscribers(){
		return subscribers.values();
	}
	public Collection<TS> getSubscribers(T subject)
	{
		return subscribersByTargetObject.get(subject.getId());
	}
}