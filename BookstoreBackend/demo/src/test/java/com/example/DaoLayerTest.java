package com.example;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import com.example.database.MyDataSource;
import com.example.repository.AuthorRepository;
/*
In the spring example they duplicate all the beans for setting up the datasource and proxy from 
the main code, why can't I component scan my datasource file from here so no need to copy paste?
Try it..
Ok I guess of the properties will need to be different, connection string for example,
can I override a bean definition? If the database beans took values from config I could
pass my test a different config file.
Need to setup a H2 DB here and my datasource to connect to it
I was trying to see what beans got loaded into the applicationContext to see if I set it up correctly,
when using @SpringBootTest
it's searching up and finding my SpringBootApplication annotation and is loading in everything
I might be able to @DataJpaTest or @JdbcTest with @ContextConfiguration and pass in the classes I need.
I wanted to use my datasource beans from my main application in my test application, as the jooq-spring example just duplicated all the beans into the test class. 
I added @SpringBootTest however this searched upwards and included all beans from my main spring boot application which I didn’t want. 
I then tried @RunWith however this is a JUnit4 annotation which I’m not using as I am on JUnit5/Juptier. And in JUnit5 it recommeds to simply use @SpringBootTest. 
I then tried a combination of @SpringBootTest and @ContextConfiguration and this seemed to solve the issue.
However it's still loading values from my application.properties file..
Don't think youre supposed to use both SpringBootTest and ContextConfiguration annotations
I wanted to not duplicate (some) beans into my test class, and instead load only the beans I wanted to test, from the main code. 
However it seems I got all or nothing, when using SpringBootTest it included all beans, and when using ContextConfiguration with my specified classes only, 
I couldn't find a way to get at the ApplicationContext to check what beans I had avaliable. 
Even trying to use an autowired repository object isn't working with ContextConfiguration.. got it working in the end, with ExtendWith
Need to fix Transactional in my main application - I guess doing the testTransaction test has narrowed down the issue :)
Then wondering, how to actually test that transactions are being rolled back? as that only happens after the test?
maybe in the afterEach.
So this config is working:
    @SpringBootTest
    @ExtendWith(SpringExtension.class)
    @PropertySource({"classpath:test_db.properties"})
    If data does get persisted to the DB it will need to be reset with: docker compose -f docker-compose-jooq.yml up --build
This config is also working:
    @ContextConfiguration(classes = {MyDataSource.class, AuthorRepository.class})
    @PropertySource({"classpath:test_db.properties"})
    @ExtendWith(SpringExtension.class)
    @EnableTransactionManagement(proxyTargetClass = true)
    When I just had this @EnableTransactionManagement, it gave me an error:
    Unsatisfied dependency expressed through field 'repository': Bean named 'authorRepository' is expected to be of type 'com.example.demo.repository.AuthorRepository' but was actually of type 'jdk.proxy2.$Proxy32'
    And this link: https://stackoverflow.com/questions/52603759/beannotofrequiredtypeexception-bean-named-x-is-expected-to-be-of-type-x-but-was/52615936
    Said to change the annotation to: @EnableTransactionManagement(proxyTargetClass = true)
    Which is now working.
    */
//@SpringBootTest
@ContextConfiguration(classes = {MyDataSource.class, AuthorRepository.class})
@PropertySource({"classpath:test_db.properties"})
@ExtendWith(SpringExtension.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class DaoLayerTest {
    @Autowired
    private AuthorRepository repository;
    @Test
    @Transactional
    public void insertFirstNameTest(){
        // Ensure clean DB state
        assertEquals(0, repository.getFirstNames().size());
        
        // Insert
        repository.insert("Sam", null);
        // Expect
        assertEquals(1, repository.getFirstNames().size());
    }
}