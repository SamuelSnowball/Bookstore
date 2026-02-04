package com.bookstore.order.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.common.database.MyDataSource;
import com.example.common.repository.BaseIntegrationTest;
import com.example.database.generated.tables.pojos.CartItem;
import com.example.database.generated.tables.pojos.CartItemDetailVw;

// Inherits @Transactional from BaseIntegrationTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MyDataSource.class, CartRepository.class})
class CartRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CartRepository cartRepository;

    private static final int USER_ID = 1;
    private static final int BOOK_ID = 1;
    private static final int QUANTITY = 2;

    @Test
    void testAddToCart_NewItem() {
        // Arrange - verify cart is empty initially
        assertEquals(0, cartRepository.getCartItemsByUserId(USER_ID).size());

        // Act - add new item
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);

        // Assert
        List<CartItemDetailVw> cartItems = cartRepository.getCartItemsByUserId(USER_ID);
        assertEquals(1, cartItems.size());
        assertEquals(USER_ID, cartItems.get(0).getUserId());
        assertEquals(BOOK_ID, cartItems.get(0).getBookId());
        assertEquals(QUANTITY, cartItems.get(0).getBookQuantity());
    }

    @Test
    void testAddToCart_ExistingItem_UpdatesQuantity() {
        // Arrange - add initial item
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        List<CartItemDetailVw> initialItems = cartRepository.getCartItemsByUserId(USER_ID);
        assertEquals(1, initialItems.size());
        int initialQuantity = initialItems.get(0).getBookQuantity();

        // Act - add same item again
        cartRepository.addToCart(USER_ID, BOOK_ID, 3);

        // Assert - quantity should be updated
        List<CartItemDetailVw> updatedItems = cartRepository.getCartItemsByUserId(USER_ID);
        assertEquals(1, updatedItems.size());
        assertEquals(initialQuantity + 3, updatedItems.get(0).getBookQuantity());
    }

    @Test
    void testGetCartItemsByUserId() {
        // Arrange - add multiple items
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        cartRepository.addToCart(USER_ID, BOOK_ID + 1, QUANTITY);

        // Act
        List<CartItemDetailVw> cartItems = cartRepository.getCartItemsByUserId(USER_ID);

        // Assert
        assertEquals(2, cartItems.size());
    }

    @Test
    void testGetCartItemsByUserId_EmptyCart() {
        // Act
        List<CartItemDetailVw> cartItems = cartRepository.getCartItemsByUserId(999);

        // Assert
        assertEquals(0, cartItems.size());
    }

    @Test
    void testGetCartItem() {
        // Arrange
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);

        // Act
        CartItem cartItem = cartRepository.getCartItem(USER_ID, BOOK_ID);

        // Assert
        assertNotNull(cartItem);
        assertEquals(USER_ID, cartItem.getUserId());
        assertEquals(BOOK_ID, cartItem.getBookId());
    }

    @Test
    void testGetCartItem_NotFound() {
        // Act
        CartItem cartItem = cartRepository.getCartItem(999, 999);

        // Assert
        assertNull(cartItem);
    }

    @Test
    void testUpdateCartItemQuantity() {
        // Arrange
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        CartItem item = cartRepository.getCartItem(USER_ID, BOOK_ID);
        int newQuantity = 5;

        // Act
        cartRepository.updateCartItemQuantity(item.getId(), newQuantity);

        // Assert
        CartItem updatedItem = cartRepository.getCartItem(USER_ID, BOOK_ID);
        assertEquals(newQuantity, updatedItem.getBookQuantity());
    }

    @Test
    void testRemoveFromCart() {
        // Arrange
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        CartItem item = cartRepository.getCartItem(USER_ID, BOOK_ID);
        assertEquals(1, cartRepository.getCartItemsByUserId(USER_ID).size());

        // Act
        cartRepository.removeFromCart(item.getId());

        // Assert
        assertEquals(0, cartRepository.getCartItemsByUserId(USER_ID).size());
    }

    @Test
    void testClearCart() {
        // Arrange - add multiple items
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        cartRepository.addToCart(USER_ID, BOOK_ID + 1, QUANTITY);
        cartRepository.addToCart(USER_ID, BOOK_ID + 2, QUANTITY);
        assertEquals(3, cartRepository.getCartItemsByUserId(USER_ID).size());

        // Act
        cartRepository.clearCart(USER_ID);

        // Assert
        assertEquals(0, cartRepository.getCartItemsByUserId(USER_ID).size());
    }

    @Test
    void testClearCart_OnlyRemovesSpecifiedUserItems() {
        // Arrange - add items for same user but different books
        cartRepository.addToCart(USER_ID, BOOK_ID, QUANTITY);
        cartRepository.addToCart(USER_ID, BOOK_ID + 1, QUANTITY);

        // Verify we have 2 items
        assertEquals(2, cartRepository.getCartItemsByUserId(USER_ID).size());

        // Act - clear cart for user
        cartRepository.clearCart(USER_ID);

        // Assert - all items for user should be removed
        assertEquals(0, cartRepository.getCartItemsByUserId(USER_ID).size());
    }
}

