package com.example;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
/*
 * Nearly a full server test as by using mockMvc we lose some functionality?
 * Things like setting headers perhaps? Lookup restTemplate vs mockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class NearlyFullServerTest {
    
    @Autowired
	private MockMvc mockMvc;
	@Test
	void greetingShouldReturnDefaultMessage() throws Exception {
		this.mockMvc.perform(get("/helloworld")).andExpect(content().string(containsString("Hello world!")));
	}
}