package dropshipping_project.demo;

import dropshipping_project.demo.payload.MessageResponse;
import dropshipping_project.demo.payload.OrderRequest;
import dropshipping_project.demo.payload.ShipOrderRequest;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List; 

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    // Use hasAuthority to match the exact string in the database/token
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest orderRequest, Authentication authentication) {
        String username = authentication.getName();
        try {
            Order newOrder = orderService.createOrder(orderRequest, username);
            return ResponseEntity.ok(newOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPPLIER') or hasAuthority('ROLE_STAFF')")
    public ResponseEntity<?> shipOrder(@PathVariable Long orderId, 
                                       @Valid @RequestBody ShipOrderRequest shipRequest) {
        try {
            Order updatedOrder = orderService.shipOrder(orderId, shipRequest);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping
    // --- UPDATED: Use hasAuthority('ROLE_...') ---
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER') or hasAuthority('ROLE_SUPPLIER') or hasAuthority('ROLE_ADMIN')") 
    public ResponseEntity<List<Order>> getOrderHistory(Authentication authentication) {
        
        // --- DEBUG LOGS ---
        System.out.println("DEBUG: /api/orders endpoint reached!");
        if (authentication != null) {
            System.out.println("DEBUG: User is: " + authentication.getName());
            System.out.println("DEBUG: Authorities: " + authentication.getAuthorities());
        }
        // ------------------

        List<Order> orders = orderService.getOrders(authentication);
        return ResponseEntity.ok(orders);
    }
}