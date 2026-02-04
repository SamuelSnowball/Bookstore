package com.bookstore.entity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.bookstore.entity.repository.AddressRepository;
import com.example.database.generated.tables.pojos.UserAddress;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void testGetAddressesByUserId() {
        // Arrange
        int userId = 1;
        UserAddress address1 = new UserAddress(1, userId, "123 Main St", "City1", "State1", "12345", "USA", true, LocalDateTime.now());
        UserAddress address2 = new UserAddress(2, userId, "456 Oak Ave", "City2", "State2", "67890", "USA", false, LocalDateTime.now());
        
        List<UserAddress> mockAddresses = Arrays.asList(address1, address2);
        when(addressRepository.getAddressesByUserId(userId)).thenReturn(mockAddresses);

        // Act
        List<UserAddress> result = addressService.getAddressesByUserId(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("123 Main St", result.get(0).getStreetAddress());
        verify(addressRepository, times(1)).getAddressesByUserId(userId);
    }

    @Test
    void testGetAddressById_Found() {
        // Arrange
        int addressId = 1;
        UserAddress address = new UserAddress(addressId, 1, "123 Main St", "City", "State", "12345", "USA", false, LocalDateTime.now());
        when(addressRepository.getAddressById(addressId)).thenReturn(address);

        // Act
        UserAddress result = addressService.getAddressById(addressId);

        // Assert
        assertNotNull(result);
        assertEquals("123 Main St", result.getStreetAddress());
        verify(addressRepository, times(1)).getAddressById(addressId);
    }

    @Test
    void testGetAddressById_NotFound() {
        // Arrange
        when(addressRepository.getAddressById(999)).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> addressService.getAddressById(999));
        verify(addressRepository, times(1)).getAddressById(999);
    }

    @Test
    void testCreateAddress_FirstAddress() {
        // Arrange
        int userId = 1;
        UserAddress address = new UserAddress(null, null, "123 Main St", "City", "State", "12345", "USA", false, null);
        UserAddress createdAddress = new UserAddress(1, userId, "123 Main St", "City", "State", "12345", "USA", true, LocalDateTime.now());
        when(addressRepository.getAddressesByUserId(userId)).thenReturn(List.of());
        when(addressRepository.createAddress(any(UserAddress.class))).thenReturn(1);
        when(addressRepository.getAddressById(1)).thenReturn(createdAddress);

        // Act
        UserAddress result = addressService.createAddress(userId, address);

        // Assert
        verify(addressRepository, times(1)).createAddress(any(UserAddress.class));
        verify(addressRepository, times(1)).getAddressById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void testCreateAddress_WithDefault() {
        // Arrange
        int userId = 1;
        UserAddress existingAddress = new UserAddress(1, userId, "Existing St", "City", "State", "11111", "USA", true, LocalDateTime.now());
        UserAddress newAddress = new UserAddress(null, null, "123 Main St", "City", "State", "12345", "USA", true, null);
        UserAddress createdAddress = new UserAddress(2, userId, "123 Main St", "City", "State", "12345", "USA", true, LocalDateTime.now());
        
        when(addressRepository.getAddressesByUserId(userId)).thenReturn(List.of(existingAddress));
        when(addressRepository.createAddress(any(UserAddress.class))).thenReturn(2);
        when(addressRepository.getAddressById(2)).thenReturn(createdAddress);

        // Act
        UserAddress result = addressService.createAddress(userId, newAddress);

        // Assert
        verify(addressRepository, times(1)).createAddress(any(UserAddress.class));
        verify(addressRepository, times(1)).getAddressById(2);
        assertNotNull(result);
        assertEquals(2, result.getId());
    }

    @Test
    void testUpdateAddress_Success() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        LocalDateTime createdAt = LocalDateTime.now();
        UserAddress existing = new UserAddress(addressId, userId, "Old St", "Old City", "OS", "11111", "USA", false, createdAt);
        UserAddress updates = new UserAddress(null, null, "New St", "New City", "NS", "22222", "USA", true, null);
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);
        doNothing().when(addressRepository).updateAddress(any(UserAddress.class));

        // Act
        addressService.updateAddress(userId, addressId, updates);

        // Assert
        verify(addressRepository, times(1)).getAddressById(addressId);
        verify(addressRepository, times(1)).updateAddress(any(UserAddress.class));
    }

    @Test
    void testUpdateAddress_WrongUser() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        UserAddress existing = new UserAddress(addressId, 2, "St", "City", "ST", "11111", "USA", false, LocalDateTime.now());
        UserAddress updates = new UserAddress(null, null, "New St", "City", "ST", "22222", "USA", false, null);
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> addressService.updateAddress(userId, addressId, updates));
        verify(addressRepository, never()).updateAddress(any());
    }

    @Test
    void testDeleteAddress_Success() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        UserAddress existing = new UserAddress(addressId, userId, "St", "City", "ST", "11111", "USA", false, LocalDateTime.now());
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);
        doNothing().when(addressRepository).deleteAddress(addressId);

        // Act
        addressService.deleteAddress(userId, addressId);

        // Assert
        verify(addressRepository, times(1)).deleteAddress(addressId);
    }

    @Test
    void testDeleteAddress_WrongUser() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        UserAddress existing = new UserAddress(addressId, 2, "St", "City", "ST", "11111", "USA", false, LocalDateTime.now());
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);

        // Act & Assert
        assertThrows(ResponseStatusException.class,
            () -> addressService.deleteAddress(userId, addressId));
        verify(addressRepository, never()).deleteAddress(anyInt());
    }

    @Test
    void testSetDefaultAddress_Success() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        UserAddress existing = new UserAddress(addressId, userId, "St", "City", "ST", "11111", "USA", false, LocalDateTime.now());
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);
        doNothing().when(addressRepository).setDefaultAddress(userId, addressId);

        // Act
        addressService.setDefaultAddress(userId, addressId);

        // Assert
        verify(addressRepository, times(1)).setDefaultAddress(userId, addressId);
    }

    @Test
    void testSetDefaultAddress_WrongUser() {
        // Arrange
        int userId = 1;
        int addressId = 1;
        UserAddress existing = new UserAddress(addressId, 2, "St", "City", "ST", "11111", "USA", false, LocalDateTime.now());
        
        when(addressRepository.getAddressById(addressId)).thenReturn(existing);

        // Act & Assert
        assertThrows(ResponseStatusException.class,
            () -> addressService.setDefaultAddress(userId, addressId));
        verify(addressRepository, never()).setDefaultAddress(anyInt(), anyInt());
    }

    @Test
    void testGetDefaultAddress() {
        // Arrange
        int userId = 1;
        UserAddress defaultAddress = new UserAddress(1, userId, "Default St", "City", "ST", "11111", "USA", true, LocalDateTime.now());
        
        when(addressRepository.getDefaultAddress(userId)).thenReturn(defaultAddress);

        // Act
        UserAddress result = addressService.getDefaultAddress(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDefault());
        assertEquals("Default St", result.getStreetAddress());
        verify(addressRepository, times(1)).getDefaultAddress(userId);
    }
}
