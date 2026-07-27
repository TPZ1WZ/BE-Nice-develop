package com.proj.webprojrct.admin.service;

import com.proj.webprojrct.admin.dto.StoreSettingsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreSettingsService {

    private static final String SETTINGS_FILE = "store_settings.properties";
    
    public StoreSettingsDTO getSettings() {
        Properties props = loadProperties();
        
        return StoreSettingsDTO.builder()
                .storeName(props.getProperty("store.name", "Nike Store"))
                .storePhone(props.getProperty("store.phone", "0123 456 789"))
                .storeAddress(props.getProperty("store.address", "123 Nguyễn Văn Linh, TP.HCM"))
                .notifNewOrders(Boolean.parseBoolean(props.getProperty("notif.new_orders", "true")))
                .notifOutOfStock(Boolean.parseBoolean(props.getProperty("notif.out_of_stock", "true")))
                .notifSystem(Boolean.parseBoolean(props.getProperty("notif.system", "true")))
                .build();
    }
    
    public StoreSettingsDTO updateSettings(StoreSettingsDTO settingsDTO) {
        Properties props = new Properties();
        
        props.setProperty("store.name", settingsDTO.getStoreName());
        props.setProperty("store.phone", settingsDTO.getStorePhone());
        props.setProperty("store.address", settingsDTO.getStoreAddress());
        props.setProperty("notif.new_orders", String.valueOf(settingsDTO.getNotifNewOrders()));
        props.setProperty("notif.out_of_stock", String.valueOf(settingsDTO.getNotifOutOfStock()));
        props.setProperty("notif.system", String.valueOf(settingsDTO.getNotifSystem()));
        
        saveProperties(props);
        
        log.info("Settings saved: {} - {}", settingsDTO.getStoreName(), settingsDTO.getStorePhone());
        return settingsDTO;
    }
    
    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            props.load(input);
        } catch (IOException e) {
            log.warn("Could not load settings file, using defaults");
        }
        return props;
    }
    
    private void saveProperties(Properties props) {
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            props.store(output, "Store Settings");
        } catch (IOException e) {
            log.error("Failed to save settings", e);
        }
    }
}
