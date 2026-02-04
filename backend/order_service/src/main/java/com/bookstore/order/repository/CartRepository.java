package com.bookstore.order.repository;

import static com.example.database.generated.Tables.CART_ITEM;
import static com.example.database.generated.Tables.CART_ITEM_DETAIL_VW;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Repository;

import com.example.database.generated.tables.daos.CartItemDao;
import com.example.database.generated.tables.pojos.CartItem;
import com.example.database.generated.tables.pojos.CartItemDetailVw;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class CartRepository extends CartItemDao {

    private final DSLContext create;

    public CartRepository(DSLContext dslContext, DefaultConfiguration configuration) {
        super(configuration);
        this.create = dslContext;
    }

    public List<CartItemDetailVw> getCartItemsByUserId(int userId) {
        return create.selectFrom(CART_ITEM_DETAIL_VW)
                .where(CART_ITEM_DETAIL_VW.USER_ID.eq(userId))
                .fetch()
                .into(CartItemDetailVw.class);
    }

    public CartItem getCartItem(int userId, int bookId) {
        return create.selectFrom(CART_ITEM)
                .where(CART_ITEM.USER_ID.eq(userId).and(CART_ITEM.BOOK_ID.eq(bookId)))
                .fetchOneInto(CartItem.class);
    }

    // Use the DAO
    public CartItem getCartItemById(int cartItemId) {
        return create.selectFrom(CART_ITEM)
                .where(CART_ITEM.ID.eq(cartItemId))
                .fetchOneInto(CartItem.class);
    }

    public void addToCart(int userId, int bookId, int quantity) {
        log.debug("Adding to cart - userId: {}, bookId: {}, quantity: {}", userId, bookId, quantity);
        CartItem existingItem = getCartItem(userId, bookId);
        
        if (existingItem != null) {
            // Update quantity if item already exists
            int newQuantity = existingItem.getBookQuantity() + quantity;
            log.info("Item already in cart. Updating quantity from {} to {}", existingItem.getBookQuantity(), newQuantity);
            create.update(CART_ITEM)
                    .set(CART_ITEM.BOOK_QUANTITY, newQuantity)
                    .where(CART_ITEM.ID.eq(existingItem.getId()))
                    .execute();
        } else {
            // Insert new cart item
            CartItem newItem = new CartItem(null, userId, bookId, quantity);
            insert(newItem);
        }
    }

    public void updateCartItemQuantity(int cartItemId, int quantity) {
        create.update(CART_ITEM)
                .set(CART_ITEM.BOOK_QUANTITY, quantity)
                .where(CART_ITEM.ID.eq(cartItemId))
                .execute();
    }

    public void removeFromCart(int cartItemId) {
        create.deleteFrom(CART_ITEM)
                .where(CART_ITEM.ID.eq(cartItemId))
                .execute();
    }

    public void clearCart(int userId) {
        create.deleteFrom(CART_ITEM)
                .where(CART_ITEM.USER_ID.eq(userId))
                .execute();
    }
}

