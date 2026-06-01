package com.swirl.ecomengine.product.tag;

import com.swirl.ecomengine.product.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Defines the type of tag (SALE, NEW, PROMOTION, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    // Optional human-readable label for UI
    private String label;

    // Fixed discount amount (e.g., 50 SEK off)
    @Positive
    private Double discountAmount;

    // Percentage-based discount (e.g., 20% off)
    @Positive
    private Double discountPercent;

    // Promotion: number of items required to trigger the offer (e.g., 3)
    @Min(1)
    private Integer promoBuyQuantity;

    // Promotion: number of items the customer pays for (e.g., 2)
    @Min(1)
    private Integer promoPayQuantity;

    // Optional validity period
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    // Many tags can belong to one product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
