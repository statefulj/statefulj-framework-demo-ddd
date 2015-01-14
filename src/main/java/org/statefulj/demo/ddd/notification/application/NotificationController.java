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
package org.statefulj.demo.ddd.notification.application;

import javax.annotation.Resource;

import org.statefulj.demo.ddd.account.domain.Account;
import org.statefulj.demo.ddd.account.domain.AccountApplicationNotificationEvent;
import org.statefulj.demo.ddd.notification.domain.Notification;
import org.statefulj.demo.ddd.notification.domain.NotificationService;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.annotations.Transitions;

import static org.statefulj.demo.ddd.notification.domain.Notification.*;

@StatefulController(
	clazz=Notification.class,
	startState=NON_EXISTENT,
	finderId=NotificationService.ID
)
public class NotificationController {
	
	// EVENTS
	//
	public static final String ACCOUNT_APPROVED_EVENT = "camel:" + Account.ACCOUNT_APPLICATION_APPROVED;
	public static final String ACCOUNT_REJECTED_EVENT = "camel:" + Account.ACCOUNT_APPLICATION_REJECTED;
	public static final String DELETE_EVENT = "jersey:delete:/notifications/{id}";
	
	@Resource
	NotificationApplicationService notificationApplicationService;
	
	@Transitions({
		@Transition(event=ACCOUNT_APPROVED_EVENT),
		@Transition(event=ACCOUNT_REJECTED_EVENT)
	})
	public void applicationReviewed(Notification notification, String event, AccountApplicationNotificationEvent response) {
		notificationApplicationService.notify(
				notification, 
				response.getCutstomerId(), 
				response.getAccountId(), 
				response.getType(), 
				response.getReason());
	}
	
	@Transition(event=DELETE_EVENT)
	public void deleteNotification(Notification notification) {
		notification.delete();
	}
}
