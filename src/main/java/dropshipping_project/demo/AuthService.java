package dropshipping_project.demo;

import dropshipping_project.demo.payload.JwtResponse;
import dropshipping_project.demo.payload.LoginRequest;
import dropshipping_project.demo.payload.RegisterRequest;
import dropshipping_project.demo.security.jwt.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Authenticates a user and generates a JWT.
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {

        logger.info("--- LOGIN ATTEMPT ---");
        logger.info("Username received: '{}'", loginRequest.getUsername());
        logger.info("Password length received: {}", loginRequest.getPassword().length());
        
        // This is where Spring Security does its magic
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // If successful, set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate the JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get user details from the authentication object
        User userDetails = (User) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse(null);

        // Return the response object
        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role);
    }

    /**
     * Registers a new user.
     */
    public void registerUser(RegisterRequest registerRequest) {
        // Check if username is already taken
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(registerRequest.getUsername(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword())); // <-- Always hash the password!

        // Assign the default "CUSTOMER" role
        Role userRole = roleRepository.findByName(ERole.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Error: Role 'CUSTOMER' is not found."));
        user.setRole(userRole);

        // Save the user to the database
        userRepository.save(user);
    }
}