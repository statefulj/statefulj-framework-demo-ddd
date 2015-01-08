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
package org.statefulj.demo.ddd.account.domain;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.model.StatefulFSM;

import static org.statefulj.demo.ddd.account.domain.Account.NON_EXISTENT;
import static org.statefulj.demo.ddd.account.domain.CheckingAccount.*;

@Entity
@Scope("prototype")
@StatefulController(
	value=BEAN_ID,
	clazz=CheckingAccount.class,
	startState=NON_EXISTENT
)
public class CheckingAccount extends Account {
	
	public static final String BEAN_ID = "checkingAccount";

	public final static String TYPE = "Checking";
	
	@FSM(BEAN_ID)
	@Transient
	private StatefulFSM<Account> fsm;
	
	@Override
	@Transient
	public String getType() {
		return TYPE;
	}

	@Override
	@Transient
	protected StatefulFSM<Account> getFSM() {
		return this.fsm;
	}
}
