package dropshipping_project.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; 

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * This bean will run on startup to populate the database with
     * test data (Roles, a Category, and a Supplier account).
     */
    @Bean
    public CommandLineRunner initialData(RoleRepository roleRepository, 
                                         CategoryRepository categoryRepository,
                                         SupplierRepository supplierRepository,
                                         UserRepository userRepository,
                                         PasswordEncoder passwordEncoder) { // Inject all repos
        return args -> {
            // 1. Populate Roles
            if (roleRepository.count() == 0) {
                System.out.println("Populating roles table...");
                roleRepository.save(new Role(ERole.ROLE_CUSTOMER));
                roleRepository.save(new Role(ERole.ROLE_SUPPLIER));
                roleRepository.save(new Role(ERole.ROLE_STAFF));
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
            }

            // 2. Populate a test Category
            if (categoryRepository.findByName("Electronics").isEmpty()) {
                System.out.println("Populating categories table...");
                Category electronics = new Category("Electronics", "Electronic devices");
                categoryRepository.save(electronics);
            }

            if (categoryRepository.findByName("Household").isEmpty()) {
                System.out.println("Populating categories table...");
                Category household = new Category("Household", "Household tools");
                categoryRepository.save(household);
            }

            // 3. Populate a test Supplier
            if (supplierRepository.count() == 0) {
                System.out.println("Creating test supplier account...");
                
                Role supplierRole = roleRepository.findByName(ERole.ROLE_SUPPLIER)
                        .orElseThrow(() -> new RuntimeException("Error: ROLE_SUPPLIER not found."));

                User supplierUser = new User(
                        "supplier1",
                        "supplier@test.com",
                        passwordEncoder.encode("password123") 
                );
                supplierUser.setRole(supplierRole);
                userRepository.save(supplierUser); 

                Supplier supplierProfile = new Supplier(
                        "MegaCorp Supplies",
                        "555-1234",
                        supplierUser 
                );
                supplierRepository.save(supplierProfile);
            }

            if (userRepository.findByUsername("supplier2").isEmpty()) {
                System.out.println("Creating test supplier2 account...");
                
                Role supplierRole = roleRepository.findByName(ERole.ROLE_SUPPLIER)
                        .orElseThrow(() -> new RuntimeException("Error: ROLE_SUPPLIER not found."));

                User supplierUser2 = new User(
                        "supplier2",
                        "supplier2@test.com",
                        passwordEncoder.encode("password123") 
                );
                supplierUser2.setRole(supplierRole);
                userRepository.save(supplierUser2); 

                Supplier supplierProfile2 = new Supplier(
                        "Aries Ltd.",
                        "074-7474",
                        supplierUser2 
                );
                supplierRepository.save(supplierProfile2);
            } else {
                System.out.println("supplier2 account already exists.");
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                System.out.println("Creating test admin account...");
                
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found."));

                User adminUser = new User(
                        "admin",
                        "admin@test.com",
                        passwordEncoder.encode("password123") 
                );
                adminUser.setRole(adminRole);
                userRepository.save(adminUser);
                                
            } else {
                System.out.println("admin account already exists.");
            }
        };
    }
}