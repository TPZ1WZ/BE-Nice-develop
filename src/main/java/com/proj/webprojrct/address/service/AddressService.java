package com.proj.webprojrct.address.service;

import com.proj.webprojrct.address.dto.AddressRequest;
import com.proj.webprojrct.address.dto.AddressResponse;
import com.proj.webprojrct.address.entity.Address;
import com.proj.webprojrct.address.repository.AddressRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(String userEmail) {
        System.out.println("DEBUG: Looking for user with email: " + userEmail);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
            
        System.out.println("DEBUG: Found user with id: " + user.getId());
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Address address = addressRepository.findByUserAndIsDefaultTrue(user)
            .orElseThrow(() -> new RuntimeException("No default address found"));
            
        return toResponse(address);
    }
    
    @Transactional
    public AddressResponse createAddress(String userEmail, AddressRequest request) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // If this is set as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetAllDefaults(user);
        }
        
        // If this is the first address, make it default
        List<Address> existingAddresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        boolean isFirstAddress = existingAddresses.isEmpty();
        
        Address address = Address.builder()
            .user(user)
            .recipientName(request.getRecipientName())
            .phoneNumber(request.getPhoneNumber())
            .addressLine(request.getAddressLine())
            .ward(request.getWard())
            .district(request.getDistrict())
            .city(request.getCity())
            .isDefault(isFirstAddress || Boolean.TRUE.equals(request.getIsDefault()))
            .build();
            
        address = addressRepository.save(address);
        return toResponse(address);
    }
    
    @Transactional
    public AddressResponse updateAddress(String userEmail, Long addressId, AddressRequest request) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // If setting as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            unsetAllDefaults(user);
        }
        
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine(request.getAddressLine());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        
        address = addressRepository.save(address);
        return toResponse(address);
    }
    
    @Transactional
    public void deleteAddress(String userEmail, Long addressId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Address not found"));
        
        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);
        
        // If deleted address was default, set another as default
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }
    
    @Transactional
    public AddressResponse setDefaultAddress(String userEmail, Long addressId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Address address = addressRepository.findByIdAndUser(addressId, user)
            .orElseThrow(() -> new RuntimeException("Address not found"));
        
        // Unset all other defaults
        unsetAllDefaults(user);
        
        // Set this as default
        address.setIsDefault(true);
        address = addressRepository.save(address);
        
        return toResponse(address);
    }
    
    private void unsetAllDefaults(User user) {
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        addresses.forEach(addr -> {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        });
    }
    
    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
            .id(address.getId())
            .recipientName(address.getRecipientName())
            .phoneNumber(address.getPhoneNumber())
            .addressLine(address.getAddressLine())
            .ward(address.getWard())
            .district(address.getDistrict())
            .city(address.getCity())
            .isDefault(address.getIsDefault())
            .createdAt(address.getCreatedAt())
            .updatedAt(address.getUpdatedAt())
            .build();
    }
}
