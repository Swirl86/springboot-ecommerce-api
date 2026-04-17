package com.swirl.ecomengine.address.controller;

import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.address.service.AddressService;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Address")
@RestController
@RequestMapping("/users/me/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService service;

    // ---------------------------------------------------------
    // CREATE OR UPDATE ADDRESS
    // ---------------------------------------------------------
    @Operation(
            summary = "Create or update address",
            description = "Creates a new address for the user or updates the existing one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid address data")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public AddressResponse createOrUpdate(
            @AuthenticatedUser User user,
            @Valid @RequestBody CreateOrUpdateAddressRequest req
    ) {
        return service.createOrUpdate(user, req);
    }

    // ---------------------------------------------------------
    // GET ADDRESS
    // ---------------------------------------------------------
    @Operation(
            summary = "Get address",
            description = "Returns the user's saved address, if one exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "User has no saved address")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public AddressResponse get(@AuthenticatedUser User user) {
        return service.get(user);
    }

    // ---------------------------------------------------------
    // DELETE ADDRESS
    // ---------------------------------------------------------
    @Operation(
            summary = "Delete address",
            description = "Deletes the user's saved address, if one exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address deleted successfully"),
            @ApiResponse(responseCode = "204", description = "User had no address to delete")
    })
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void delete(@AuthenticatedUser User user) {
        service.delete(user);
    }
}