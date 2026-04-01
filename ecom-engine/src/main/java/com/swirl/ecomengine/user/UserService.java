package com.swirl.ecomengine.user;

import com.swirl.ecomengine.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User update(User user) {
        return userRepository.save(user);
    }
}