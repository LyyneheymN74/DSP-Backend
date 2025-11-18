package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // This will be used by the OrderService
    Optional<Inventory> findByProduct(Product product);

    // This will be used by the InventoryController
    Optional<Inventory> findByProductId(Long productId);
}