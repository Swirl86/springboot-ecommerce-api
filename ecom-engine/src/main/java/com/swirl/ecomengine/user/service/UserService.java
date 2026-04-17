package com.swirl.ecomengine.user.service;

import com.swirl.ecomengine.address.Address;
import com.swirl.ecomengine.address.AddressRepository;
import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ConflictException;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import com.swirl.ecomengine.user.dto.FullProfileResponse;
import com.swirl.ecomengine.user.dto.UpdateUserProfileRequest;
import com.swirl.ecomengine.user.dto.UserProfileResponse;
import com.swirl.ecomengine.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.swirl.ecomengine.common.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

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
    // UPDATE USER (internal use)
    // ---------------------------------------------------------
    public User update(User user) {
        validateUserForUpdate(user);
        return userRepository.save(user);
    }

    private void validateUserForUpdate(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException(user.getId().toString());
        }
    }

    // ---------------------------------------------------------
    // PROFILE: GET
    // ---------------------------------------------------------
    public UserProfileResponse getProfile(User user) {
        return buildProfileResponse(user);
    }

    public FullProfileResponse getFullProfile(User user) {

        int orderCount = orderRepository.countByUserId(user.getId());

        Address address = addressRepository.findByUserId(user.getId());
        AddressResponse addressResponse = address == null ? null : new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry()
        );

        return new FullProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                orderCount,
                addressResponse
        );
    }

    // ---------------------------------------------------------
    // PROFILE: UPDATE
    // ---------------------------------------------------------
    public UserProfileResponse updateProfile(User user, UpdateUserProfileRequest req) {

        // NAME
        if (hasText(req.name())) {
            user.setName(req.name());
        }

        // EMAIL
        if (hasText(req.email())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new ConflictException("Email is already in use");
            }
            user.setEmail(req.email());
        }

        // PHONE
        if (hasText(req.phone())) {
            user.setPhone(req.phone());
        }

        // PASSWORD
        if (hasText(req.newPassword())) {

            if (req.currentPassword() == null || req.currentPassword().isBlank()) {
                throw new BadRequestException("Current password is required to change password");
            }

            if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }

            if (passwordEncoder.matches(req.newPassword(), user.getPassword())) {
                throw new BadRequestException("New password cannot be the same as the current password");
            }

            user.setPassword(passwordEncoder.encode(req.newPassword()));
        }

        userRepository.save(user);

        return buildProfileResponse(user);
    }

    // ---------------------------------------------------------
    // PROFILE RESPONSE BUILDER
    // ---------------------------------------------------------
    private UserProfileResponse buildProfileResponse(User user) {
        int orderCount = getOrderCountFor(user);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                orderCount
        );
    }

    private int getOrderCountFor(User user) {
        return orderRepository.countByUserId(user.getId());
    }
}