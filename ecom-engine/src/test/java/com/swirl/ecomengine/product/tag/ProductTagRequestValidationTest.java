package com.swirl.ecomengine.product.tag;

import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTagRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------------------------------------------------------
    // TYPE REQUIRED
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenTypeIsNull() {
        ProductTagRequest req = new ProductTagRequest();
        req.setLabel("Test");

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("Tag type is required"));
    }

    // ---------------------------------------------------------
    // DISCOUNT: amount and percent cannot both be set
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenBothDiscountAmountAndPercentAreSet() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.SALE);
        req.setDiscountAmount(50.0);
        req.setDiscountPercent(10.0);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("Cannot set both discountAmount and discountPercent"));
    }

    // ---------------------------------------------------------
    // PROMOTION: buy/pay required
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenPromotionMissingBuyOrPay() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.PROMOTION);
        req.setPromoBuyQuantity(3);
        req.setPromoPayQuantity(null);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("Promotion tags require promoBuyQuantity and promoPayQuantity"));
    }

    // ---------------------------------------------------------
    // PROMOTION: pay cannot exceed buy
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenPromoPayGreaterThanPromoBuy() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.PROMOTION);
        req.setPromoBuyQuantity(2);
        req.setPromoPayQuantity(3);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("promoPayQuantity cannot be greater than promoBuyQuantity"));
    }

    // ---------------------------------------------------------
    // POSITIVE DISCOUNT AMOUNT
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenDiscountAmountIsNegative() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.SALE);
        req.setDiscountAmount(-10.0);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("discountAmount must be positive"));
    }

    // ---------------------------------------------------------
    // POSITIVE DISCOUNT PERCENT
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenDiscountPercentIsNegative() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.SALE);
        req.setDiscountPercent(-5.0);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("discountPercent must be positive"));
    }

    // ---------------------------------------------------------
    // PROMOTION: buy/pay must be >= 1
    // ---------------------------------------------------------
    @Test
    void shouldFail_whenPromoBuyQuantityIsZero() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.PROMOTION);
        req.setPromoBuyQuantity(0);
        req.setPromoPayQuantity(1);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations)
                .anyMatch(v -> v.getMessage().contains("promoBuyQuantity must be at least 1"));
    }

    // ---------------------------------------------------------
    // VALID REQUEST
    // ---------------------------------------------------------
    @Test
    void shouldPass_whenValidPromotionTag() {
        ProductTagRequest req = new ProductTagRequest();
        req.setType(TagType.PROMOTION);
        req.setLabel("Buy 3 pay for 2");
        req.setPromoBuyQuantity(3);
        req.setPromoPayQuantity(2);

        Set<ConstraintViolation<ProductTagRequest>> violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }
}
