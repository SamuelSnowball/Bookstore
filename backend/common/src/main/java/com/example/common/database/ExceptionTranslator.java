package com.example.common.database;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.SQLDialect;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/*
Exception Translation: jOOQ can be configured to translate its specific DataAccessException into Spring's data access exception hierarchy, providing a consistent error-handling mechanism across the entire application.
*/
@Component
@Slf4j
public class ExceptionTranslator implements ExecuteListener {

    @Override
    public void exception(ExecuteContext context) {

        log.info("ExceptionTranslator running...");

        SQLDialect dialect = context.configuration().dialect();
        SQLExceptionTranslator translator = new SQLErrorCodeSQLExceptionTranslator(dialect.name());
        context.exception(translator
                .translate("Access database using Jooq", context.sql(), context.sqlException()));
    }
}
