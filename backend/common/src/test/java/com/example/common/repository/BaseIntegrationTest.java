package com.example.common.repository;

import static com.example.database.generated.Tables.USER;

import java.sql.Connection;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

/*
This is used by the repository layer tests, as well as the full integration tests in the integration test module.

The Testcontainers MySQL container needs to start before Spring tries to create the DataSource bean.

Since the container is manually started in the static block, there are no 
@SpringBootTest, @Testcontainers, or @Container  annotations on this class or the subclasses, as otherwise
these annotations would conflict with the manual lifecycle management.

The tests are connecting to a Testcontainers MySQL Docker container, not your production database.

BaseTest defines:
@DynamicPropertySource in BaseTest overrides the @Value properties:

When tests run:
Testcontainers spins up a temporary MySQL 8.0 Docker container
Assigns it a random port (e.g., 49153)
The tests connect to this isolated container
After tests finish, the container is destroyed
*/
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // So that each test class shares the same instance, avoiding the
                                                // liquibase changelog from being run multiple times
public abstract class BaseIntegrationTest {

    private static boolean usersInitialized = false;

    protected static MySQLContainer<?> mysql;

    /*
     * Initialize and start container in static block to ensure it starts before
     * Spring context
     * Now the container will start in the static initialization block, which
     * executes before Spring tries to load the application context. This ensures
     * the container is running and has a mapped port before @DynamicPropertySource
     * tries to access getJdbcUrl().
     */
    static {
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("mydatabase")
                .withUsername("test")
                .withPassword("test")
                .withCommand("--default-authentication-plugin=mysql_native_password");
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog.xml");
    }

    @BeforeEach
    void setUp() {
        // Insert test users once using JOOQ with direct connection
        if (!usersInitialized) {
            try (Connection conn = mysql.createConnection("")) {
                DSLContext dsl = DSL.using(conn, SQLDialect.MYSQL);

                // UserId 1 is the demo user inserted by liquibase

                dsl.insertInto(USER)
                        .set(USER.ID, 2)
                        .set(USER.USERNAME, "testuser2")
                        .execute();

                dsl.insertInto(USER)
                        .set(USER.ID, 3)
                        .set(USER.USERNAME, "testuser3")
                        .execute();

                usersInitialized = true;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize test users", e);
            }
        }
    }
}
