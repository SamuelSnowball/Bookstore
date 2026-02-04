package com.bookstore.entity.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bookstore.entity.repository.AddressRepository;
import com.example.database.generated.tables.pojos.UserAddress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    public List<UserAddress> getAddressesByUserId(int userId) {
        log.info("Getting addresses for user: {}", userId);
        return addressRepository.getAddressesByUserId(userId);
    }

    public UserAddress getAddressById(int id) {
        log.info("Getting address by id: {}", id);
        UserAddress address = addressRepository.getAddressById(id);
        if (address == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found with id: " + id);
        }
        return address;
    }

    public UserAddress createAddress(int userId, UserAddress address) {
        log.info("Creating address for user: {}", userId);
        
        // If this is the first address, make it default
        List<UserAddress> existingAddresses = addressRepository.getAddressesByUserId(userId);
        boolean shouldBeDefault = existingAddresses.isEmpty() || Boolean.TRUE.equals(address.getIsDefault());
        
        // Create new immutable instance with userId and default flag
        UserAddress addressToCreate = new UserAddress(
                null,  // id will be generated
                userId,
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                shouldBeDefault,
                null  // createdAt will be set by database
        );
        Integer createdId = addressRepository.createAddress(addressToCreate);
        return addressRepository.getAddressById(createdId);
    }

    public void updateAddress(int userId, int addressId, UserAddress address) {
        log.info("Updating address {} for user: {}", addressId, userId);
        UserAddress existing = getAddressById(addressId);
        
        // Verify this address belongs to the user
        if (!existing.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }
        
        // Create new immutable instance with addressId and userId
        UserAddress addressToUpdate = new UserAddress(
                addressId,
                userId,
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                existing.getCreatedAt()  // preserve createdAt
        );
        
        addressRepository.updateAddress(addressToUpdate);
    }

    public void deleteAddress(int userId, int addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);
        UserAddress existing = getAddressById(addressId);
        
        // Verify this address belongs to the user
        if (!existing.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }
        
        addressRepository.deleteAddress(addressId);
    }

    public void setDefaultAddress(int userId, int addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);
        UserAddress existing = getAddressById(addressId);
        
        // Verify this address belongs to the user
        if (!existing.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
        }
        
        addressRepository.setDefaultAddress(userId, addressId);
    }

    public UserAddress getDefaultAddress(int userId) {
        log.info("Getting default address for user: {}", userId);
        return addressRepository.getDefaultAddress(userId);
    }
}
