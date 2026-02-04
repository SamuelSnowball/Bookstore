package com.bookstore.order.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.database.generated.tables.pojos.CartItem;
import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.bookstore.order.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;

    public List<CartItemDetailVw> getCartItems(int userId) {
        return cartRepository.getCartItemsByUserId(userId);
    }

    @Transactional
    public void addToCart(int userId, int bookId, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }
        cartRepository.addToCart(userId, bookId, quantity);
    }

    @Transactional
    public void updateQuantity(int userId, int cartItemId, int quantity) {
        verifyCartItemOwnership(userId, cartItemId);
        
        if (quantity <= 0) {
            cartRepository.removeFromCart(cartItemId);
        } else {
            cartRepository.updateCartItemQuantity(cartItemId, quantity);
        }
    }

    @Transactional
    public void removeItem(int userId, int cartItemId) {
        CartItem cartItem = cartRepository.getCartItemById(cartItemId);
        if (cartItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        if (cartItem.getUserId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this cart item");
        }
        
        // Decrement quantity by 1, or remove if it would become 0
        int newQuantity = cartItem.getBookQuantity() - 1;
        if (newQuantity <= 0) {
            cartRepository.removeFromCart(cartItemId);
        } else {
            cartRepository.updateCartItemQuantity(cartItemId, newQuantity);
        }
    }

    @Transactional
    public void clearCart(int userId) {
        cartRepository.clearCart(userId);
    }

    private void verifyCartItemOwnership(int userId, int cartItemId) {
        CartItem cartItem = cartRepository.getCartItemById(cartItemId);
        if (cartItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found");
        }
        if (cartItem.getUserId() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to modify this cart item");
        }
    }
}

