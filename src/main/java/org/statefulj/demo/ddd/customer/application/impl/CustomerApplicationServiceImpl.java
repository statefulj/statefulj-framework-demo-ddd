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
package org.statefulj.demo.ddd.customer.application.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.statefulj.demo.ddd.customer.application.CustomerApplicationService;
import org.statefulj.demo.ddd.customer.domain.ContactInfoFactory;
import org.statefulj.demo.ddd.customer.domain.Customer;
import org.statefulj.demo.ddd.customer.domain.CustomerService;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionService;
import org.statefulj.demo.ddd.customer.domain.RegistrationForm;
import org.statefulj.demo.ddd.exception.DuplicateUserException;

@Service(CustomerApplicationService.ID)
class CustomerApplicationServiceImpl implements CustomerApplicationService {

	@Resource
	private CustomerService customerService;

	@Resource
	private CustomerSessionService customerSessionService;

	@Override
	public void registerAndLogin(Customer customer, RegistrationForm regForm, HttpSession session) {
		try {
			Long id = customerService.nextId();
			customer.register(id, regForm.getPassword(), ContactInfoFactory.derivedFrom(regForm));
			customerService.save(customer);
			customerSessionService.login(session, customer);
		} catch(Exception e) {
			if (e.getCause() instanceof ConstraintViolationException) {
				throw new DuplicateUserException();
			} else {
				throw new RuntimeException(e);
			}
		}
	}

}
