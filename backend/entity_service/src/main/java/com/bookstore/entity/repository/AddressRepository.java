package com.bookstore.entity.repository;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.database.generated.tables.pojos.UserAddress;
import static com.example.database.generated.tables.UserAddress.USER_ADDRESS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AddressRepository {

    private final DSLContext create;

    public List<UserAddress> getAddressesByUserId(int userId) {
        return create.selectFrom(USER_ADDRESS)
                .where(USER_ADDRESS.USER_ID.eq(userId))
                .fetch()
                .into(UserAddress.class);
    }

    public UserAddress getAddressById(int id) {
        return create.selectFrom(USER_ADDRESS)
                .where(USER_ADDRESS.ID.eq(id))
                .fetchOneInto(UserAddress.class);
    }

    public Integer createAddress(UserAddress address) {
        // If this is set as default, unset all other defaults for this user
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            create.update(USER_ADDRESS)
                    .set(USER_ADDRESS.IS_DEFAULT, false)
                    .where(USER_ADDRESS.USER_ID.eq(address.getUserId()))
                    .execute();
        }

        return create.insertInto(USER_ADDRESS,
                USER_ADDRESS.USER_ID,
                USER_ADDRESS.STREET_ADDRESS,
                USER_ADDRESS.CITY,
                USER_ADDRESS.STATE,
                USER_ADDRESS.POSTAL_CODE,
                USER_ADDRESS.COUNTRY,
                USER_ADDRESS.IS_DEFAULT)
                .values(
                        address.getUserId(),
                        address.getStreetAddress(),
                        address.getCity(),
                        address.getState(),
                        address.getPostalCode(),
                        address.getCountry(),
                        address.getIsDefault() != null && address.getIsDefault())
                .returningResult(USER_ADDRESS.ID)
                .fetchOne()
                .into(Integer.class);
    }

    public void updateAddress(UserAddress address) {
        // If this is set as default, unset all other defaults for this user
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            create.update(USER_ADDRESS)
                    .set(USER_ADDRESS.IS_DEFAULT, false)
                    .where(USER_ADDRESS.USER_ID.eq(address.getUserId())
                            .and(USER_ADDRESS.ID.ne(address.getId())))
                    .execute();
        }

        create.update(USER_ADDRESS)
                .set(USER_ADDRESS.STREET_ADDRESS, address.getStreetAddress())
                .set(USER_ADDRESS.CITY, address.getCity())
                .set(USER_ADDRESS.STATE, address.getState())
                .set(USER_ADDRESS.POSTAL_CODE, address.getPostalCode())
                .set(USER_ADDRESS.COUNTRY, address.getCountry())
                .set(USER_ADDRESS.IS_DEFAULT, address.getIsDefault())
                .where(USER_ADDRESS.ID.eq(address.getId()))
                .execute();
    }

    public void deleteAddress(int id) {
        create.deleteFrom(USER_ADDRESS)
                .where(USER_ADDRESS.ID.eq(id))
                .execute();
    }

    public void setDefaultAddress(int userId, int addressId) {
        // Unset all defaults for this user
        create.update(USER_ADDRESS)
                .set(USER_ADDRESS.IS_DEFAULT, false)
                .where(USER_ADDRESS.USER_ID.eq(userId))
                .execute();
        // Set the specified address as default
        create.update(USER_ADDRESS)
                .set(USER_ADDRESS.IS_DEFAULT, true)
                .where(USER_ADDRESS.ID.eq(addressId)
                        .and(USER_ADDRESS.USER_ID.eq(userId)))
                .execute();
    }

    public UserAddress getDefaultAddress(int userId) {
        return create.selectFrom(USER_ADDRESS)
                .where(USER_ADDRESS.USER_ID.eq(userId)
                        .and(USER_ADDRESS.IS_DEFAULT.eq(true)))
                .fetchOneInto(UserAddress.class);
    }
}
