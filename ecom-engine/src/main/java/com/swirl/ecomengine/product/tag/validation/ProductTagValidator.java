package com.swirl.ecomengine.product.tag.validation;

import com.swirl.ecomengine.product.tag.TagType;
import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductTagValidator implements ConstraintValidator<ValidProductTag, ProductTagRequest> {

    @Override
    public boolean isValid(ProductTagRequest req, ConstraintValidatorContext ctx) {

        if (req == null) return true;

        boolean valid = true;

        // Helper to add custom messages
        ctx.disableDefaultConstraintViolation();

        // ---------------------------------------------------------
        // RULE 1: type is required
        // ---------------------------------------------------------
        if (req.getType() == null) {
            ctx.buildConstraintViolationWithTemplate("Tag type is required")
                    .addPropertyNode("type")
                    .addConstraintViolation();
            valid = false;
        }

        // ---------------------------------------------------------
        // RULE 2: discountAmount and discountPercent cannot both be set
        // ---------------------------------------------------------
        if (req.getDiscountAmount() != null && req.getDiscountPercent() != null) {
            ctx.buildConstraintViolationWithTemplate("Cannot set both discountAmount and discountPercent")
                    .addConstraintViolation();
            valid = false;
        }

        // ---------------------------------------------------------
        // RULE 3: PROMOTION requires promoBuyQuantity and promoPayQuantity
        // ---------------------------------------------------------
        if (req.getType() == TagType.PROMOTION) {
            if (req.getPromoBuyQuantity() == null || req.getPromoPayQuantity() == null) {
                ctx.buildConstraintViolationWithTemplate(
                        "Promotion tags require promoBuyQuantity and promoPayQuantity"
                ).addConstraintViolation();
                valid = false;
            } else if (req.getPromoPayQuantity() > req.getPromoBuyQuantity()) {
                ctx.buildConstraintViolationWithTemplate(
                        "promoPayQuantity cannot be greater than promoBuyQuantity"
                ).addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
