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
package org.statefulj.demo.ddd.customer.domain.impl;

import java.math.BigInteger;

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
import org.statefulj.demo.ddd.customer.domain.Customer;
import org.statefulj.demo.ddd.customer.domain.CustomerService;

@Service(value=CustomerService.ID)
@Transactional
class CustomerServiceImpl implements CustomerService {
	
	@Resource
	CustomerRepository customerRepo;

	@PersistenceContext
	EntityManager entityManager;

	@Resource
	JpaTransactionManager transactionManager;	
	
	@Override
	public void save(Customer user) {
		customerRepo.save(user);
		entityManager.flush();
	}

	@Transactional
	@Override
	public Long nextId() {
		return ((BigInteger)entityManager.createNativeQuery("CALL NEXT VALUE FOR customer_sequence").getSingleResult()).longValue();
	}
	
	@PostConstruct
	private void init() {
		TransactionTemplate tt = new TransactionTemplate(transactionManager);
		tt.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				return entityManager.createNativeQuery("CREATE SEQUENCE customer_sequence AS BIGINT START WITH 1").executeUpdate();
			}
			
		});
	}
}
