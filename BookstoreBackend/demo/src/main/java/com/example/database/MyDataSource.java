package com.example.database;
import javax.sql.DataSource;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


// Also contains JOOQConfiguration
@Configuration
@EnableTransactionManagement // Also used here as class used within tests
public class MyDataSource {
    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() {
        return new TransactionAwareDataSourceProxy(mysqlDataSource());
    }
    // DataSourceTransactionManager - Binds a JDBC Connection from the specified DataSource to the current thread, potentially allowing for one thread-bound Connection per DataSource.
    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(mysqlDataSource()); // surely this should use the 
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
        //jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        SQLDialect dialect = SQLDialect.MYSQL;
        jooqConfiguration.set(dialect);
        return jooqConfiguration;
    }

    @Bean
    @LiquibaseDataSource
    public DataSource mysqlDataSource() {
        /*
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC");
        dataSourceBuilder.username("root");
        dataSourceBuilder.password("mysql");
        return dataSourceBuilder.build();
        */
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DatabaseDriver.MYSQL.getDriverClassName());
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("mysql");
        config.setMaximumPoolSize(1);
        //config.setAutoCommit(false);
        return new HikariDataSource(config);
    }
}