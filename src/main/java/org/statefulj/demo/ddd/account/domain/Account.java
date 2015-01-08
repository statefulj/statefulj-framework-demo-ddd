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

import javax.annotation.Resource;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.statefulj.demo.ddd.customer.domain.CustomerId;
import org.statefulj.demo.ddd.shared.domain.DomainEntity;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;

import static org.statefulj.demo.ddd.account.domain.Account.*;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Table(name=TABLE)
@StatefulController(
	clazz=Account.class,
	startState=NON_EXISTENT,
	noops={
		@Transition(from=APPROVAL_PENDING, event=APPROVE, to=ACTIVE),
		@Transition(from=APPROVAL_PENDING, event=REJECT, to=REJECTED)
	}
)
public abstract class Account extends DomainEntity<Account> {
	
	public static final String TABLE = "Account";

	// External Events
	//
	public static final String ACCOUNT_APPLICATION = "direct:account.application";
	public static final String ACCOUNT_APPLICATION_APPROVED = "direct:account.application.approved";
	public static final String ACCOUNT_APPLICATION_REJECTED = "direct:account.application.rejected";
	
	// States
	//
	public static final String NON_EXISTENT = "NON_EXISTENT";
	public static final String ACTIVE = "ACTIVE";
	public static final String DELETED = "DELETED";
	public static final String APPROVAL_PENDING = "APPROVAL_PENDING";
	public static final String REJECTED = "REJECTED";

	// Internal Events
	//
	static final String APPLY = "apply";
	static final String APPROVE = "approve";
	static final String REJECT = "reject";

	@EmbeddedId
	private AccountId accountId;
	
	private CustomerId customerId;
	
	private BigDecimal amount;
	
	@Resource
	@Transient
	private AccountEventProducer accountEventProducer;

	public AccountId getAccountId() {
		return accountId;
	}

	public CustomerId getCustomerId() {
		return customerId;
	}

	public BigDecimal getAmount() {
		return amount;
	}
	
	public void apply(Long id, CustomerId customerId, ApplicationForm applicationForm) {
		onEvent(APPLY, id, customerId, applicationForm);
	}
	
	public void approve(AccountApplicationResponseEvent response) {
		onEvent(APPROVE, response);
	}
	
	public void reject(AccountApplicationResponseEvent response) {
		onEvent(REJECT, response);
	}
	
	@Transient
	public abstract String getType();

	public String toString() {
		return getType() + ": state=" + getState();
	}

	@Transition(from=NON_EXISTENT, event=APPLY, to=APPROVAL_PENDING)
	protected void doApply(String event, Long id, CustomerId customerId, ApplicationForm applicationForm) {
		this.accountId = new AccountId(id);
		this.amount = applicationForm.getAmount();
		this.customerId = customerId;
		
		AccountApplicationEvent appEvent = new AccountApplicationEvent(
				accountId, 
				customerId,
				getType(),
				applicationForm.getAmount());
		
		this.accountEventProducer.submitForReview(appEvent);
	} 

	protected AccountEventProducer getAccountEventProducer() {
		return this.accountEventProducer;
	}
}
