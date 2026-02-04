package com.example.common.repository;

import static com.example.database.generated.Tables.USER;

import org.jooq.DSLContext;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Repository;

import com.example.database.generated.tables.daos.UserDao;
import com.example.database.generated.tables.pojos.User;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class UserRepository extends UserDao {

    private final DSLContext create;

    public UserRepository(DSLContext dslContext, DefaultConfiguration configuration) {
        super(configuration);
        this.create = dslContext;
    }

    public User findByUsername(String username) {
        return create.selectFrom(USER)
                .where(USER.USERNAME.eq(username))
                .fetchOneInto(User.class);
    }
}
