package com.swirl.ecomengine.admin.user.service;

import com.swirl.ecomengine.common.exception.UserAccessDeniedException;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import com.swirl.ecomengine.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers(User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new UserAccessDeniedException();
        }

        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id, User adminUser) {

        if (!adminUser.isAdmin()) {
            throw new UserAccessDeniedException();
        }

        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }
}