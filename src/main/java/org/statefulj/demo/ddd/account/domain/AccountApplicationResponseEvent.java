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
package org.statefulj.demo.ddd.account.domain;

import org.springframework.data.annotation.Id;
import org.statefulj.demo.ddd.customer.domain.CustomerId;

public class AccountApplicationResponseEvent {
	
	@Id
	private AccountId accountId;
	
	private CustomerId customerId;
	
	private boolean approved;
	
	private String reason;
	
	private String type;
	
	public AccountApplicationResponseEvent(
			AccountId accountId, 
			CustomerId customerId, 
			boolean approved, 
			String type,
			String reason) {
		this.accountId = accountId;
		this.customerId = customerId;
		this.approved = approved;
		this.type = type;
		this.reason = reason;
	}
	
	public AccountApplicationResponseEvent(
			CustomerId customerId, 
			boolean approved, 
			String type,
			String reason) {
		this.customerId = customerId;
		this.approved = approved;
		this.type = type;
		this.reason = reason;
	}
	
	public AccountId getAccountId() {
		return this.accountId;
	}

	public CustomerId getCutstomerId() {
		return this.customerId;
	}

	public boolean isApproved() {
		return approved;
	}

	public String getReason() {
		return reason;
	}

	public String getType() {
		return type;
	}
}
