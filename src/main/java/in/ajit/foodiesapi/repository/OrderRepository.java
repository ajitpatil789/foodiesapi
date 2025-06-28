package in.ajit.foodiesapi.repository;

import in.ajit.foodiesapi.entity.OrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<OrderEntity, String> {
    List<OrderEntity> findByUserId(String userId);

    Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);
}
