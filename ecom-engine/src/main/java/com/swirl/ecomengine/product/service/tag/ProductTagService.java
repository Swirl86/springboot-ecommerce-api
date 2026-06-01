package com.swirl.ecomengine.product.service.tag;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.tag.ProductTag;
import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.product.exception.TagNotFoundException;
import com.swirl.ecomengine.product.exception.TagDoesNotBelongToProductException;
import com.swirl.ecomengine.product.tag.ProductTagMapper;
import com.swirl.ecomengine.product.tag.ProductTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductTagService {

    private final ProductRepository productRepository;
    private final ProductTagRepository tagRepository;
    private final ProductTagMapper mapper;

    public ProductTagResponse addTagToProduct(Long productId, ProductTagRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductTag tag = mapper.toEntity(request);
        tag.setProduct(product);

        product.getTags().add(tag);
        tagRepository.save(tag);

        return mapper.toResponse(tag);
    }

    public void removeTagFromProduct(Long productId, Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        ProductTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));

        if (!tag.getProduct().getId().equals(productId)) {
            throw new TagDoesNotBelongToProductException(productId, tagId);
        }

        product.getTags().remove(tag);
        tagRepository.delete(tag);
    }
}
