package in.ajit.foodiesapi.service;

import org.springframework.security.core.Authentication;

public interface AuthenticationFacade {
    Authentication getAuthentication(); // for cart to get logged-in user
}
