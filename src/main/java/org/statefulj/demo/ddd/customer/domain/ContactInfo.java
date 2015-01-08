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
package org.statefulj.demo.ddd.customer.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ContactInfo {
	
	private Name name;
	
	@Column(unique=true)
	private Email email;
	
	public ContactInfo() {}
	
	public ContactInfo(Name name, Email email) {
		this.name = name;
		this.email = email;
	}

	public Name getName() {
		return name;
	}

	public Email getEmail() {
		return email;
	}

	public ContactInfo update(String firstName, String lastName) {
		Name name = this.name.update(firstName, lastName);
		return new ContactInfo(name, this.email);
	}
}
