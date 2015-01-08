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

import java.math.BigInteger;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.statefulj.demo.ddd.account.domain.Account;
import org.statefulj.demo.ddd.account.domain.AccountId;
import org.statefulj.demo.ddd.account.domain.AccountService;
import org.statefulj.demo.ddd.account.domain.CheckingAccount;
import org.statefulj.demo.ddd.account.domain.LoanAccount;
import org.statefulj.demo.ddd.account.domain.SavingsAccount;
import org.statefulj.demo.ddd.customer.domain.CustomerId;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionService;
import org.statefulj.framework.core.model.Factory;
import org.statefulj.framework.core.model.Finder;

@Transactional
@Service(AccountService.ID)
public class AccountServiceImpl implements 
	AccountService, 
	Finder<Account, Object>, 
	Factory<Account, HttpServletRequest> {
	
	@Resource
	AccountRepository accountRepository;

	@Resource
	CustomerSessionService userSessionService;
	
	@PersistenceContext
	EntityManager entityManager;

	@Resource
	JpaTransactionManager transactionManager;	
	
	@Override
	public List<Account> findAccounts(CustomerId customerId) {
		return accountRepository.findByCustomerId(customerId);
	}

	@Override
	public Account create(Class<Account> clazz, String event, HttpServletRequest request) {
		Account account = null;
		
		switch(request.getParameter("type")) {
		
			case "checking" :
				account = new CheckingAccount();
				break;
		
			case "savings" :
				account = new SavingsAccount();
				break;
				
			case "loan" :
				account = new LoanAccount();
				break;
				
			default :
				throw new RuntimeException("Unrecognized account type " + request.getParameter("type"));
		}
		
		return account;
	}

	@Override
	public void save(Account account) {
		this.accountRepository.save(account);
	}

	@Transactional
	@Override
	public Long nextId() {
		return ((BigInteger)entityManager.createNativeQuery("CALL NEXT VALUE FOR account_sequence").getSingleResult()).longValue();
	}
	
	@PostConstruct
	private void init() {
		TransactionTemplate tt = new TransactionTemplate(transactionManager);
		tt.execute(new TransactionCallback<Object>() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				return entityManager.createNativeQuery("CREATE SEQUENCE account_sequence AS BIGINT START WITH 1").executeUpdate();
			}
			
		});
	}

	@Override
	public Account find(Class<Account> clazz, String event, Object context) {
		return null;
	}

	@Override
	public Account find(Class<Account> clazz, Object id, String event,
			Object context) {
		AccountId accountId = null;
		if (id instanceof AccountId) {
			accountId = (AccountId)id;
		} else {
			accountId = new AccountId((Long)id);
		}
		return this.accountRepository.findOne(accountId);
	}
}