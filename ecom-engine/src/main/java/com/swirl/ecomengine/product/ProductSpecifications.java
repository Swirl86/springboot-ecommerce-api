package com.swirl.ecomengine.product;

import org.springframework.data.jpa.domain.Specification;

/**
 * <pre>
 * Utility class with reusable JPA Specifications for filtering Product entities.
 *
 * Each method returns a single filter condition (or null if unused), allowing
 * ProductService to dynamically combine filters for search, sorting and pagination.
 *
 * Filters supported:
 * - Category ID
 * - Min/Max price
 * - Text search on product name
 * </pre>
 */
public class ProductSpecifications {

    public static Specification<Product> withCategory(Long categoryId) {
        if (categoryId == null) return null;

        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> withMinPrice(Double minPrice) {
        if (minPrice == null) return null;

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> withMaxPrice(Double maxPrice) {
        if (maxPrice == null) return null;

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> withSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) return null;

        String pattern = "%" + searchTerm.toLowerCase() + "%";

        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), pattern);
    }
}