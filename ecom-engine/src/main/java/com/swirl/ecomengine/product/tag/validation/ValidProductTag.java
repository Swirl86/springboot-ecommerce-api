package com.swirl.ecomengine.product.tag.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductTagValidator.class)
public @interface ValidProductTag {

    String message() default "Invalid product tag configuration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
