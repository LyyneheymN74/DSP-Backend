package dropshipping_project.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a User by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a User exists with the given username.
     */
    Boolean existsByUsername(String username);

    /**
     * Checks if a User exists with the given email.
     */
    Boolean existsByEmail(String email);
}