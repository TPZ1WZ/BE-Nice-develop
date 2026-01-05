package com.proj.webprojrct.user.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proj.webprojrct.common.entity.BaseEntity;
// import com.proj.webprojrct.document.entity.Document;
// import com.proj.webprojrct.event.entity.Event;
// import com.proj.webprojrct.project.entity.Project;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 255)
    private String phone; // ✅ ADD: Phone number field (matches DB schema)

    @Column(length = 500)
    private String address; // ✅ ADD: Address field

    @Column(name = "password_hash", nullable = false, length = 255)
    @JsonIgnore
    private String passwordHash;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Sử dụng STRING thay vì ORDINAL mặc định
    private UserRole role;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // --- MODERATION FIELDS ---
    @Builder.Default
    @Column(name = "is_banned", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBanned = false;

    @Column(name = "ban_reason", columnDefinition = "TEXT")
    private String banReason;

    @Column(name = "ban_until")
    private LocalDateTime banUntil;
    // -------------------------

    // --- LOYALTY SYSTEM ---
    @Builder.Default
    @Column(name = "loyalty_points", nullable = false)
    private Integer loyaltyPoints = 0; // Nike Coin tích lũy
    // ----------------------

    @Column(length = 1000)
    private String refreshToken;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    // Bạn cần override thêm các phương thức UserDetails:
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role.name());
    }

    @Override
    @JsonIgnore
    public String getPassword() {

        return this.passwordHash;
    }

}
