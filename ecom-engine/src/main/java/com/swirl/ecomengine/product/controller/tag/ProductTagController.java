package com.swirl.ecomengine.product.controller.tag;

import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import com.swirl.ecomengine.product.service.tag.ProductTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/tags")
@RequiredArgsConstructor
public class ProductTagController {

    private final ProductTagService tagService;

    @PostMapping
    public ProductTagResponse addTag(
            @PathVariable Long productId,
            @Valid @RequestBody ProductTagRequest request
    ) {
        return tagService.addTagToProduct(productId, request);
    }

    @DeleteMapping("/{tagId}")
    public void removeTag(
            @PathVariable Long productId,
            @PathVariable Long tagId
    ) {
        tagService.removeTagFromProduct(productId, tagId);
    }
}
