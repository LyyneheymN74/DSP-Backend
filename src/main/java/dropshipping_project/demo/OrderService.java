package dropshipping_project.demo;

// Import payload DTOs
import dropshipping_project.demo.payload.CartItemRequest;
import dropshipping_project.demo.payload.OrderRequest;
import dropshipping_project.demo.payload.ShipOrderRequest;

// Import Java, Spring, and Jakarta classes
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate; // <-- Added for initialization
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private ShippingRepository shippingRepository; 

    @Autowired
    private SupplierRepository supplierRepository;

    @Transactional
    public Order createOrder(OrderRequest orderRequest, String username) {
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BigDecimal total = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        for (CartItemRequest itemRequest : orderRequest.getItems()) {
            
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Error: Product with ID " + itemRequest.getProductId() + " not found."));
            
            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() -> new RuntimeException("Error: Inventory not found for product " + product.getName()));

            if (inventory.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Error: Not enough stock for " + product.getName() + ". Only " + inventory.getQuantity() + " available.");
            }
            
            inventory.setQuantity(inventory.getQuantity() - itemRequest.getQuantity());
            inventoryRepository.save(inventory); 
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity());
            orderItems.add(orderItem);
        }

        order.setTotalPrice(total);
        order.setOrderItems(orderItems);
        
        Payment payment = new Payment(order, total, "SIMULATED_CARD");
        order.setPayment(payment); 

        return orderRepository.save(order);
    }

    public Order shipOrder(Long orderId, ShipOrderRequest shipRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found with ID " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new RuntimeException("Error: This order has already been shipped.");
        }

        Shipping shipping = new Shipping(
                order,
                shipRequest.getTrackingNumber(),
                shipRequest.getShippingCompany()
        );
        shippingRepository.save(shipping); 

        order.setStatus(OrderStatus.SHIPPED);
        order.setShipping(shipping);
        
        return orderRepository.save(order);
    }

    /**
     * Gets the order history.
     * - If the user is a CUSTOMER, returns only their own orders.
     * - If the user is an ADMIN or SUPPLIER, returns all orders.
     */
    @Transactional // <--- CRITICAL: Keeps session open to load lazy data
    public List<Order> getOrders(Authentication authentication) {
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // ROBUST ROLE CHECKING
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        boolean isSupplier = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPLIER"));

        if (isAdmin) {
            System.out.println("User is ADMIN, fetching all orders.");
            List<Order> orders = orderRepository.findAll();
            // Initialize users for Admin too
            orders.forEach(o -> Hibernate.initialize(o.getUser())); 
            return orders;
        } 
        else if (isSupplier) {
            System.out.println("User is SUPPLIER, fetching supplier-specific orders.");
            Supplier supplier = supplierRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Error: Supplier profile not found for user."));
            
            List<Order> orders = orderRepository.findAllOrdersBySupplier(supplier);
            
            // --- THIS IS THE FIX FOR THE CRASH ---
            // We manually "touch" the user object to force it to load from the database
            // while we are still inside the Transaction. This fixes the "ByteBuddy" error.
            orders.forEach(order -> {
                if (order.getUser() != null) {
                    Hibernate.initialize(order.getUser());
                }
            });
            // -------------------------------------
            
            return orders;
        } 
        else {
            System.out.println("User is CUSTOMER, fetching their own orders.");
            return orderRepository.findAllByUser(user);
        }
    }
}