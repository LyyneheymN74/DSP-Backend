package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // We can add a helper method to find a payment by its order
    Optional<Payment> findByOrder(Order order);
}