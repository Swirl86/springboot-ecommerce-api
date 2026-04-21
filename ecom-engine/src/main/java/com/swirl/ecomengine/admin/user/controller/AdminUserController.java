package com.swirl.ecomengine.admin.user.controller;

import com.swirl.ecomengine.admin.user.AdminUserMapper;
import com.swirl.ecomengine.admin.user.dto.AdminUserResponse;
import com.swirl.ecomengine.admin.user.service.AdminUserService;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin Users", description = "Admin-only user management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminUserController {

    private final AdminUserService service;
    private final AdminUserMapper mapper;

    public AdminUserController(AdminUserService service, AdminUserMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // GET ALL USERS
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "List all users", description = "Returns all users with basic profile info. Admin only.")
    @ApiResponse(responseCode = "200", description = "Users returned successfully")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(
            @AuthenticatedUser User admin
    ) {
        var users = service.getAllUsers(admin);

        return ResponseEntity.ok(
                users.stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    // ---------------------------------------------------------
    // GET USER BY ID
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns detailed user info. Admin only.")
    public ResponseEntity<AdminUserResponse> getUserById(
            @AuthenticatedUser User admin,
            @PathVariable Long id
    ) {
        var user = service.getUserById(id, admin);
        return ResponseEntity.ok(mapper.toResponse(user));
    }
}
