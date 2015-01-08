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

import java.math.BigDecimal;

import javax.persistence.Id;

import org.statefulj.demo.ddd.customer.domain.CustomerId;

public class AccountApplicationEvent {
	
	@Id
	private AccountId accountId;
	
	private CustomerId customerId;
	
	private BigDecimal amount;
	
	private String type;
	
	public AccountApplicationEvent(
			AccountId accountId, 
			CustomerId customerId, 
			String type, 
			BigDecimal amount) {
		this.accountId = accountId;
		this.customerId = customerId;
		this.type = type;
		this.amount = amount;
	}

	public AccountId getAccountId() {
		return this.accountId;
	}

	public CustomerId getCustomerId() {
		return this.customerId;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public String getType() {
		return this.type;
	}
}
