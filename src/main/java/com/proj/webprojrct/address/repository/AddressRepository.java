package com.proj.webprojrct.address.repository;

import com.proj.webprojrct.address.entity.Address;
import com.proj.webprojrct.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    
    Optional<Address> findByIdAndUser(Long id, User user);
    
    List<Address> findByUserAndIdNot(User user, Long id);
}
