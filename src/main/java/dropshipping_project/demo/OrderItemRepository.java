package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // List<Order> findAllByUser(User user);
    boolean existsByProduct(Product product);
}