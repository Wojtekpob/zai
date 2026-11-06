package pl.edu.pw.ee.zai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.edu.pw.ee.zai.model.UserEntity;
import pl.edu.pw.ee.zai.repository.UserRepository;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username:admin}") String adminUsername,
                           @Value("${app.admin.password:Admin123!}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            UserEntity admin = new UserEntity(
                    adminUsername,
                    passwordEncoder.encode(adminPassword),
                    "ROLE_ADMIN",
                    Instant.now()
            );
            userRepository.save(admin);
        }
    }
}
