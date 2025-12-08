package com.proj.webprojrct.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VnpayDTO {
    private Long orderId;
    private String paymentUrl;
    private String txnId;
}
