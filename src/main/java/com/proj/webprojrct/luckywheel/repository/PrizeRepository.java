package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<Prize, Long> {
    
    List<Prize> findByIsActiveTrue();
    
    @Query("SELECT p FROM Prize p WHERE p.isActive = true AND (p.quantity IS NULL OR p.remainingQuantity > 0)")
    List<Prize> findAvailablePrizes();
    
    @Query("SELECT p FROM Prize p WHERE p.isActive = true AND p.type != 'NOTHING' " +
           "AND (p.quantity IS NULL OR p.remainingQuantity > 0) " +
           "ORDER BY p.discountValue DESC, p.remainingQuantity DESC")
    List<Prize> findAttractivePrizes();
}
