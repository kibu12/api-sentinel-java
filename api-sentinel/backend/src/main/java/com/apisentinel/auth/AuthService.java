package com.apisentinel.auth;

import com.apisentinel.audit.AuditService;
import com.apisentinel.exception.ForbiddenException;
import com.apisentinel.exception.ResourceNotFoundException;
import com.apisentinel.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new ForbiddenException("Email is already registered: " + request.email());
        }

        String role = (request.role() != null && request.role().equalsIgnoreCase("ADMIN"))
                ? "ROLE_ADMIN"
                : "ROLE_USER";

        User user = new User();
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setStatus("ACTIVE");

        user = userRepository.save(user);

        auditService.record(user, "USER_REGISTERED", "USER", user.getId().toString(), "Role: " + role);

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            auditService.record(null, "LOGIN_FAILED", "USER", user.getId().toString(), "Invalid password attempt");
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new ForbiddenException("Account is disabled");
        }

        auditService.record(user, "LOGIN_SUCCESS", "USER", user.getId().toString(), "Login authenticated");

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("No authenticated user in context");
        }

        String userIdStr = (String) auth.getPrincipal();
        return userRepository.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
