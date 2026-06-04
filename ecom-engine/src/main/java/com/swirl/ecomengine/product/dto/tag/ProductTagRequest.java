package com.swirl.ecomengine.product.dto.tag;

import com.swirl.ecomengine.product.tag.TagType;
import com.swirl.ecomengine.product.tag.validation.ValidProductTag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidProductTag
public class ProductTagRequest {

    @NotNull(message = "Tag type is required")
    private TagType type;

    private String label;

    @Positive(message = "discountAmount must be positive")
    private Double discountAmount;

    @Positive(message = "discountPercent must be positive")
    private Double discountPercent;

    @Min(value = 1, message = "promoBuyQuantity must be at least 1")
    private Integer promoBuyQuantity;

    @Min(value = 1, message = "promoPayQuantity must be at least 1")
    private Integer promoPayQuantity;
}
