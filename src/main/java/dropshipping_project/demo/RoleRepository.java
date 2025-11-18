package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Finds a Role in the database by its ERole name.
     */
    Optional<Role> findByName(ERole name);
}