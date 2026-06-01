package com.swirl.ecomengine.product.tag;

import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTagMapperTest {

    private final ProductTagMapper mapper = new ProductTagMapper();

    // ---------------------------------------------------------
    // MAP REQUEST → ENTITY
    // ---------------------------------------------------------
    @Test
    void toEntity_shouldMapAllFields() {
        ProductTagRequest request = TestDataFactory.productTagRequest(
                TagType.PROMOTION,
                "Buy 3 pay for 2",
                10.0,
                20.0,
                3,
                2
        );

        ProductTag entity = mapper.toEntity(request);

        assertThat(entity.getType()).isEqualTo(TagType.PROMOTION);
        assertThat(entity.getLabel()).isEqualTo("Buy 3 pay for 2");
        assertThat(entity.getPromoBuyQuantity()).isEqualTo(3);
        assertThat(entity.getPromoPayQuantity()).isEqualTo(2);
        assertThat(entity.getDiscountAmount()).isEqualTo(10.0);
        assertThat(entity.getDiscountPercent()).isEqualTo(20.0);
    }

    // ---------------------------------------------------------
    // MAP ENTITY → RESPONSE
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldMapAllFields() {
        ProductTag tag = TestDataFactory.productTag(
                null,
                10L,
                TagType.SALE,
                "Extra price"
        );
        tag.setDiscountAmount(50.0);
        tag.setDiscountPercent(10.0);

        ProductTagResponse dto = mapper.toResponse(tag);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getType()).isEqualTo(TagType.SALE);
        assertThat(dto.getLabel()).isEqualTo("Extra price");
        assertThat(dto.getDiscountAmount()).isEqualTo(50.0);
        assertThat(dto.getDiscountPercent()).isEqualTo(10.0);
        assertThat(dto.getPromoBuyQuantity()).isNull();
        assertThat(dto.getPromoPayQuantity()).isNull();
    }

    // ---------------------------------------------------------
    // MAP ENTITY WITH ONLY TYPE
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldHandleMinimalEntity() {
        ProductTag tag = TestDataFactory.productTag(
                null,
                5L,
                TagType.NEW,
                null
        );

        ProductTagResponse dto = mapper.toResponse(tag);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getType()).isEqualTo(TagType.NEW);
        assertThat(dto.getLabel()).isNull();
        assertThat(dto.getDiscountAmount()).isNull();
        assertThat(dto.getDiscountPercent()).isNull();
        assertThat(dto.getPromoBuyQuantity()).isNull();
        assertThat(dto.getPromoPayQuantity()).isNull();
    }
}
