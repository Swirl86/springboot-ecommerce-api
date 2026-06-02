package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.tag.ProductTag;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be empty")
    @Column(nullable = false)
    private String name;

    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false)
    private double price;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // A product can have zero or many tags.
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductTag> tags = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods to maintain bidirectional relationship
    public void addTag(ProductTag tag) {
        tags.add(tag);
        tag.setProduct(this);
    }

    public void removeTag(ProductTag tag) {
        tags.remove(tag);
        tag.setProduct(null);
    }
}