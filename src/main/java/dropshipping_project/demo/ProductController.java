package dropshipping_project.demo;

// Import payload DTOs
import dropshipping_project.demo.payload.MessageResponse;
import dropshipping_project.demo.payload.ProductRequest;

// Import Java, Spring, and Jakarta classes
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * PUBLIC endpoint for anyone to see all products.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * SECURED endpoint for a supplier to see ONLY their own products.
     */
    @GetMapping("/myproducts")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')") // Also allow Admin
    public ResponseEntity<List<Product>> getSupplierProducts(Authentication authentication) {
        String username = authentication.getName();
        List<Product> products = productService.getMyProducts(username);
        return ResponseEntity.ok(products);
    }

    /**
     * SECURED endpoint to create a new product.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest, Authentication authentication) {
        String username = authentication.getName();
        try {
            Product newProduct = productService.createProduct(productRequest, username);
            return ResponseEntity.ok(newProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * SECURED endpoint to update an existing product.
     */
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, 
                                           @Valid @RequestBody ProductRequest productRequest, 
                                           Authentication authentication) {
        String username = authentication.getName();
        try {
            Product updatedProduct = productService.updateProduct(productId, productRequest, username);
            return ResponseEntity.ok(updatedProduct);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * SECURED endpoint to delete a product.
     * Secured for Admins (full access) and Suppliers (own products only).
     */
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId,
                                           Authentication authentication) {
        
        String username = authentication.getName();
        try {
            productService.deleteProduct(productId, username);
            return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            // This catches "Product not found" or "Cannot delete"
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}