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
package org.statefulj.demo.ddd.customer.application;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.statefulj.demo.ddd.account.domain.AccountService;
import org.statefulj.demo.ddd.customer.domain.Customer;
import org.statefulj.demo.ddd.customer.domain.CustomerSessionService;
import org.statefulj.demo.ddd.customer.domain.RegistrationForm;
import org.statefulj.demo.ddd.exception.DuplicateUserException;
import org.statefulj.demo.ddd.notification.domain.NotificationService;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.annotations.Transitions;

import static org.statefulj.demo.ddd.customer.domain.Customer.*;

@StatefulController(
	clazz=Customer.class, 
	startState=START_STATE,
	finderId=CustomerSessionService.ID
)
class CustomerController {
	
	// Events
	//
	static final String HOMEPAGE_EVENT = "springmvc:/";
	static final String LOGIN_PAGE_EVENT = "springmvc:/login";
	static final String DETAILS_PAGE_EVENT = "springmvc:/customer";
	static final String REGISTRATION_PAGE_EVENT = "springmvc:/registration";
	static final String CONFIRMATION_PAGE_EVENT = "springmvc:/confirmation";
	static final String LOAN_PAGE_EVENT = "springmvc:/accounts/loan";
	static final String SAVINGS_PAGE_EVENT = "springmvc:/accounts/savings";
	static final String CHECKING_PAGE_EVENT = "springmvc:/accounts/checking";

	static final String REGISTER_EVENT = "springmvc:post:/registration";
	static final String CONFIRMATION_EVENT = "springmvc:post:/customer/confirmation";
	static final String DELETE_EVENT = "springmvc:/customer/delete";
	
	@Resource
	private AccountService accountService;
	
	@Resource
	private NotificationService notificationService;
	
	@Resource
	private CustomerApplicationService customerApplicationService;
	
	// -- UNREGISTERED -- //
	
	@Transition(from=UNREGISTERED, event=HOMEPAGE_EVENT)
	protected String homePage() {
 		return "index";
	}
	
	@Transition(from=UNREGISTERED, event=LOGIN_PAGE_EVENT)
	protected String loginPage() {
 		return "login";
	}
	
	@Transition(from=UNREGISTERED, event=REGISTRATION_PAGE_EVENT)
	protected String registrationPage() {
 		return "registration";
	}
	
	@Transition(from=UNREGISTERED, event=REGISTER_EVENT)
	protected String register(
			Customer customer,
			String event, 
			HttpSession session, 
			@Valid RegistrationForm regForm,
			BindingResult result,
			Model model) {
		
		// If the Registration Form is invalid, display the Registration Form
		//
		if (result.hasErrors() || !regForm.isValid()) {
			model.addAttribute("message", "Ooops... Try again");
			model.addAttribute("reg", regForm);
			return "registration";
		} else {
			customerApplicationService.registerAndLogin(customer, regForm, session);
			return "redirect:/confirmation";
		}
	}
	
	// -- REGISTERED_UNCONFIRMED -- //

	@Transition(from=REGISTERED_UNCONFIRMED, event=DETAILS_PAGE_EVENT)
	protected String redirectToConfirmation() {
		return "redirect:/confirmation";
	}
	
	@Transition(from=REGISTERED_UNCONFIRMED, event=CONFIRMATION_PAGE_EVENT)
	protected String confirmationPage(Customer customer, String event, Model model) {
		model.addAttribute("customer", customer);
		return "confirmation";
	}
	
	@Transition(from=REGISTERED_UNCONFIRMED, event=CONFIRMATION_EVENT)
	protected String confirmUser(
			Customer customer,
			String event, 
			@RequestParam Integer tokenValue) {
		
		// If a valid token, emit a "successful-confirmation" event, this will
		// transition the User into a REGISTERED_CONFIRMED state
		//
		if (customer.confirm(tokenValue)) {
			return "redirect:/customer";
		} else {
			return "redirect:/confirmation?msg=bad+token";
		}
	}

	// If we're logged in, don't display either login or registration pages
	//
	@Transitions({
		@Transition(event=HOMEPAGE_EVENT),
		@Transition(event=LOGIN_PAGE_EVENT),
		@Transition(event=REGISTRATION_PAGE_EVENT)
	})
	protected String redirectToUser() {
 		return "redirect:/customer";
	}

	// Method signature must be the same for a given event
	//
	@Transition(from=REGISTERED_CONFIRMED, event=CONFIRMATION_PAGE_EVENT)
	protected String redirectToUser(Customer customer, String event, Model model) {
 		return "redirect:/customer";
	}

	// -- REGISTERED_CONFIRMED -- //

	@Transition(from=REGISTERED_CONFIRMED, event=DETAILS_PAGE_EVENT)
	protected String userPage(Customer customer, String event, Model model) {
		model.addAttribute("customer", customer);
		model.addAttribute("accounts", accountService.findAccounts(customer.getCustomerId()));
		model.addAttribute("notifications", notificationService.findByCustomerId(customer.getCustomerId()));
		return "customer";
	}

	@Transitions({
		@Transition(from=REGISTERED_CONFIRMED, event=LOAN_PAGE_EVENT),
		@Transition(from=REGISTERED_CONFIRMED, event=SAVINGS_PAGE_EVENT),
		@Transition(from=REGISTERED_CONFIRMED, event=CHECKING_PAGE_EVENT)
	})
	protected String createAccountForm(Customer customer, String event, Model model) {
		String createAccountUri =  "/accounts"; 
		String[] parts = event.split("/");
		String type = parts[2];
		String typeTitle = WordUtils.capitalize(type);
		
		model.addAttribute("createAccountUri", createAccountUri);
		model.addAttribute("type", type);
		model.addAttribute("typeTitle", typeTitle);
		
		return "createAccount";
	}

	@Transition(from=REGISTERED_CONFIRMED, event=DELETE_EVENT)
	protected String deleteUser(Customer customer, String event) {
		customer.delete();
		return "redirect:/logout";
	}
	
	// -- Error Handling -- //

	@ExceptionHandler(DuplicateUserException.class)
	protected ModelAndView handleError(DuplicateUserException e) {
		ModelAndView mv = new ModelAndView();
		mv.getModel().put("message", "Ooops... That User is already registered.  Try a different email");
		mv.getModel().put("reg", new RegistrationForm());
		return mv;
	}
}
