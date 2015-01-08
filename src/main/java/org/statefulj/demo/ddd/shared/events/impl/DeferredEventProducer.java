/***
 * 
 * Copyright 2014 Andrew Hall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.statefulj.demo.ddd.shared.events.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.statefulj.demo.ddd.shared.domain.tx.TransactionObserver;
import org.statefulj.demo.ddd.shared.events.EventProducer;

/**
 * Class supports broadcasting events only after the Transaction has been completed.  This ensures
 * that the originating class has been persisted to the database prior to clients receiving the events
 *
 */
public class DeferredEventProducer implements EventProducer, TransactionObserver {

	@Produce
	private ProducerTemplate producerTemplate;
	
	private String uri;
	
	ThreadLocal<List<Object>> deferredEvents = new ThreadLocal<List<Object>>();
	
	public DeferredEventProducer(String uri) {
		this.uri = uri;
	}
	
	@Override
	public void emit(Object event) {
		List<Object> deferredEvents = this.deferredEvents.get();
		if (deferredEvents == null) {
			deferredEvents = new LinkedList<Object>();
			this.deferredEvents.set(deferredEvents);
		}
		deferredEvents.add(event);
	}

	@Override
	public void onComplete(Object transaction) {
		List<Object> deferredEvents = this.deferredEvents.get();
		this.deferredEvents.remove();
		if (deferredEvents != null) {
			for (Object event : deferredEvents) {
				this.producerTemplate.sendBody(this.uri, event);
			}
		}
	}

}
