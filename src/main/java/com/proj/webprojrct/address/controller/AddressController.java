package com.proj.webprojrct.address.controller;

import com.proj.webprojrct.address.dto.AddressRequest;
import com.proj.webprojrct.address.dto.AddressResponse;
import com.proj.webprojrct.address.service.AddressService;
import com.proj.webprojrct.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;
    
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses() {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        return ResponseEntity.ok(addressService.getUserAddresses(userEmail));
    }
    
    @GetMapping("/default")
    public ResponseEntity<AddressResponse> getDefaultAddress() {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        try {
            return ResponseEntity.ok(addressService.getDefaultAddress(userEmail));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@RequestBody AddressRequest request) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        return ResponseEntity.ok(addressService.createAddress(userEmail, request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
        @PathVariable Long id,
        @RequestBody AddressRequest request
    ) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        return ResponseEntity.ok(addressService.updateAddress(userEmail, id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        addressService.deleteAddress(userEmail, id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/set-default")
    public ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable Long id) {
        String userEmail = SecurityUtil.getCurrentUserEmail();
        return ResponseEntity.ok(addressService.setDefaultAddress(userEmail, id));
    }
}
