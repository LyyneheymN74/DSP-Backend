package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // This finds all orders for a specific customer
    List<Order> findAllByUser(User user);

    // --- UPDATED QUERY ---
    // This query finds all orders that contain at least one product
    // belonging to the given supplier.
    // We use DISTINCT to avoid duplicate orders if an order has multiple items from the same supplier.
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.product p " +
           "WHERE p.supplier = :supplier")
    List<Order> findAllOrdersBySupplier(@Param("supplier") Supplier supplier);
    
    /**
     * This is a custom query for the Admin dashboard.
     * It fetches ALL orders and eagerly joins all related data
     * (user, items, product, supplier, shipping) to prevent
     * lazy loading (N+1) problems when sending the JSON response.
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.orderItems oi " +
           "LEFT JOIN FETCH oi.product p " +
           "LEFT JOIN FETCH p.supplier s " +
           "LEFT JOIN FETCH o.shipping sh")
    List<Order> findAllWithDetails();
}