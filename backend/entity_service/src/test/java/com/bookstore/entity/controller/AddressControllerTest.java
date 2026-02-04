package com.bookstore.entity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.bookstore.entity.service.AddressService;
import com.example.common.database.MyDataSource;
import com.example.common.repository.UserRepository;
import com.example.common.security.CustomJwtDecoder;
import com.example.common.security.JwtUtil;
import com.example.database.generated.tables.pojos.UserAddress;
import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.integration.spring.SpringLiquibase;

@WebMvcTest(
    controllers = AddressController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {MyDataSource.class})
)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
class AddressControllerTest {

    private static final Integer USER_ID = 1;
    private static final Integer ADDRESS_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    private DSLContext dsl;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private SpringLiquibase liquibase;

    @Test
    void testGetAddresses_ReturnsAddresses() throws Exception {
        // Arrange
        UserAddress address1 = new UserAddress(1, USER_ID, "123 Main St", "City1", "State1", "12345", "USA", true, LocalDateTime.now());
        UserAddress address2 = new UserAddress(2, USER_ID, "456 Oak Ave", "City2", "State2", "67890", "USA", false, LocalDateTime.now());
        List<UserAddress> addresses = List.of(address1, address2);

        when(addressService.getAddressesByUserId(USER_ID)).thenReturn(addresses);

        // Act & Assert
        mockMvc.perform(get("/address")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].streetAddress").value("123 Main St"))
                .andExpect(jsonPath("$[1].streetAddress").value("456 Oak Ave"));

        verify(addressService, times(1)).getAddressesByUserId(USER_ID);
    }

    @Test
    void testGetAddresses_ReturnsEmptyList() throws Exception {
        when(addressService.getAddressesByUserId(USER_ID)).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/address")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testGetDefaultAddress_ReturnsDefault() throws Exception {
        UserAddress defaultAddress = new UserAddress(1, USER_ID, "123 Default St", "City", "State", "12345", "USA", true, LocalDateTime.now());
        when(addressService.getDefaultAddress(USER_ID)).thenReturn(defaultAddress);

        // Act & Assert
        mockMvc.perform(get("/address/default")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streetAddress").value("123 Default St"))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void testGetDefaultAddress_NotFound() throws Exception {
        when(addressService.getDefaultAddress(USER_ID)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/address/default")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID))))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateAddress_Success() throws Exception {
        String requestBody = """
                {
                    "streetAddress": "123 New St",
                    "city": "New City",
                    "state": "NC",
                    "postalCode": "12345",
                    "country": "USA",
                    "isDefault": false
                }
                """;

        UserAddress createdAddress = new UserAddress(99, USER_ID, "123 New St", "New City", "NC", "12345", "USA", false, null);
        when(addressService.createAddress(eq(USER_ID), any(UserAddress.class))).thenReturn(createdAddress);

        // Act & Assert
        mockMvc.perform(post("/address")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.streetAddress").value("123 New St"))
                .andExpect(jsonPath("$.city").value("New City"));

        verify(addressService, times(1)).createAddress(eq(USER_ID), any(UserAddress.class));
    }

    @Test
    void testUpdateAddress_Success() throws Exception {
        String requestBody = """
                {
                    "streetAddress": "456 Updated St",
                    "city": "Updated City",
                    "state": "UC",
                    "postalCode": "54321",
                    "country": "USA",
                    "isDefault": true
                }
                """;

        doNothing().when(addressService).updateAddress(eq(USER_ID), eq(ADDRESS_ID), any(UserAddress.class));

        // Act & Assert
        mockMvc.perform(put("/address/" + ADDRESS_ID)
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        verify(addressService, times(1)).updateAddress(eq(USER_ID), eq(ADDRESS_ID), any(UserAddress.class));
    }

    @Test
    void testUpdateAddress_NotFound() throws Exception {
        String requestBody = """
                {
                    "streetAddress": "456 Updated St",
                    "city": "Updated City",
                    "state": "UC",
                    "postalCode": "54321",
                    "country": "USA"
                }
                """;

        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(addressService).updateAddress(eq(USER_ID), eq(999), any(UserAddress.class));

        // Act & Assert
        mockMvc.perform(put("/address/999")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteAddress_Success() throws Exception {
        doNothing().when(addressService).deleteAddress(USER_ID, ADDRESS_ID);

        // Act & Assert
        mockMvc.perform(delete("/address/" + ADDRESS_ID)
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(addressService, times(1)).deleteAddress(USER_ID, ADDRESS_ID);
    }

    @Test
    void testDeleteAddress_NotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(addressService).deleteAddress(USER_ID, 999);

        // Act & Assert
        mockMvc.perform(delete("/address/999")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSetDefaultAddress_Success() throws Exception {
        doNothing().when(addressService).setDefaultAddress(USER_ID, ADDRESS_ID);

        // Act & Assert
        mockMvc.perform(patch("/address/" + ADDRESS_ID + "/set-default")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isOk());

        verify(addressService, times(1)).setDefaultAddress(USER_ID, ADDRESS_ID);
    }

    @Test
    void testSetDefaultAddress_NotFound() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(addressService).setDefaultAddress(USER_ID, 999);

        // Act & Assert
        mockMvc.perform(patch("/address/999/set-default")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateAddress_WithDefaultNull() throws Exception {
        String requestBody = """
                {
                    "streetAddress": "123 New St",
                    "city": "New City",
                    "state": "NC",
                    "postalCode": "12345",
                    "country": "USA"
                }
                """;

        UserAddress createdAddress = new UserAddress(99, USER_ID, "123 New St", "New City", "NC", "12345", "USA", false, null);
        when(addressService.createAddress(eq(USER_ID), any(UserAddress.class))).thenReturn(createdAddress);

        // Act & Assert
        mockMvc.perform(post("/address")
                .with(jwt().jwt(jwt -> jwt.claim("userId", USER_ID)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99));

        verify(addressService, times(1)).createAddress(eq(USER_ID), any(UserAddress.class));
    }
}
