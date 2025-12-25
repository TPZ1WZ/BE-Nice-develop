package com.proj.webprojrct.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSettingsDTO {
    
    @JsonProperty("store_name")
    private String storeName;
    
    @JsonProperty("store_phone")
    private String storePhone;
    
    @JsonProperty("store_address")
    private String storeAddress;
    
    @JsonProperty("notif_new_orders")
    private Boolean notifNewOrders;
    
    @JsonProperty("notif_out_of_stock")
    private Boolean notifOutOfStock;
    
    @JsonProperty("notif_system")
    private Boolean notifSystem;
}
