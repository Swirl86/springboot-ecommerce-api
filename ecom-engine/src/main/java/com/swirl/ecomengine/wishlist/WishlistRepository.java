package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUser(User user);

    boolean existsByUserAndProduct(User user, Product product);

    int deleteByUserAndProduct(User user, Product product);

    void deleteAllByUser(User user);
}
