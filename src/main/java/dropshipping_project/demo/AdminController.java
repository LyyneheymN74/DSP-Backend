package dropshipping_project.demo;

// Import the new classes
import dropshipping_project.demo.Order;
import dropshipping_project.demo.OrderRepository;
import dropshipping_project.demo.payload.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;


    /**
     * ADMIN-ONLY: Gets a list of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * ADMIN-ONLY: Toggles a user's account (enabled/disabled).
     */
    @PutMapping("/users/{userId}/toggle")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);

        String status = user.isEnabled() ? "enabled" : "disabled";
        return ResponseEntity.ok(new MessageResponse("User " + user.getUsername() + " has been " + status));
    }

    /**
     * ADMIN-ONLY: Gets a list of ALL orders in the system.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        // --- THIS LINE IS UPDATED ---
        // Call the new method to get all data at once
        return ResponseEntity.ok(orderRepository.findAllWithDetails());
    }
}