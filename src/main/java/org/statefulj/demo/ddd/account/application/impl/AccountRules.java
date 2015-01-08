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
package org.statefulj.demo.ddd.account.application.impl;

import java.util.Calendar;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.math.RandomUtils;
import org.statefulj.demo.ddd.account.domain.AccountApplicationEvent;
import org.statefulj.demo.ddd.account.domain.AccountApplicationResponseEvent;
import org.statefulj.demo.ddd.account.domain.LoanAccount;

import static org.statefulj.demo.ddd.account.domain.Account.*;

import static org.apache.camel.ExchangePattern.*;

public class AccountRules {
	static Random random = new Random(Calendar.getInstance().getTimeInMillis());
	
	// Public Queues
	//
	public static final String ACCOUNT_APPROVED = "direct:account.approved";
	public static final String ACCOUNT_REJECTED = "direct:account.rejected";
	
	// Private Queues
	//
	private static final String REVIEW_LOAN = "vm:loan.application.review";

	// Determines whether the application is a loan
	//
	private static Predicate isLoan = new Predicate() {
		@Override
		public boolean matches(Exchange exchange) {
			return getApplication(exchange).getType().equals(LoanAccount.TYPE);
		}
	};
	
	// Determines whether a loan is approved
	//
	private static Predicate isLoanApproved = new Predicate() {
		@Override
		public boolean matches(Exchange exchange) {
			return getResponse(exchange).isApproved();
		}
	};
	
	// Randomly approve a loan.  Still better than most lending standards...
	//
	private static Processor reviewApplication = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {
			boolean approved = RandomUtils.nextBoolean(random);
			setResponse(exchange, approved);
		}
	};

	// Accept application
	//
	private static Processor acceptLoan = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {
			setResponse(exchange, true);
		}
	};

	public static RouteBuilder routingRules() {
        return new RouteBuilder() {
            public void configure() {
            	
            	// When there is a loan application, route the review queue; otherwise,
            	// automatically approve
            	//
                from(ACCOUNT_APPLICATION).
                	choice().
                		when(isLoan).
                			to(REVIEW_LOAN).
                		otherwise().
                			process(acceptLoan).
                			to(ACCOUNT_APPROVED);
                
            	// It takes 5 seconds to review... very slow - bankers' hours...
            	//
                from(REVIEW_LOAN).
                	delay(5 * 1000).
                	process(reviewApplication).
                	choice().
                    	when(isLoanApproved).
                        	to(ACCOUNT_APPROVED).
                        otherwise().
                        	to(ACCOUNT_REJECTED);
           }
        };
	}

	private static void setResponse(Exchange exchange, boolean approved) {
		AccountApplicationEvent application = getApplication(exchange);
		AccountApplicationResponseEvent response = 
				new AccountApplicationResponseEvent(
					application.getAccountId(),
					application.getCustomerId(),
					approved,
					application.getType(),
					(approved) 
						? "You're approved cause we like you" 
						: "Sorry, we can't approve the loan - you are a deadbeat"	
				);
		exchange.getOut().setBody(response);
	}
	
    private static AccountApplicationEvent getApplication(Exchange exchange) {
    	return getInMessage(exchange, AccountApplicationEvent.class);
    }
    
    private static AccountApplicationResponseEvent getResponse(Exchange exchange) {
    	return getInMessage(exchange, AccountApplicationResponseEvent.class);
    }
    
	private static <T> T getInMessage(Exchange exchange, Class<T> returnType) {
    	return exchange.getIn().getBody(returnType);
    }
}
