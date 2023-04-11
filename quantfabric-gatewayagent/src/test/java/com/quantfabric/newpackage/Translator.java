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
package com.quantfabric.newpackage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.quantfabric.algo.market.datamodel.IncrementalUpdate;
import com.quantfabric.algo.market.gateway.access.product.Description;
import com.quantfabric.algo.market.gate.access.product.producer.FullBook;
import com.quantfabric.algo.market.gateway.access.product.subscriber.Subscriber;
import com.quantfabric.net.stream.Event;

@SuppressWarnings("unchecked")
public class Translator implements Subscriber {

	final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z(Z)");

	private final Description d;

	private boolean isSubscribed = false;
	
	private FullBook lastSnapshot;
	private long id;
	private final Buffer iQ = new CircularFifoBuffer(15);
	private final Buffer sQ = new CircularFifoBuffer(3);
	
	private final Map<IncrementalUpdate, String> sourceMap = new HashMap<IncrementalUpdate, String>();
	
	private final Object lock = new Object();
	private volatile boolean isSubscribeInProgress = false;
	
	public Translator(Description productDesc) {
		System.out.println("Translator created");
		this.d = productDesc;
	}

	@Override
	public void update(Event event) {
		if (event.getSource() == null) {
			System.out.println("EVENT SOURCE IS NULL");
		}
		System.out.println("in " + timeFormat.format(new Date()) + " - " + d + " :" + event);
		if (event.getEventBean() instanceof FullBook) {
			
			lastSnapshot = (FullBook) event.getEventBean();
			sQ.add(lastSnapshot);
		}
		if (event.getEventBean() instanceof IncrementalUpdate) {
			IncrementalUpdate incremental = (IncrementalUpdate) event.getEventBean();
			
			sourceMap.put(incremental, event.getSource());
			if (isSubscribeInProgress) {
				synchronized (lock){}
				
			}
			iQ.add(event);
			if (isSubscribed) {
				System.out.println("out " + timeFormat.format(new Date()) + " - " + d + " :" + event);
			}
		}	
	}

	public boolean subscribe() throws InterruptedException {
		
		synchronized (lock) {
			isSubscribeInProgress = true;
			
			System.out.println("subscribe request");
			
			lastSnapshot = (FullBook) sQ.get();
			
			id = lastSnapshot.getSnapshotId();
			
			System.out.println("--->>> sending snapshot " + lastSnapshot);
			checkUnsentIncrementals(iQ.iterator(), id);

			isSubscribed = true;
			isSubscribeInProgress = false;
			
			return isSubscribed;
		}
	}
	
	private void checkUnsentIncrementals(Iterator<Event> iter, long id) throws InterruptedException{
		while (iter.hasNext()) {
			IncrementalUpdate incr = (IncrementalUpdate) iter.next().getEventBean();
			if (incr == null) {
				
				return;
			}
			if (incr.getSnapshotId() > id) {
				
				System.out.println("<<<--- sending incr update [" + sourceMap.get(incr) + "] " + incr);				
				
			}
		}
	}
}
