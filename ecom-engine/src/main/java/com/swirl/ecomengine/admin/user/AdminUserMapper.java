package com.swirl.ecomengine.admin.user;

import com.swirl.ecomengine.admin.user.dto.AdminUserResponse;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserMapper {

    private final OrderRepository orderRepository;

    public AdminUserResponse toResponse(User user) {
        int orderCount = orderRepository.countByUser(user);

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                orderCount
        );
    }
}