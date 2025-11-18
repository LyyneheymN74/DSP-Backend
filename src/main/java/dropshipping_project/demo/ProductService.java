package dropshipping_project.demo;

// Import payload DTOs
import dropshipping_project.demo.payload.ProductRequest;

// Import Java, Spring, and Jakarta classes
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// All entities (User, Product, etc.) and repositories (UserRepository, etc.)
// are in the same package, so no other imports are needed.

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository; // For delete safety check

    /**
     * Creates a new product and its initial inventory record.
     */
    @Transactional
    public Product createProduct(ProductRequest productRequest, String username) {
        
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Error: Category not found with ID " + productRequest.getCategoryId()));

        User loggedInUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' not found"));

        Supplier supplier = supplierRepository.findByUser(loggedInUser)
                .orElseThrow(() -> new RuntimeException("Error: Supplier profile not found for user '" + username + "'."));

        // 1. Create the product
        Product product = new Product(
                productRequest.getName(),
                productRequest.getDescription(),
                productRequest.getPrice(),
                category,
                supplier
                // --- THIS IS THE FIX ---
                // We now pass the imageUrl from the request to the constructor
                // (Make sure your Product.java constructor accepts this)
        );
        product.setImageUrl(productRequest.getImageUrl()); // <-- ADD THIS LINE

        // 2. Create its initial inventory (starting at 0)
        Inventory inventory = new Inventory(product, 0);
        
        // 3. Link them together
        product.setInventory(inventory);
        
        // 4. Save the product. This will also save the new Inventory
        //    because of cascade = CascadeType.ALL.
        return productRepository.save(product);
    }

    /**
     * Gets all available products.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Updates an existing product.
     * Includes security logic to check ownership.
     */
    @Transactional
    public Product updateProduct(Long productId, ProductRequest productRequest, String loggedInUsername) {

        // 1. Find the logged-in user and their role
        User loggedInUser = userRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isAdmin = loggedInUser.getRole().getName().equals(ERole.ROLE_ADMIN);

        // 2. Find the product to be updated
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with ID " + productId));

        // 3. SECURITY CHECK
        // If the user is NOT an admin, we must check if they are the supplier who owns this product.
        if (!isAdmin) {
            // Find the supplier profile for the logged-in user
            Supplier loggedInSupplier = supplierRepository.findByUser(loggedInUser)
                    .orElseThrow(() -> new RuntimeException("Error: Supplier profile not found."));
            
            // Check if the product's supplier ID matches the logged-in supplier's ID
            if (product.getSupplier() == null || !product.getSupplier().getId().equals(loggedInSupplier.getId())) {
                // If they don't match, throw an error
                throw new AccessDeniedException("Access Denied: You do not own this product.");
            }
        }
        
        // 4. Find the new category
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Error: Category not found with ID " + productRequest.getCategoryId()));

        // 5. Update the product's fields
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);
        
        // --- THIS IS THE FIX ---
        product.setImageUrl(productRequest.getImageUrl()); // <-- ADD THIS LINE
        
        // Note: The supplier is not changed.
        // We are not updating inventory here, only product details.

        // 6. Save and return the updated product
        return productRepository.save(product);
    }

    /**
     * Gets all products for the currently logged-in supplier.
     */
    public List<Product> getMyProducts(String loggedInUsername) {
        // 1. Find the logged-in user
        User loggedInUser = userRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. Find their supplier profile
        Supplier loggedInSupplier = supplierRepository.findByUser(loggedInUser)
                .orElseThrow(() -> new RuntimeException("Error: Supplier profile not found."));
        
        // 3. Use the new repository method to find their products
        return productRepository.findAllBySupplier(loggedInSupplier);
    }

    /**
     * Deletes a product.
     * Includes security logic to check ownership.
     * Includes safety logic to prevent deleting a product that has been ordered.
     */
    @Transactional
    public void deleteProduct(Long productId, String loggedInUsername) {

        // 1. Find the logged-in user and their role
        User loggedInUser = userRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isAdmin = loggedInUser.getRole().getName().equals(ERole.ROLE_ADMIN);

        // 2. Find the product to be deleted
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Error: Product not found with ID " + productId));

        // 3. SECURITY CHECK (Ownership)
        // If the user is NOT an admin, check if they own this product.
        if (!isAdmin) {
            Supplier loggedInSupplier = supplierRepository.findByUser(loggedInUser)
                    .orElseThrow(() -> new RuntimeException("Error: Supplier profile not found."));
            
            if (product.getSupplier() == null || !product.getSupplier().getId().equals(loggedInSupplier.getId())) {
                throw new AccessDeniedException("Access Denied: You do not own this product.");
            }
        }

        // 4. SAFETY CHECK (Order History)
        // Check if this product is part of any OrderItem
        if (orderItemRepository.existsByProduct(product)) {
            throw new RuntimeException("Error: Cannot delete. This product is part of a customer's order history.");
        }

        // 5. If all checks pass, delete the product.
        // Spring Data JPA will automatically handle deleting the associated
        // Inventory record because of the CascadeType.ALL link.
        productRepository.delete(product);
    }
}
