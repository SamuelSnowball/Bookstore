package com.bookstore.entity.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.common.repository.BaseIntegrationTest;
import com.example.database.generated.tables.pojos.UserAddress;

// Inherits @Transactional from BaseIntegrationTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyDataSource.class, AddressRepository.class})
class AddressRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void testGetAddressesByUserId() {
        // Act
        List<UserAddress> addresses = addressRepository.getAddressesByUserId(1);

        // Assert
        assertNotNull(addresses);
        assertTrue(addresses.size() >= 0);
    }

    @Test
    void testGetAddressById() {
        // First create an address
        UserAddress newAddress = new UserAddress(
            null, 1, "123 Test St", "Test City", "TS", "12345", "USA", false, null
        );
        addressRepository.createAddress(newAddress);
        
        // Get all addresses for user 1
        List<UserAddress> addresses = addressRepository.getAddressesByUserId(1);
        assertFalse(addresses.isEmpty());
        
        // Get the first address by ID
        UserAddress address = addressRepository.getAddressById(addresses.get(0).getId());
        
        // Assert
        assertNotNull(address);
        assertEquals(1, address.getUserId());
    }

    @Test
    void testCreateAddress() {
        // Arrange
        UserAddress newAddress = new UserAddress(
            null, 1, "456 New St", "New City", "NC", "54321", "USA", false, null
        );

        // Act
        Integer createdId = addressRepository.createAddress(newAddress);

        // Assert - verify it was created and returned ID
        assertNotNull(createdId);
        UserAddress created = addressRepository.getAddressById(createdId);
        assertNotNull(created);
        assertEquals("456 New St", created.getStreetAddress());
        assertEquals("New City", created.getCity());
    }

    @Test
    void testUpdateAddress() {
        // First create an address
        UserAddress newAddress = new UserAddress(
            null, 1, "789 Old St", "Old City", "OC", "11111", "USA", false, null
        );
        addressRepository.createAddress(newAddress);
        
        // Get the created address
        List<UserAddress> addresses = addressRepository.getAddressesByUserId(1);
        UserAddress created = addresses.stream()
            .filter(a -> "789 Old St".equals(a.getStreetAddress()))
            .findFirst()
            .orElseThrow();
        
        // Update the address
        UserAddress updated = new UserAddress(
            created.getId(), 1, "789 New St", "New City", "NC", "22222", "USA", false, created.getCreatedAt()
        );
        addressRepository.updateAddress(updated);
        
        // Verify update
        UserAddress result = addressRepository.getAddressById(created.getId());
        assertEquals("789 New St", result.getStreetAddress());
        assertEquals("New City", result.getCity());
    }

    @Test
    void testDeleteAddress() {
        // First create an address
        UserAddress newAddress = new UserAddress(
            null, 1, "999 Delete St", "Delete City", "DC", "99999", "USA", false, null
        );
        addressRepository.createAddress(newAddress);
        
        // Get the created address
        List<UserAddress> addresses = addressRepository.getAddressesByUserId(1);
        UserAddress created = addresses.stream()
            .filter(a -> "999 Delete St".equals(a.getStreetAddress()))
            .findFirst()
            .orElseThrow();
        
        // Delete it
        addressRepository.deleteAddress(created.getId());
        
        // Verify deletion
        UserAddress result = addressRepository.getAddressById(created.getId());
        assertNull(result);
    }

    @Test
    void testSetDefaultAddress() {
        // Create two addresses
        UserAddress addr1 = new UserAddress(
            null, 2, "111 First St", "City", "ST", "11111", "USA", true, null
        );
        UserAddress addr2 = new UserAddress(
            null, 2, "222 Second St", "City", "ST", "22222", "USA", false, null
        );
        
        addressRepository.createAddress(addr1);
        addressRepository.createAddress(addr2);
        
        // Get the created addresses
        List<UserAddress> addresses = addressRepository.getAddressesByUserId(2);
        UserAddress first = addresses.stream()
            .filter(a -> "111 First St".equals(a.getStreetAddress()))
            .findFirst()
            .orElseThrow();
        UserAddress second = addresses.stream()
            .filter(a -> "222 Second St".equals(a.getStreetAddress()))
            .findFirst()
            .orElseThrow();
        
        // Set second as default
        addressRepository.setDefaultAddress(2, second.getId());
        
        // Verify
        UserAddress defaultAddr = addressRepository.getDefaultAddress(2);
        assertNotNull(defaultAddr);
        assertEquals("222 Second St", defaultAddr.getStreetAddress());
        assertTrue(defaultAddr.getIsDefault());
    }

    @Test
    void testGetDefaultAddress() {
        // Create address with default flag
        UserAddress newAddress = new UserAddress(
            null, 3, "333 Default St", "City", "ST", "33333", "USA", true, null
        );
        addressRepository.createAddress(newAddress);
        
        // Get default address
        UserAddress result = addressRepository.getDefaultAddress(3);
        
        // Assert
        assertNotNull(result);
        assertEquals("333 Default St", result.getStreetAddress());
        assertTrue(result.getIsDefault());
    }
}
