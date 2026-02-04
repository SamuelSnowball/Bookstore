# Entity Service

The Entity Service is a Spring Boot microservice responsible for managing core entities in the Bookstore application:
- Books
- Authors
- User Addresses

## Features

- RESTful API for Book, Author, and Address management
- JWT-based authentication
- JOOQ for type-safe database queries
- Liquibase database migrations
- Swagger/OpenAPI documentation
- Health checks via Spring Actuator
- Docker support

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.2**
- **JOOQ 3.19.14** - Type-safe SQL
- **Liquibase 4.31.0** - Database migrations
- **MySQL 8.0** - Database
- **JWT** - Authentication
- **Springdoc OpenAPI** - API documentation

## Running Locally

### Prerequisites
- Java 21
- Maven 3.9+
- MySQL 8.0 running on localhost:3306
- Database: `bookstore`

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The service will start on port **9001**.

### Access Swagger UI
http://localhost:9001/swagger-ui.html

### Health Check
http://localhost:9001/actuator/health

## API Endpoints

### Books
- `GET /book` - Get all books
- `GET /book/{id}` - Get book by ID
- `POST /book` - Create a new book
- `PUT /book/{id}` - Update a book
- `DELETE /book/{id}` - Delete a book
- `GET /book/search?title={title}` - Search books by title

### Authors
- `GET /author` - Get all authors
- `GET /author/{id}` - Get author by ID
- `POST /author` - Create a new author
- `PUT /author/{id}` - Update an author
- `DELETE /author/{id}` - Delete an author
- `GET /author/search?lastName={lastName}` - Search authors by last name

### Addresses
- `GET /address` - Get all addresses
- `GET /address/{id}` - Get address by ID
- `GET /address/user/{userId}` - Get all addresses for a user
- `POST /address` - Create a new address
- `PUT /address/{id}` - Update an address
- `DELETE /address/{id}` - Delete an address

## Docker

### Build Image
```bash
docker build -t entity-service:1.0.0 .
```

### Run Container
```bash
docker run -d \
  -p 9001:9001 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/bookstore \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  --name entity-service \
  entity-service:1.0.0
```

## Kubernetes Deployment

See the Helm chart in `../helm/entity-service/` for Kubernetes deployment configuration.

## Configuration

Key configuration properties in `application.properties`:

```properties
server.port=9001
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore
jwt.secret=YourSecretKeyHere
cors.allowed-origins=http://localhost:5173
```

## Security

All endpoints require JWT authentication via `Authorization: Bearer <token>` header. The JWT must be obtained from the authentication service (or monolith during migration).

## Database

This service shares the MySQL database with other services. It uses these tables:
- `book`
- `author`
- `book_author` (join table)
- `user_address`
- `users` (for authentication only)

## Development

### JOOQ Code Generation

JOOQ classes are currently manually created. For automatic generation, configure the JOOQ Maven plugin and run:
```bash
mvn generate-sources
```

### Running Tests

```bash
mvn test
```

Tests use Testcontainers for MySQL.
