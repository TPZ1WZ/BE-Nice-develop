package com.proj.webprojrct.cart.repository;

import com.proj.webprojrct.cart.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
	@EntityGraph(attributePaths = {"items", "items.product"})
	@Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
	Optional<Cart> findByUserId(@Param("userId") Long userId);
}
