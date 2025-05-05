# Overview
Undockerized Spring application which connects to dockerized MySQL instance.

# Inital setup
1. The DB needs to run for liquibase to generate the schema, use the docker compose command.
2. Then jooq codegen needs to run to generate the models, use the liquibase update command

# Liquibase setup - copied from JOOQ with Spring
To generate DB with liquibase: 
```mvn liquibase:dropAll liquibase:update``` 
Run app:
```mvn liquibase:update && mvn spring-boot:run``` This will call the jooq generate command first, and them call the liquibase changelog update - which is weird. Also I needed to add in, create if not exists, into the changelog.sql file otherwise it errored saying the tables already existed. I have incorporated a liquibase:update before the spring-boot:run so code is executed in the correct order.

Liquibase setup
I added the liquibase-core dependency, and also the maven plugin. Then I added the changelog file, which will replace the currently used schema.sql which I load in via docker-compose. Setup relevant properties in pom and application.properties.

Notes:
JOOQ and liquibase both connect with the root user
Liquibase should obviously run before JOOQ codegen feature to to create the required tables.

To blat everything and create schema??:
```
mvn liquibase:dropAll && mvn liquibase:update && mvn spring-boot:run
mvn liquibase:dropAll
mvn liquibase:update
mvn spring-boot:run
```

# Database setup
Run the database with: 
```docker-compose -f docker-compose-jooq.yml up --build ```

# Todo
- Data gets inserted into the DB twice when building and then first running..
- Add in JsonView, to strip out the IDs before sending back to the client
- Without dropAll i.e just using spring-boot:run it complains the tables already exist. How to not wipe the DB every time? Look into the spring-boot plugin or the lifecycle the liquibase plugin is attached to.
- add tests to current functionality
    - add custom models, so you can use build methods and custom jackson etc
    - seperate between the two in the service layer
    - within, the controller tests, check for the Model/Domain class vs the generated Pojo
- transactions?
- swagger API with example models
- build out the schema
- users
- orders
- full payment service

# Resources
- Liquibase SQL format: https://docs.liquibase.com/concepts/changelogs/sql-format.html
- Books: https://www.oclc.org/en/worldcat/library100/top500.html





The commit that deleted everything: https://github.com/SamuelSnowball/JavaPlayground/commit/2e69a7edb3f35f5a9be4dd63aff07fbaa7c587c5#diff-1c35cb41a4a35c95bbb9c9c93710cf31b2c31e2a4012fc15583bb94138e2c51a

Notes
- I've managed to delete my schema file / init directory, if I rebuild DB it won't work
