package com.swirl.ecomengine.user.service;

import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import com.swirl.ecomengine.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------
    public List<User> getAll() {
        return userRepository.findAll();
    }

    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------
    public User update(User user) {

        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }

        if (user.getId() == null) {
            throw new BadRequestException("User ID cannot be null");
        }

        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException(user.getId().toString());
        }

        return userRepository.save(user);
    }
}
