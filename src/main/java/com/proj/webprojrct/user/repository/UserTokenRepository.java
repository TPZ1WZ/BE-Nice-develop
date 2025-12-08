package com.proj.webprojrct.user.repository;

import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.entity.UserToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long>, JpaSpecificationExecutor<UserToken> {

    @Query("SELECT c FROM UserToken c WHERE c.token = :token AND c.user.email = :email")
    Optional<UserToken> findByTokenAndUserEmail(@Param("token") long token, @Param("email") String email);

    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE user_token
            SET confirmed_at = ?2
            WHERE token = ?1
              AND user_id = (SELECT u.id FROM users u WHERE u.email = ?3)
            """,
            nativeQuery = true
    )
    void updateConfirmedAt(long token, LocalDateTime confirmedAt, String email);

    Optional<UserToken> findUserTokenByToken(Long token);

    void deleteUserTokenByToken(Long token);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserToken c WHERE c.expiresAt < :now")
    void deleteAllExpiredSince(@Param("now") LocalDateTime now);
}
