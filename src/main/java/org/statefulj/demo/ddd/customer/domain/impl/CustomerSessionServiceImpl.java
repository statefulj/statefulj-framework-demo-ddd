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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.statefulj.demo.ddd.customer.domain.Customer;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionDetails;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionService;
import org.statefulj.framework.core.model.Finder;

@Service(value=CustomerSessionService.ID)
@Transactional
class CustomerSessionServiceImpl implements CustomerSessionService, UserDetailsService, Finder<Customer, HttpServletRequest> {
	
	@Resource
	CustomerRepository customerRepo;

	@Override
	public UserDetails loadUserByUsername(String customername) throws UsernameNotFoundException {
		final Customer customer = customerRepo.findByContactInfoEmailEmail(customername);
		
		if (customer == null) {
			throw new UsernameNotFoundException("Unable to locate " + customername);
		}
		
		return getDetails(customer);
	}

	@Override
	public Customer find(Class<Customer> clazz, String event, HttpServletRequest context) {
		return findLoggedInCustomer();
	}

	@Override
	public Customer find(Class<Customer> clazz, Object id, String event, HttpServletRequest context) {
		return null;
	}

	@Override
	public Customer findLoggedInCustomer() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String name = auth.getName(); //get logged in customername
		return customerRepo.findByContactInfoEmailEmail(name);
	}

	@Override
	public void login(HttpSession session, Customer customer) {
      UserDetails customerDetails = this.getDetails(customer);
      UsernamePasswordAuthenticationToken auth = 
    		  new UsernamePasswordAuthenticationToken(
    				  customerDetails, 
    				  customer.getPassword(), 
    				  customerDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);  
      session.setAttribute(
    		  HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
    		  SecurityContextHolder.getContext());  
	}
	
	private UserDetails getDetails(final Customer customer) {
		return new CustomerSessionDetails(customer);
	}

}
