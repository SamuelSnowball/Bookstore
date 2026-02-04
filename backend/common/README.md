# Common Library

This is a shared library module containing common Java classes used across multiple microservices in the Bookstore application.

Liquibase will run automatically when the any application using the libary starts via the SpringLiquibase bean configured in common/MyDataSource.java.

## Contents

This module includes:

### Database Configuration
- **MyDataSource** - Configures HikariCP connection pool, jOOQ DSL context, Liquibase, and transaction management
- **ExceptionTranslator** - Translates jOOQ exceptions to Spring data access exceptions
- **Database Migrations** - Contains all Liquibase changelogs and SQL scripts in `src/main/resources/db/`

### Security Components
- **JwtUtil** - JWT token generation and validation utilities
- **SecurityConfig** - Spring Security configuration with CORS and JWT support
- **CustomJwtDecoder** - Custom JWT decoder bridging jjwt and Spring Security OAuth2

### Exception Handling
- **GlobalExceptionHandler** - Global exception handler for consistent error responses

## Usage

### 1. Install the common library

From the `common` directory, run:
```bash
mvn clean install
```

This will install the library to your local Maven repository.

### 2. Add as a dependency

In your microservice's `pom.xml`, add:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 3. Import configurations

In your Spring Boot application, use `@ComponentScan` to include the common package:

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.yourservice", "com.example.common"})
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

Or selectively import specific configurations:

```java
@SpringBootApplication
@Import({MyDataSource.class, SecurityConfig.class, GlobalExceptionHandler.class})
public class YourServiceApplication {
    // ...
}
```

## Configuration Properties

Ensure your microservice's `application.properties` includes:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mydatabase?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=mysql

# Security (if needed, the secret key is currently hardcoded in JwtUtil)
```

## Notes

- The security configuration in `SecurityConfig` may need to be customized per microservice based on specific endpoint access rules
- Consider externalizing the JWT secret key to environment variables in production
- Each microservice can override or extend these configurations as needed
