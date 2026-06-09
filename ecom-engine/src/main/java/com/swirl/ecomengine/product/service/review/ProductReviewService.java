package com.swirl.ecomengine.product.service.review;

import com.swirl.ecomengine.common.exception.ForbiddenException;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.dto.review.ProductReviewRequest;
import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.product.exception.ReviewNotAllowedException;
import com.swirl.ecomengine.product.exception.ReviewNotFoundException;
import com.swirl.ecomengine.product.review.ProductReview;
import com.swirl.ecomengine.product.review.ProductReviewMapper;
import com.swirl.ecomengine.product.review.ProductReviewRepository;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductReviewService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductReviewRepository reviewRepository;
    private final OrderRepository orderRepository; // Used to verify that the user has purchased the product
    private final ProductReviewMapper mapper;

    // ---------------------------------------------------------
    // CREATE REVIEW (user must be authenticated + must have purchased)
    // ---------------------------------------------------------
    /**
     * Creates a new review for a product.
     * <pre>
     * Business rules:
     * - The user must be authenticated
     * - The user must have purchased the product
     * - The user may only review a product once
     */
    public ProductReviewResponse addReview(
            Long productId,
            User user,
            ProductReviewRequest request
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        Long userId = user.getId();

        if (!orderRepository.existsByUserIdAndStatusInAndItemsProductId(
                userId, OrderStatus.VALID_STATE_FOR_REVIEW, productId
        )) {
            throw new ReviewNotAllowedException("User must purchase product before reviewing");
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new ReviewNotAllowedException("User has already reviewed this product");
        }

        ProductReview review = new ProductReview();
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setProduct(product);
        review.setUser(user);

        return mapper.toResponse(reviewRepository.save(review));
    }

    // ---------------------------------------------------------
    // GET ALL REVIEWS FOR A PRODUCT
    // ---------------------------------------------------------
    /**
     * Returns all reviews for a product, sorted by newest first.
     */
    public List<ProductReviewResponse> getReviewsForProduct(Long productId) {
        return reviewRepository.findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET AVERAGE RATING FOR A PRODUCT
    // ---------------------------------------------------------
    /**
     * Returns the average rating for a product.
     * If no reviews exist, returns 0.0.
     */
    public double getAverageRating(Long productId) {
        Double avg = reviewRepository.findAverageRating(productId);
        return avg != null ? avg : 0.0;
    }

    // ---------------------------------------------------------
    // UPDATE RATING FOR A PRODUCT
    // ---------------------------------------------------------
    public ProductReviewResponse updateReview(Long productId, Long reviewId, User user, ProductReviewRequest request) {

        ProductReview review = reviewRepository.findByIdAndProductId(reviewId, productId)
                .orElseThrow(ReviewNotFoundException::new);

        if (!review.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own review");
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        return mapper.toResponse(reviewRepository.save(review));
    }
}
