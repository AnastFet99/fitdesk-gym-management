package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.dto.AuthResponse;
import gr.aueb.cf10.gymapp.dto.LoginRequest;
import gr.aueb.cf10.gymapp.dto.RegisterRequest;
import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.repository.UserRepository;
import gr.aueb.cf10.gymapp.security.CustomUserDetails;
import gr.aueb.cf10.gymapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.error("User with email {} already exists", request.email());
            throw new EntityAlreadyExistsException("User", "email", request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with uuid: {}", savedUser.getUuid());

        // Generate JWT token
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getUuid(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            log.info("User logged in successfully: {}", request.email());

            return new AuthResponse(
                    token,
                    user.getUuid(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            );
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
