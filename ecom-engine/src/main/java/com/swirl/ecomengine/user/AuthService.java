package com.swirl.ecomengine.user;

import com.swirl.ecomengine.common.exception.EmailAlreadyExistsException;
import com.swirl.ecomengine.user.dto.RegisterRequest;
import com.swirl.ecomengine.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRole()
        );
    }
}
