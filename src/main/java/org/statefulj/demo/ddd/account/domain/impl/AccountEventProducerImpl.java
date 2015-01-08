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
package org.statefulj.demo.ddd.account.domain.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.statefulj.demo.ddd.account.domain.AccountApplicationEvent;
import org.statefulj.demo.ddd.account.domain.AccountApplicationResponseEvent;
import org.statefulj.demo.ddd.account.domain.AccountEventProducer;
import org.statefulj.demo.ddd.shared.events.EventProducer;

@Service(AccountEventProducer.ID)
class AccountEventProducerImpl implements AccountEventProducer {

	@Resource
	EventProducer accountApplicationEventProducer;
	
	@Resource
	EventProducer accountApprovedEventProducer;
	
	@Resource
	EventProducer accountRejectedEventProducer;
	
	@Override
	public void submitForReview(AccountApplicationEvent event) {
		this.accountApplicationEventProducer.emit(event);
	}

	@Override
	public void applicationApproved(AccountApplicationResponseEvent response) {
		this.accountApprovedEventProducer.emit(response);
	}

	@Override
	public void applicationRejected(AccountApplicationResponseEvent response) {
		this.accountRejectedEventProducer.emit(response);
	}

}
