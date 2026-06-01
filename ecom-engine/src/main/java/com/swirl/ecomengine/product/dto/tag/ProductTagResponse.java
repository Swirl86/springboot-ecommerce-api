package com.swirl.ecomengine.product.dto.tag;

import com.swirl.ecomengine.product.tag.TagType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductTagResponse {

    private Long id;
    private TagType type;
    private String label;

    private Double discountAmount;
    private Double discountPercent;

    private Integer promoBuyQuantity;
    private Integer promoPayQuantity;
}
