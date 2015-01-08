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
package org.statefulj.demo.ddd.notification.domain.impl;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.statefulj.demo.ddd.customer.domain.CustomerId;
import org.statefulj.demo.ddd.notification.domain.Notification;
import org.statefulj.demo.ddd.notification.domain.NotificationId;
import org.statefulj.demo.ddd.notification.domain.NotificationService;
import org.statefulj.framework.core.model.Finder;

@Service(NotificationService.ID)
@Transactional
public class NotificationServiceImpl implements NotificationService, Finder<Notification, Object> {

	@Resource
	private NotificationRepository notificationRepo;

	@PersistenceContext
	private EntityManager entityManager;

	@Resource
	private JpaTransactionManager transactionManager;	
	
	@Override
	public void save(Notification notification) {
		this.notificationRepo.save(notification);
	}

	@Transactional
	@Override
	public Long nextId() {
		return ((BigInteger)this.entityManager.createNativeQuery("CALL NEXT VALUE FOR notification_sequence").getSingleResult()).longValue();
	}
	
	@Override
	public List<Notification> findByCustomerId(CustomerId customerId) {
		return notificationRepo.findByCustomerId(customerId);
	}

	@Override
	public Notification find(Class<Notification> clazz, String event, Object context) {
		return null;
	}

	@Override
	public Notification find(Class<Notification> clazz, Object id, String event, Object context) {
		NotificationId notificationId = null;
		if (id instanceof NotificationId) {
			notificationId = (NotificationId)id;
		} else {
			notificationId = new NotificationId((Long)id);
		}
		return this.notificationRepo.findOne(notificationId);
	}

	@PostConstruct
	private void init() {
		TransactionTemplate tt = new TransactionTemplate(transactionManager);
		tt.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				return entityManager.createNativeQuery("CREATE SEQUENCE notification_sequence AS BIGINT START WITH 1").executeUpdate();
			}
			
		});
	}
}
