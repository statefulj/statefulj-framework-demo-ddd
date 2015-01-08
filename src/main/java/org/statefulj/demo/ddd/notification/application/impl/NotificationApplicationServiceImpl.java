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
package org.statefulj.demo.ddd.notification.application.impl;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.statefulj.demo.ddd.account.domain.AccountId;
import org.statefulj.demo.ddd.customer.domain.CustomerId;
import org.statefulj.demo.ddd.notification.application.NotificationApplicationService;
import org.statefulj.demo.ddd.notification.domain.Notification;
import org.statefulj.demo.ddd.notification.domain.NotificationService;

@Service(NotificationApplicationService.ID)
@Transactional
public class NotificationApplicationServiceImpl implements
		NotificationApplicationService {

	@Resource
	NotificationService notificationService;
	
	@Override
	public void notify(Notification notification, CustomerId customerId, AccountId accountId, String type, String message) {
		Long id = this.notificationService.nextId();
		notification.notify(id, customerId, accountId, type, message);
		this.notificationService.save(notification);
	}

}
