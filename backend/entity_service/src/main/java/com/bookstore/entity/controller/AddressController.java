package com.bookstore.entity.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.entity.service.AddressService;
import com.example.common.controller.BaseController;
import com.example.database.generated.tables.pojos.UserAddress;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping(path = "/address", produces = { MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Address", description = "APIs for user address management")
@RequiredArgsConstructor
@Slf4j
public class AddressController extends BaseController {

    private final AddressService addressService;

    @Operation(summary = "Get all addresses for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserAddress.class)))
            })
    })
    @GetMapping
    public List<UserAddress> getAddresses() {
        Integer userId = getCurrentUserId();
        log.info("Getting addresses for user: {}", userId);
        return addressService.getAddressesByUserId(userId);
    }

    @Operation(summary = "Get default address for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserAddress.class))
            }),
            @ApiResponse(responseCode = "404", description = "No default address found")
    })
    @GetMapping("/default")
    public UserAddress getDefaultAddress() {
        Integer userId = getCurrentUserId();
        log.info("Getting default address for user: {}", userId);
        return addressService.getDefaultAddress(userId);
    }

    @Operation(summary = "Create new address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address created", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserAddress.class))
            })
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAddress createAddress(@RequestBody CreateAddressRequest request) {
        Integer userId = getCurrentUserId();
        log.info("Creating address for user: {}", userId);
        
        UserAddress address = new UserAddress(
                null,  // id will be generated
                null,  // userId will be set by service
                request.getStreetAddress(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getCountry(),
                request.getIsDefault() != null && request.getIsDefault(),
                null  // createdAt will be set by database
        );
        
        return addressService.createAddress(userId, address);
    }

    @Operation(summary = "Update address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your address"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PutMapping("/{addressId}")
    public void updateAddress(
            @Parameter(description = "Address ID") @PathVariable int addressId,
            @RequestBody UpdateAddressRequest request) {
        Integer userId = getCurrentUserId();
        log.info("User {} updating address: {}", userId, addressId);
        
        UserAddress address = new UserAddress(
                null,  // id will be set by service
                null,  // userId will be set by service
                request.getStreetAddress(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getCountry(),
                request.getIsDefault() != null && request.getIsDefault(),
                null  // createdAt will be preserved by service
        );
        
        addressService.updateAddress(userId, addressId, address);
    }

    @Operation(summary = "Delete address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Address deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your address"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @Parameter(description = "Address ID") @PathVariable int addressId) {
        Integer userId = getCurrentUserId();
        log.info("User {} deleting address: {}", userId, addressId);
        addressService.deleteAddress(userId, addressId);
    }

    @Operation(summary = "Set default address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Default address updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your address"),
            @ApiResponse(responseCode = "404", description = "Address not found")
    })
    @PatchMapping("/{addressId}/set-default")
    public void setDefaultAddress(
            @Parameter(description = "Address ID") @PathVariable int addressId) {
        Integer userId = getCurrentUserId();
        log.info("User {} setting default address: {}", userId, addressId);
        addressService.setDefaultAddress(userId, addressId);
    }
}

@Data
class CreateAddressRequest {
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}

@Data
class UpdateAddressRequest {
    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}
