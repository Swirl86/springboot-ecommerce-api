package com.swirl.ecomengine.product.review;

import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.NotFoundException;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.dto.review.ProductReviewRequest;
import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.product.service.review.ProductReviewService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductReviewServiceTest {

    private ProductRepository productRepository;
    private ProductReviewRepository reviewRepository;
    private OrderRepository orderRepository;
    private ProductReviewMapper mapper;

    private ProductReviewService service;

    private Product product;
    private User testUser;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        reviewRepository = mock(ProductReviewRepository.class);
        orderRepository = mock(OrderRepository.class);
        mapper = mock(ProductReviewMapper.class);

        service = new ProductReviewService(
                productRepository,
                userRepository,
                reviewRepository,
                orderRepository,
                mapper
        );

        product = TestDataFactory.defaultProduct(TestDataFactory.defaultCategory());
        product.setId(1L);

        testUser = TestDataFactory.user(pwd -> "encoded");
        testUser.setId(5L);
    }

    // ------------------------------------------------------------
    // addReview — adds review successfully
    // ------------------------------------------------------------
    @Test
    void addReview_addsReviewSuccessfully() {
        ProductReviewRequest request = TestDataFactory.productReviewRequest(5, "Great!");

        TestDataFactory.completedOrder(testUser, product, 1);

        ProductReview saved = new ProductReview();
        saved.setId(10L);
        saved.setRating(5);
        saved.setComment("Great!");
        saved.setProduct(product);
        saved.setUser(testUser);

        ProductReviewResponse response =
                TestDataFactory.newProductReviewResponse(10L, 5, "Great!", "user@example.com");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.existsByUserIdAndStatusInAndItemsProductId(
                5L, OrderStatus.VALID_STATE_FOR_REVIEW, 1L
        )).thenReturn(true);

        when(reviewRepository.existsByProductIdAndUserId(1L, 5L)).thenReturn(false);
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        ProductReviewResponse result = service.addReview(1L, testUser, request);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Great!");
    }

    // ------------------------------------------------------------
    // addReview — throws when product not found
    // ------------------------------------------------------------
    @Test
    void addReview_throwsWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductReviewRequest request = TestDataFactory.productReviewRequest(5, "Nice");

        assertThatThrownBy(() ->
                service.addReview(1L, testUser, request)
        ).isInstanceOf(NotFoundException.class);
    }

    // ------------------------------------------------------------
    // addReview — throws when user has not purchased product
    // ------------------------------------------------------------
    @Test
    void addReview_throwsWhenUserHasNotPurchased() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.existsByUserIdAndStatusInAndItemsProductId(
                5L, OrderStatus.VALID_STATE_FOR_REVIEW, 1L
        )).thenReturn(false);

        ProductReviewRequest request = TestDataFactory.productReviewRequest(5, "Nice");

        assertThatThrownBy(() ->
                service.addReview(1L, testUser, request)
        ).isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------
    // addReview — throws when user already reviewed
    // ------------------------------------------------------------
    @Test
    void addReview_throwsWhenUserAlreadyReviewed() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(orderRepository.existsByUserIdAndStatusInAndItemsProductId(
                5L, OrderStatus.VALID_STATE_FOR_REVIEW, 1L
        )).thenReturn(true);

        when(reviewRepository.existsByProductIdAndUserId(1L, 5L)).thenReturn(true);

        ProductReviewRequest request = TestDataFactory.productReviewRequest(5, "Nice");

        assertThatThrownBy(() ->
                service.addReview(1L, testUser, request)
        ).isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------
    // getReviewsForProduct — returns mapped list
    // ------------------------------------------------------------
    @Test
    void getReviewsForProduct_returnsList() {
        ProductReview r1 = TestDataFactory.productReview(
                1L, product, testUser, 5, "Great"
        );

        ProductReview r2 = TestDataFactory.productReview(
                2L, product, testUser, 4, "Good"
        );

        ProductReviewResponse res1 =
                TestDataFactory.newProductReviewResponse(1L, 5, "Great", "user@example.com");

        ProductReviewResponse res2 =
                TestDataFactory.newProductReviewResponse(2L, 4, "Good", "user@example.com");

        when(reviewRepository.findAllByProductIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(r1, r2));

        when(mapper.toResponse(r1)).thenReturn(res1);
        when(mapper.toResponse(r2)).thenReturn(res2);

        List<ProductReviewResponse> result = service.getReviewsForProduct(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rating()).isEqualTo(5);
        assertThat(result.get(1).rating()).isEqualTo(4);
    }

    // ------------------------------------------------------------
    // getAverageRating — returns value or zero
    // ------------------------------------------------------------
    @Test
    void getAverageRating_returnsValue() {
        when(reviewRepository.findAverageRating(1L)).thenReturn(4.5);

        double avg = service.getAverageRating(1L);

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    void getAverageRating_returnsZeroWhenNull() {
        when(reviewRepository.findAverageRating(1L)).thenReturn(null);

        double avg = service.getAverageRating(1L);

        assertThat(avg).isEqualTo(0.0);
    }
}
