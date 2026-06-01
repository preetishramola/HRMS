package com.PreetishRamola.hrms.auth;

import com.PreetishRamola.hrms.employee.User;
import com.PreetishRamola.hrms.employee.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        String token = jwtService.generateToken(user);

        return JwtResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .employeeId(user.getEmployee() != null ? user.getEmployee().getId() : null)
                .name(user.getEmployee() != null ? user.getEmployee().getFullName() : user.getEmail())
                .email(user.getEmail())
                .build();
    }
}