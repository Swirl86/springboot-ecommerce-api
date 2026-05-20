package com.swirl.ecomengine.cart;

import com.swirl.ecomengine.cart.dto.CartItemResponse;
import com.swirl.ecomengine.cart.dto.CartResponse;
import com.swirl.ecomengine.cart.item.CartItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getItems().stream()
                        .map(item -> {
                            String imageUrl = item.getProduct().getImageUrls().isEmpty()
                                    ? null
                                    : item.getProduct().getImageUrls().get(0);

                            return new CartItemResponse(
                                    item.getId(),
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    imageUrl,
                                    item.getUnitPrice(),
                                    item.getQuantity(),
                                    item.getTotalPrice()
                            );
                        })
                        .collect(Collectors.toList()),
                total
        );
    }
}