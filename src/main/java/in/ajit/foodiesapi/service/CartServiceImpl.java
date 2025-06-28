package in.ajit.foodiesapi.service;

import in.ajit.foodiesapi.entity.CartEntity;
import in.ajit.foodiesapi.io.CartRequest;
import in.ajit.foodiesapi.io.CartResponse;
import in.ajit.foodiesapi.repository.CartRepository;
import in.ajit.foodiesapi.repository.FoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserService userService;
    private final FoodRepository foodRepository;

//    @Override
//    public CartResponse addToCart(CartRequest request) {
//        String loggedInUserId = userService.findByUserId();
//        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
//        CartEntity cart = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
//        Map<String, Integer> cartItems = cart.getItems();
//        cartItems.put(request.getFoodId(), cartItems.getOrDefault(request.getFoodId(), 0) + 1);
//        cart.setItems(cartItems);
//        cart = cartRepository.save(cart);
//        return convertToResponse(cart);
//    }


    @Override
    public CartResponse addToCart(CartRequest request) {
        String foodId = request.getFoodId();
        if (!foodRepository.existsById(foodId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid foodId");
        }
        String loggedInUserId = userService.findByUserId();
        Optional<CartEntity> cartOptional = cartRepository.findByUserId(loggedInUserId);
        CartEntity cart = cartOptional.orElseGet(() -> new CartEntity(loggedInUserId, new HashMap<>()));
        Map<String, Integer> cartItems = cart.getItems();
        int quantityToAdd = request.getQuantity() != null ? request.getQuantity() : 1;
        cartItems.put(foodId, cartItems.getOrDefault(foodId, 0) + quantityToAdd);
        cart.setItems(cartItems);
        cart = cartRepository.save(cart);
        return convertToResponse(cart);
    }

    @Override
    public CartResponse getCart() {
        String loggedInUserId = userService.findByUserId();
        CartEntity entity = cartRepository.findByUserId(loggedInUserId)
                .orElse(new CartEntity(null, loggedInUserId, new HashMap<>()));
        return convertToResponse(entity);
    }

    @Override
    public void clearCart() {
        String loggedInUserId = userService.findByUserId();
        cartRepository.deleteByUserId(loggedInUserId);
    }

    @Override
    public CartResponse removeFromCart(CartRequest cartRequest) {
        String loggedInUserId = userService.findByUserId();
        CartEntity entity = cartRepository.findByUserId(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Cart is not found"));
        Map<String, Integer> cartItems = entity.getItems();
        if (cartItems.containsKey(cartRequest.getFoodId())) {
            int currentQty = cartItems.get(cartRequest.getFoodId());
            if (currentQty > 0) {
                cartItems.put(cartRequest.getFoodId(), currentQty - 1);
            } else {
                cartItems.remove(cartRequest.getFoodId());
            }
            entity = cartRepository.save(entity);
        }
        return convertToResponse(entity);
    }

    private CartResponse convertToResponse(CartEntity cartEntity) {
        return CartResponse.builder()
                .id(cartEntity.getId())
                .userId(cartEntity.getUserId())
                .items(cartEntity.getItems())
                .build();
    }
}
