package com.proj.webprojrct.user.repository;

import java.util.Optional;

import com.proj.webprojrct.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    Optional<User> findUserByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE User u SET u.refreshToken = NULL WHERE u.email = :email")
    void clearRefreshToken(@Param("email") String email);
}
