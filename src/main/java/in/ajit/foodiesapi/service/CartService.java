package in.ajit.foodiesapi.service;

import in.ajit.foodiesapi.io.CartRequest;
import in.ajit.foodiesapi.io.CartResponse;

public interface CartService {
    CartResponse addToCart(CartRequest request);

    CartResponse getCart();

    void clearCart();

    CartResponse removeFromCart(CartRequest cartRequest);
}
