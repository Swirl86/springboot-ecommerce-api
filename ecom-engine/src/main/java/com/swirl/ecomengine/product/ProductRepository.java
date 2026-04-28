package com.swirl.ecomengine.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT MAX(p.updatedAt) FROM Product p")
    LocalDateTime findLastUpdated();

    @Query("""
    SELECT MAX(p.updatedAt)
    FROM Product p
    WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
""")
    LocalDateTime findLastUpdatedFiltered(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("q") String q
    );
}

