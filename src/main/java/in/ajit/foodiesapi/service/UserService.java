package in.ajit.foodiesapi.service;

import in.ajit.foodiesapi.io.UserRequest;
import in.ajit.foodiesapi.io.UserResponse;

public interface UserService {
    UserResponse registerUser(UserRequest request);

    String findByUserId();
}
