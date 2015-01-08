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
package org.statefulj.demo.ddd.shared.domain.tx;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.JpaTransactionManager;

public class ObservableTransactionManager extends JpaTransactionManager {

	private static final long serialVersionUID = 1L;

	private List<TransactionObserver> observers;
	
	public ObservableTransactionManager(EntityManagerFactory emf, List<TransactionObserver> observers) {
		super(emf);
		this.observers = observers;
	}
	
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		super.doCleanupAfterCompletion(transaction);
		for (TransactionObserver observer : this.observers) {
			observer.onComplete(transaction);
		}
	}
}
