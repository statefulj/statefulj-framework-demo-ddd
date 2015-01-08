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
package org.statefulj.demo.ddd.notification.domain;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Where;
import org.springframework.context.annotation.Scope;
import org.statefulj.demo.ddd.account.domain.AccountId;
import org.statefulj.demo.ddd.customer.domain.CustomerId;
import org.statefulj.demo.ddd.shared.domain.DomainEntity;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.model.StatefulFSM;

import static org.statefulj.demo.ddd.customer.domain.Customer.DELETED;
import static org.statefulj.demo.ddd.notification.domain.Notification.*;

@Entity
@Scope("prototype")
@Table(name=TABLE)
@Where(clause="state != '" + DELETED + "'")
@StatefulController(
	value=Notification.BEAN_ID,
	clazz=Notification.class,
	startState=NON_EXISTENT,
	noops={
		@Transition(from=SHOWING, event=DELETE, to=DELETED)
	}
)
public class Notification extends DomainEntity<Notification> {
	
	public static final String BEAN_ID = "notification";
	
	public static final String TABLE = "Notification";
	
	// States
	//
	public static final String NON_EXISTENT = "NON_EXISTENT";
	public static final String SHOWING = "SHOWING";
	public static final String DELETED = "DELETED";

	// Internal Events
	//
	static final String NOTIFY = "notify";
	static final String DELETE = "delete";

	@EmbeddedId
	private NotificationId notificationId;
	
	private AccountId accountId;
	
	private CustomerId customerId;
	
	private Type type;
	
	private Message message;
	
	@FSM(BEAN_ID)
	@Transient
	private StatefulFSM<Notification> fsm;
	
	public NotificationId getNotificationId() {
		return this.notificationId;
	}
	
	public String getType() {
		return this.type.getType();
	}

	public String getMessage() {
		return this.message.getMessage();
	}

	public AccountId getAccountId() {
		return this.accountId;
	}
	
	public CustomerId getCustomerId() {
		return this.customerId;
	}
	
	@Override
	protected StatefulFSM<Notification> getFSM() {
		return this.fsm;
	}
	
	public void notify(Long id, CustomerId customerId, AccountId accountId, String type, String message) {
		onEvent(NOTIFY, id, customerId, accountId, type, message);
	}
	
	public void delete() {
		onEvent(DELETE);
	}
	
	@Transition(from=NON_EXISTENT, event=NOTIFY, to=SHOWING)
	protected void doNotify(String event, Long id, CustomerId customerId, AccountId accountId, String type, String message) {
		this.notificationId = new NotificationId(id);
		this.type = new Type(type);
		this.message = new Message(message);
		this.customerId = customerId;
		this.accountId = accountId;
	}
}
