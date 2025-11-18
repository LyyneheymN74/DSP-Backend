package dropshipping_project.demo;

// Import payload DTO
import dropshipping_project.demo.payload.CategoryRequest;
import dropshipping_project.demo.payload.MessageResponse;

// Import Java, Spring, and Jakarta classes
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- For security
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // --- THIS IS NEW ---
    @Autowired
    private ProductRepository productRepository; // For the delete safety check
    // --- END OF NEW ---

    /**
     * PUBLIC endpoint for anyone to see all categories.
     * (This method is unchanged)
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    // --- FROM HERE DOWN, EVERYTHING IS NEW ---

    /**
     * ADMIN-ONLY endpoint to create a new category.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        // Check if the name already exists
        if (categoryRepository.findByName(categoryRequest.getName()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Category name is already taken!"));
        }

        // Create new category
        Category category = new Category(
                categoryRequest.getName(),
                categoryRequest.getDescription()
        );

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    /**
     * ADMIN-ONLY endpoint to update an existing category.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {
        
        // 1. Find the category we want to edit
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Category not found with id " + id));

        // 2. Check if the new name is already taken by a *different* category
        Optional<Category> byName = categoryRepository.findByName(categoryRequest.getName());
        if (byName.isPresent() && !byName.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Category name is already taken!"));
        }

        // 3. Update the fields and save
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * ADMIN-ONLY endpoint to delete a category.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        
        // 1. Find the category
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Category not found with id " + id));

        // 2. SAFETY CHECK: Check if any products are using this category
        if (productRepository.existsByCategory(category)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cannot delete. This category is still in use by products."));
        }

        // 3. If no products are using it, delete it
        categoryRepository.delete(category);
        return ResponseEntity.ok(new MessageResponse("Category deleted successfully!"));
    }
}