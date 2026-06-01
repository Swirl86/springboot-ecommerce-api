package com.swirl.ecomengine.product.tag;

import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductTagMapper {

    // Converts request DTO to entity
    public ProductTag toEntity(ProductTagRequest request) {
        ProductTag tag = new ProductTag();
        tag.setType(request.getType());
        tag.setLabel(request.getLabel());
        tag.setDiscountAmount(request.getDiscountAmount());
        tag.setDiscountPercent(request.getDiscountPercent());
        tag.setPromoBuyQuantity(request.getPromoBuyQuantity());
        tag.setPromoPayQuantity(request.getPromoPayQuantity());
        return tag;
    }

    // Converts entity to response DTO
    public ProductTagResponse toResponse(ProductTag tag) {
        ProductTagResponse response = new ProductTagResponse();
        response.setId(tag.getId());
        response.setType(tag.getType());
        response.setLabel(tag.getLabel());
        response.setDiscountAmount(tag.getDiscountAmount());
        response.setDiscountPercent(tag.getDiscountPercent());
        response.setPromoBuyQuantity(tag.getPromoBuyQuantity());
        response.setPromoPayQuantity(tag.getPromoPayQuantity());
        return response;
    }
}
