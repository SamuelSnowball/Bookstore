package com.bookstore.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.controller.BaseController;
import com.example.common.dto.CartItemDetailDto;
import com.example.database.generated.tables.pojos.CartItem;
import com.example.database.generated.tables.pojos.CartItemDetailVw;
import com.bookstore.order.service.CartService;

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
@RequestMapping(path = "/cart", produces = { MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Cart", description = "APIs for shopping cart operations")
@RequiredArgsConstructor
@Slf4j
public class CartController extends BaseController {

    private final CartService cartService;

    @Operation(summary = "Get cart items for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CartItem.class)))
            })
    })
    @GetMapping
    public List<CartItemDetailDto> getCartItems() {
        Integer userId = getCurrentUserId();
        log.info("Getting cart items for user: {}", userId);
        List<CartItemDetailVw> items = cartService.getCartItems(userId);
        
        // Convert jOOQ POJOs to DTOs for JSON serialization
        return items.stream()
                .map(item -> new CartItemDetailDto(
                    item.getCartItemId(),
                    item.getUserId(),
                    item.getBookId(),
                    item.getBookQuantity(),
                    item.getAuthorId(),
                    item.getTitle(),
                    item.getPrice(),
                    item.getDescription(),
                    item.getFirstName(),
                    item.getLastName()
                ))
                .toList();
    }

    @Operation(summary = "Add item to cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item added to cart")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addToCart(@RequestBody AddToCartRequest request) {
        Integer userId = getCurrentUserId();
        log.info("Adding to cart - userId: {}, bookId: {}, quantity: {}",
                userId, request.getBookId(), request.getQuantity());
        cartService.addToCart(userId, request.getBookId(), request.getQuantity());
    }

    @Operation(summary = "Update cart item quantity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your cart item"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @PutMapping("/{cartItemId}")
    public void updateQuantity(
            @Parameter(description = "Cart item ID") @PathVariable int cartItemId,
            @RequestBody UpdateQuantityRequest request) {
        Integer userId = getCurrentUserId();
        log.info("User with ID {} updating cart item {} to quantity: {}", userId, cartItemId, request.getQuantity());
        cartService.updateQuantity(userId, cartItemId, request.getQuantity());
    }

    @Operation(summary = "Remove item from cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your cart item"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @Parameter(description = "Cart item ID") @PathVariable int cartItemId) {
        Integer userId = getCurrentUserId();
        log.info("User with ID {} removing cart item: {}", userId, cartItemId);
        cartService.removeItem(userId, cartItemId);
    }

    @Operation(summary = "Clear all items from cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cart cleared")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        Integer userId = getCurrentUserId();
        log.info("User with ID {} clearing cart", userId);
        cartService.clearCart(userId);
    }

    @Data
    static class AddToCartRequest {
        private int bookId;
        private int quantity = 1;
    }

    @Data
    static class UpdateQuantityRequest {
        private int quantity;
    }
}
