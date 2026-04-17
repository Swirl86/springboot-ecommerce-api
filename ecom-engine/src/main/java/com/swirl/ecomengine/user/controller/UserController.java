package com.swirl.ecomengine.user.controller;

import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.dto.FullProfileResponse;
import com.swirl.ecomengine.user.dto.UpdateUserProfileRequest;
import com.swirl.ecomengine.user.dto.UserProfileResponse;
import com.swirl.ecomengine.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile")
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------------------------------------------------------
    // GET PROFILE
    // ---------------------------------------------------------
    @Operation(
            summary = "Get user profile",
            description = "Returns the authenticated user's profile information including email, name and order count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    })
    @GetMapping
    public UserProfileResponse getProfile(@AuthenticatedUser User user) {
        return userService.getProfile(user);
    }

    // ---------------------------------------------------------
    // GET FULL PROFILE (USER + ADDRESS)
    // ---------------------------------------------------------
    @Operation(
            summary = "Get full user profile",
            description = "Returns the authenticated user's profile including personal information, order count and address."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full profile retrieved successfully")
    })
    @GetMapping("/full-profile")
    public FullProfileResponse getFullProfile(@AuthenticatedUser User user) {
        return userService.getFullProfile(user);
    }

    // ---------------------------------------------------------
    // UPDATE PROFILE
    // ---------------------------------------------------------
    @Operation(
            summary = "Update user profile",
            description = "Updates the authenticated user's profile information including name, email, phone and password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid profile data")
    })
    @PutMapping
    public UserProfileResponse updateProfile(
            @AuthenticatedUser User user,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userService.updateProfile(user, request);
    }
}