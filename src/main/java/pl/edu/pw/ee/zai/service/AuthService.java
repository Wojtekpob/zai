package pl.edu.pw.ee.zai.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pw.ee.zai.dto.LoginRequest;
import pl.edu.pw.ee.zai.dto.LoginResponse;
import pl.edu.pw.ee.zai.dto.PasswordChangeRequest;
import pl.edu.pw.ee.zai.exception.ForbiddenException;
import pl.edu.pw.ee.zai.exception.UnauthorizedException;
import pl.edu.pw.ee.zai.model.RevokedTokenEntity;
import pl.edu.pw.ee.zai.model.UserEntity;
import pl.edu.pw.ee.zai.repository.RevokedTokenRepository;
import pl.edu.pw.ee.zai.repository.UserRepository;
import pl.edu.pw.ee.zai.security.JwtService;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RevokedTokenRepository revokedTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7).trim();
        String jti = jwtService.extractTokenId(token);
        if (jti != null) {
            Instant expiry = jwtService.extractExpiration(token);
            revokedTokenRepository.save(new RevokedTokenEntity(jti, expiry));
        }
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ForbiddenException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
