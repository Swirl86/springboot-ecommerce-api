package com.swirl.ecomengine.product.tag;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.product.exception.TagDoesNotBelongToProductException;
import com.swirl.ecomengine.product.exception.TagNotFoundException;
import com.swirl.ecomengine.product.service.tag.ProductTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductTagServiceTest {

    private ProductRepository productRepository;
    private ProductTagRepository tagRepository;
    private ProductTagMapper mapper;
    private ProductTagService service;

    private Product product;
    private ProductTag tag;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        tagRepository = mock(ProductTagRepository.class);
        mapper = mock(ProductTagMapper.class);

        service = new ProductTagService(productRepository, tagRepository, mapper);

        product = new Product();
        product.setId(1L);

        tag = TestDataFactory.productTag(product, 10L, TagType.SALE, "Extra price");
    }

    // ------------------------------------------------------------
    // addTagToProduct — adds tag and returns response
    // ------------------------------------------------------------
    @Test
    void addTagToProduct_addsTagSuccessfully() {
        // Arrange
        ProductTagRequest request = TestDataFactory.productTagRequest(TagType.SALE, "Extra price", null, null, null, null);
        ProductTagResponse response = TestDataFactory.productTagResponse(10L, TagType.SALE, "Extra price");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mapper.toEntity(request)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.toResponse(tag)).thenReturn(response);

        // Act
        ProductTagResponse result = service.addTagToProduct(1L, request);

        // Assert
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(TagType.SALE);
        assertThat(product.getTags()).hasSize(1);

        verify(tagRepository).save(tag);
    }

    // ------------------------------------------------------------
    // addTagToProduct — throws when product not found
    // ------------------------------------------------------------
    @Test
    void addTagToProduct_throwsWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductTagRequest request = TestDataFactory.productTagRequest(TagType.SALE);

        assertThatThrownBy(() ->
                service.addTagToProduct(1L, request)
        ).isInstanceOf(ProductNotFoundException.class);
    }

    // ------------------------------------------------------------
    // removeTagFromProduct — removes tag successfully
    // ------------------------------------------------------------
    @Test
    void removeTagFromProduct_removesTagSuccessfully() {
        // Arrange
        product.getTags().add(tag);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(10L)).thenReturn(Optional.of(tag));

        // Act
        service.removeTagFromProduct(1L, 10L);

        // Assert
        assertThat(product.getTags()).isEmpty();
        verify(tagRepository).delete(tag);
    }

    // ------------------------------------------------------------
    // removeTagFromProduct — throws when tag not found
    // ------------------------------------------------------------
    @Test
    void removeTagFromProduct_throwsWhenTagNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.removeTagFromProduct(1L, 10L)
        ).isInstanceOf(TagNotFoundException.class);
    }

    // ------------------------------------------------------------
    // removeTagFromProduct — throws when tag belongs to another product
    // ------------------------------------------------------------
    @Test
    void removeTagFromProduct_throwsWhenTagDoesNotBelongToProduct() {
        Product other = new Product();
        other.setId(2L);

        tag.setProduct(other);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(10L)).thenReturn(Optional.of(tag));

        assertThatThrownBy(() ->
                service.removeTagFromProduct(1L, 10L)
        ).isInstanceOf(TagDoesNotBelongToProductException.class);
    }
}
