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
package org.statefulj.demo.ddd.config;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.statefulj.demo.ddd.shared.domain.tx.ObservableTransactionManager;
import org.statefulj.demo.ddd.shared.domain.tx.TransactionObserver;
import org.statefulj.demo.ddd.shared.events.impl.DeferredEventProducer;

import static org.statefulj.demo.ddd.account.domain.Account.*;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	basePackages={
		"org.statefulj.demo.ddd", 
		"org.statefulj.webapp.repo"
	}
)
public class DBConfig {
	
	@Bean
	public TransactionObserver accountApplicationEventProducer() {
		return new DeferredEventProducer(ACCOUNT_APPLICATION);
	}
	
	@Bean
	public TransactionObserver accountApprovedEventProducer() {
		return new DeferredEventProducer(ACCOUNT_APPLICATION_APPROVED);
	}
	
	@Bean
	public TransactionObserver accountRejectedEventProducer() {
		return new DeferredEventProducer(ACCOUNT_APPLICATION_REJECTED);
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(Boolean.TRUE);
		vendorAdapter.setShowSql(Boolean.FALSE);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setDataSource(dataSource());
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setPackagesToScan("org.statefulj.demo.ddd", "org.statefulj.webapp.model");
		return factory;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(
			LocalContainerEntityManagerFactoryBean entityManagerFactory, 
			List<TransactionObserver> observers) {
		EntityManagerFactory factory = entityManagerFactory.getObject();
		return new ObservableTransactionManager(factory, observers);
	}
	
	@Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .build();
    }
}