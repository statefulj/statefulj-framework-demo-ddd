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
package org.statefulj.demo.ddd.customer.domain;


import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Where;
import org.springframework.context.annotation.Scope;
import org.statefulj.demo.ddd.shared.domain.DomainEntity;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.model.StatefulFSM;

import static org.statefulj.demo.ddd.utils.StatefulJUtils.*;
import static org.statefulj.demo.ddd.customer.domain.Customer.*;

@Entity
@Scope("prototype")
@Table(name=TABLE)
@Where(clause="state != '" + DELETED + "'")
@StatefulController(
	value=BEAN_ID,
	clazz=Customer.class, 
	startState=START_STATE,
	noops= {
		@Transition(event=DELETE, to=DELETED)
	}
)
public class Customer extends DomainEntity<Customer> {
	
	public final static String TABLE = "customer";
	public final static String BEAN_ID = "customer";
	
	// States
	//
	public static final String UNREGISTERED = "UNREGISTERED";
	public static final String REGISTERED_UNCONFIRMED = "REGISTERED_UNCONFIRMED";
	public static final String REGISTERED_CONFIRMED = "REGISTERED_CONFIRMED";
	public static final String DELETED = "DELETED";

	public static final String START_STATE = UNREGISTERED;
	
	// Internal Events
	//
	final static String REGISTER = "register";
	final static String CONFIRM = "confirm";
	final static String CONFIRMATION_ACCEPTED = "confirmation.accepted";
	final static String DELETE = "delete";
	
	@EmbeddedId
	CustomerId customerId;
	
	ContactInfo contactInfo;
	
	String password;
	
	ConfirmationToken confirmationToken;
	
	@FSM(BEAN_ID)
	@Transient
	StatefulFSM<Customer> fsm;

	public Customer() {}
	
	public Customer(CustomerId customerId) {
		this.customerId = customerId;
	}
	
	public CustomerId getCustomerId() {
		return this.customerId;
	}

	public ContactInfo getContactInfo() {
		return contactInfo;
	}
	
	public Email getContactEmail() {
		Email email = null;
		if (this.contactInfo != null) {
			email = this.contactInfo.getEmail();
		}
		return email;
	}
	
	public void updateContactInfo(String firstName, String lastName) {
		this.contactInfo = contactInfo.update(firstName, lastName);
	}

	public void setContactInfo(ContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}

	public String getPassword() {
		return password;
	}

	public int getTokenValue() {
		return this.confirmationToken.getValue();
	}

	public void register(Long id, String password, ContactInfo contactInfo) {
		onEvent(REGISTER, id, password, contactInfo);
	}
	
	public boolean confirm(Integer tokenValue) {
		return onEvent(Boolean.class, CONFIRM, tokenValue);
	}
	
	public void delete() {
		onEvent(DELETE);
	}
	
	@Override
	protected StatefulFSM<Customer> getFSM() {
		return this.fsm;
	}

	@Transition(from=UNREGISTERED, event=REGISTER, to=REGISTERED_UNCONFIRMED)
	protected void doRegistration(String event, Long id, String password, ContactInfo contactInfo) {
		this.customerId = new CustomerId(id);
		this.contactInfo = contactInfo;
		this.password = password;
		this.confirmationToken = new ConfirmationToken();
	}

	@Transition(event=REGISTER)
	protected void doInvalidRegistration() {
		throw new RuntimeException("User is already registered");
	}

	@Transition(from=REGISTERED_UNCONFIRMED, event=CONFIRM)
	protected Object doConfirmation(String event, Integer value) {
		return (this.confirmationToken.verify(value)) ? event(CONFIRMATION_ACCEPTED) : false;
	}
	
	@Transition(event=CONFIRM)
	protected boolean doConfirmationWhileNotUnconfirmed() {
		throw new RuntimeException("Confirmation not allowed");
	}
	
	@Transition(from=REGISTERED_UNCONFIRMED, event=CONFIRMATION_ACCEPTED, to=REGISTERED_CONFIRMED)
	protected boolean doAcceptConfirmation() {
		this.confirmationToken = null;
		return true;
	}
}
