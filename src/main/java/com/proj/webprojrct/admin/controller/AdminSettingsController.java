package com.proj.webprojrct.admin.controller;

import com.proj.webprojrct.admin.dto.StoreSettingsDTO;
import com.proj.webprojrct.admin.service.StoreSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final StoreSettingsService settingsService;

    @GetMapping
    public ResponseEntity<StoreSettingsDTO> getSettings() {
        log.info("📋 [SETTINGS] Get store settings request");
        try {
            StoreSettingsDTO settings = settingsService.getSettings();
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            log.error("❌ [SETTINGS] Failed to get settings", e);
            // Return default settings on error
            StoreSettingsDTO defaultSettings = StoreSettingsDTO.builder()
                    .storeName("Nike Store")
                    .storePhone("0123 456 789")
                    .storeAddress("123 Nguyễn Văn Linh, TP.HCM")
                    .notifNewOrders(true)
                    .notifOutOfStock(true)
                    .notifSystem(true)
                    .build();
            return ResponseEntity.ok(defaultSettings);
        }
    }

    @PutMapping
    public ResponseEntity<StoreSettingsDTO> updateSettings(@RequestBody StoreSettingsDTO settingsDTO) {
        log.info("💾 [SETTINGS] Update store settings: {}", settingsDTO.getStoreName());
        try {
            StoreSettingsDTO updated = settingsService.updateSettings(settingsDTO);
            log.info("✅ [SETTINGS] Settings updated successfully");
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("❌ [SETTINGS] Failed to update settings", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
