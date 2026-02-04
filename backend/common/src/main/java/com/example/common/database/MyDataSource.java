package com.example.common.database;

import javax.sql.DataSource;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import liquibase.integration.spring.SpringLiquibase;

/*
mysqlDataSource (raw pool)
    ↓ wrapped by
transactionAwareDataSource (transaction-aware)
    ↓ used by
connectionProvider (JOOQ)
    ↓ used by
dsl() (your JOOQ queries)
*/
@Configuration
@EnableTransactionManagement(proxyTargetClass = true) // proxyTargetClass=true needed for tests to avoid JDK proxy type issues
public class MyDataSource {

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:root}")
    private String username;

    @Value("${spring.datasource.password:mysql}")
    private String password;

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() {
        return new TransactionAwareDataSourceProxy(mysqlDataSource());
    }

    // DataSourceTransactionManager - Binds a JDBC Connection from the specified
    // DataSource to the current thread, potentially allowing for one thread-bound
    // Connection per DataSource.
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(mysqlDataSource()); // transactionManager() uses raw mysqlDataSource() → manages transactions
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider() {
        return new DataSourceConnectionProvider(transactionAwareDataSource());
    }

    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }

    @Bean
    public DefaultDSLContext dsl() {
        return new DefaultDSLContext(configuration());
    }

    @Bean
    public DefaultConfiguration configuration() {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider());
        SQLDialect dialect = SQLDialect.MYSQL;
        jooqConfiguration.set(dialect);
        return jooqConfiguration;
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog.xml");
        return liquibase;
    }

    @Bean
    @Primary
    public DataSource mysqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DatabaseDriver.MYSQL.getDriverClassName());
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(1);
        config.setReadOnly(false);
        return new HikariDataSource(config);
    }

}
