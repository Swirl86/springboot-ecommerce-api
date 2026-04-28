package com.swirl.ecomengine.category;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Category name cannot be empty")
    @Setter
    private String name;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Category() {}

    public Category(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreRemove
    public void updateTimestampBeforeDelete() {
        this.updatedAt = LocalDateTime.now();
    }
}