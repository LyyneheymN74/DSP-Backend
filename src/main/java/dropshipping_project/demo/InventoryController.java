package dropshipping_project.demo;

// Import payload DTO
import dropshipping_project.demo.payload.UpdateInventoryRequest;
import dropshipping_project.demo.payload.MessageResponse;

// Import Java, Spring, and Jakarta classes
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * SECURED endpoint for a Supplier or Admin to update product stock.
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateInventory(@PathVariable Long productId, 
                                             @Valid @RequestBody UpdateInventoryRequest request) {
        
        // 1. Find the product the user wants to update
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with ID " + productId));

        // -----------------------------------------------------------------
        // 
        // THIS IS THE FIX (replacing the line from your screenshot)
        // 
        // 2. Find the inventory for that product. 
        //    If it doesn't exist (returns null/empty), create a new Inventory object.
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElse(new Inventory(product, 0)); // <-- THIS IS THE NEW LOGIC
        //
        // -----------------------------------------------------------------


        // 3. Set the new quantity from the request
        inventory.setQuantity(request.getQuantity());

        // 4. Save the new or updated inventory record
        inventoryRepository.save(inventory);

        return ResponseEntity.ok(new MessageResponse("Inventory updated successfully to " + request.getQuantity()));
    }
}