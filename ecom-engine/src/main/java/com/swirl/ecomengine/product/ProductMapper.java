package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getDescription(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.getImageUrls()
        );
    }

    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setImageUrls(
                request.imageUrls() != null ? request.imageUrls() : Collections.emptyList()
        );
        return product;
    }
}