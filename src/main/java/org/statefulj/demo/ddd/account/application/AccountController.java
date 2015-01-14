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
package org.statefulj.demo.ddd.account.application;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.ModelAndView;
import org.statefulj.demo.ddd.account.domain.Account;
import org.statefulj.demo.ddd.account.domain.AccountApplicationResponseEvent;
import org.statefulj.demo.ddd.account.domain.AccountService;
import org.statefulj.demo.ddd.account.domain.ApplicationForm;
import org.statefulj.demo.ddd.customer.domain.Customer;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionService;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;

import static org.statefulj.demo.ddd.account.application.AccountRules.*;
import static org.statefulj.demo.ddd.account.domain.Account.*;

@StatefulController(
	value=AccountController.BEAN_ID,
	clazz=Account.class,
	startState=NON_EXISTENT,
	finderId=AccountService.ID,
	factoryId=AccountService.ID
)
public class AccountController {
	
	public static final String BEAN_ID = "accountApplication";

	// Events
	//
	static final String ACCOUNT_APPROVED_EVENT = "camel:" + ACCOUNT_APPROVED;
	static final String ACCOUNT_REJECTED_EVENT = "camel:" + ACCOUNT_REJECTED;
	static final String ACCOUNT_APPLICATION_EVENT = "springmvc:post:/accounts";
	static final String ACCOUNT_DISPLAY_EVENT = "springmvc:/accounts/{id}";
	
	@Resource
	private CustomerSessionService customerSessionService;
	
	@Resource
	private AccountApplicationService accountApplicationService;
	
	@Transition(event=ACCOUNT_APPLICATION_EVENT)
	public String applyForAccount(Account account, String event, ApplicationForm form) {
		Customer customer = customerSessionService.findLoggedInCustomer();
		accountApplicationService.applyForAccount(account, customer.getCustomerId(), form);
		return "redirect:/customer";
	}

	@Transition(event=ACCOUNT_APPROVED_EVENT)
	public void accountApproved(Account account, String event, AccountApplicationResponseEvent response) {
		account.approve(response);
	}
	
	@Transition(event=ACCOUNT_REJECTED_EVENT)
	public void accountRejected(Account account, String event, AccountApplicationResponseEvent response) {
		account.reject(response);
	}
	
	// Make sure that only the owner can access the account
	//
	@Transition(event=ACCOUNT_DISPLAY_EVENT)
	@PreAuthorize("#account.customerId.id == principal.customerId.id")
	public ModelAndView displayAccount(Account account, String event) {
		ModelAndView mv = new ModelAndView("account");
		mv.addObject("account", account);
		return mv;
	}
}
