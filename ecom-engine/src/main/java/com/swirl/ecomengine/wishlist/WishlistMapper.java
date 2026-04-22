package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.wishlist.dto.WishlistResponse;
import org.springframework.stereotype.Component;

@Component
public class WishlistMapper {

    public WishlistResponse toResponse(WishlistItem item) {
        Product p = item.getProduct();

        return new WishlistResponse(
                item.getId(),
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getDescription(),
                p.getCategory().getId()
        );
    }
}
