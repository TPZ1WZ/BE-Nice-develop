package com.proj.webprojrct.address.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(name = "address_line", nullable = false)
    private String addressLine;
    
    @Column(name = "ward")
    private String ward;
    
    @Column(name = "district", nullable = false)
    private String district;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
}
