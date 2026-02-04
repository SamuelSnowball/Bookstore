package com.bookstore.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.database.generated.tables.pojos.CartItem;
import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.bookstore.order.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final int USER_ID = 1;
    private static final int BOOK_ID = 100;
    private static final int CART_ITEM_ID = 1;
    private static final int QUANTITY = 3;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItem cart;

    @Mock
    private CartItemDetailVw cartItem;

    @InjectMocks
    private CartService cartService;

    @Test
    void testGetCartItems() {
        // Arrange
        List<CartItemDetailVw> mockCartItems = Arrays.asList(cartItem, cartItem);
        when(cartRepository.getCartItemsByUserId(USER_ID)).thenReturn(mockCartItems);

        // Act
        List<CartItemDetailVw> result = cartService.getCartItems(USER_ID);

        // Assert
        assertEquals(2, result.size());
        verify(cartRepository, times(1)).getCartItemsByUserId(USER_ID);
    }

    @Test
    void testAddToCart() {
        // Act
        cartService.addToCart(USER_ID, BOOK_ID, QUANTITY);

        // Assert
        verify(cartRepository, times(1)).addToCart(USER_ID, BOOK_ID, QUANTITY);
    }

    @Test
    void testUpdateQuantity_WithPositiveQuantity() {
        when(cartRepository.getCartItemById(CART_ITEM_ID)).thenReturn(cart);
        when(cart.getUserId()).thenReturn(USER_ID);

        // Act
        cartService.updateQuantity(USER_ID, CART_ITEM_ID, QUANTITY);

        // Assert
        verify(cartRepository, times(1)).updateCartItemQuantity(CART_ITEM_ID, QUANTITY);
        verify(cartRepository, times(0)).removeFromCart(anyInt());
    }

    @Test
    void testUpdateQuantity_WithZeroQuantity_RemovesItem() {
        when(cartRepository.getCartItemById(CART_ITEM_ID)).thenReturn(cart);
        when(cart.getUserId()).thenReturn(USER_ID);

        // Act
        cartService.updateQuantity(USER_ID, CART_ITEM_ID, 0);

        // Assert
        verify(cartRepository, times(1)).removeFromCart(CART_ITEM_ID);
        verify(cartRepository, times(0)).updateCartItemQuantity(anyInt(), anyInt());
    }

    @Test
    void testUpdateQuantity_WithNegativeQuantity_RemovesItem() {
        when(cartRepository.getCartItemById(CART_ITEM_ID)).thenReturn(cart);
        when(cart.getUserId()).thenReturn(USER_ID);

        // Act
        cartService.updateQuantity(USER_ID, CART_ITEM_ID, -1);

        // Assert
        verify(cartRepository, times(1)).removeFromCart(CART_ITEM_ID);
        verify(cartRepository, times(0)).updateCartItemQuantity(anyInt(), anyInt());
    }

    @Test
    void testRemoveItem() {
        when(cartRepository.getCartItemById(CART_ITEM_ID)).thenReturn(cart);
        when(cart.getUserId()).thenReturn(USER_ID);

        // Act
        cartService.removeItem(USER_ID, CART_ITEM_ID);

        // Assert
        verify(cartRepository, times(1)).removeFromCart(CART_ITEM_ID);
    }

    @Test
    void testClearCart() {
        // Act
        cartService.clearCart(USER_ID);

        // Assert
        verify(cartRepository, times(1)).clearCart(USER_ID);
    }
}

