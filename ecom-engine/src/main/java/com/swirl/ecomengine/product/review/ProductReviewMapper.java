package com.swirl.ecomengine.product.review;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.dto.review.ProductReviewRequest;
import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.user.User;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewMapper {

    public ProductReviewResponse toResponse(ProductReview review) {
        return new ProductReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getName(),
                review.getCreatedAt(),
                review.getLastEditedAt()
        );
    }

    public ProductReview toEntity(ProductReviewRequest req, Product product, User user) {
        ProductReview r = new ProductReview();
        r.setRating(req.rating());
        r.setComment(req.comment());
        r.setProduct(product);
        r.setUser(user);
        return r;
    }
}
