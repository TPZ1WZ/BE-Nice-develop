package com.proj.webprojrct;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test simulation cho authentication flow
 * Chạy file này để debug "Failed to fetch" và các lỗi khác
 * 
 * USAGE:
 * 1. Đảm bảo Spring Boot app đang chạy (mvn spring-boot:run)
 * 2. Run class này: mvn test -Dtest=AuthenticationFlowTest
 * 3. Hoặc run trực tiếp main method trong IDE
 */
@Slf4j
public class AuthenticationFlowTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/auth/register";
    
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        log.info("========================================");
        log.info("🧪 AUTHENTICATION FLOW TEST");
        log.info("Base URL: {}", BASE_URL);
        log.info("Time: {}", LocalDateTime.now());
        log.info("========================================\n");

        // Test 1: Connection check
        testConnectionCheck();
        
        // Test 2: Login with valid credentials
        testLoginSuccess();
        
        // Test 3: Login with invalid credentials
        testLoginBadCredentials();
        
        // Test 4: Login with non-existent user
        testLoginUserNotFound();
        
        // Test 5: CORS check
        testCorsRequest();

        log.info("\n========================================");
        log.info("✅ ALL TESTS COMPLETED");
        log.info("========================================");
    }

    /**
     * Test 1: Kiểm tra connection đến server
     */
    private static void testConnectionCheck() {
        log.info("\n🔍 TEST 1: CONNECTION CHECK");
        log.info("----------------------------------------");
        
        try {
            String url = BASE_URL + "/actuator/health";
            log.info("Sending GET request to: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            log.info("✅ Connection successful!");
            log.info("Status: {}", response.getStatusCode());
            log.info("Body: {}", response.getBody());
            
        } catch (ResourceAccessException e) {
            log.error("❌ CONNECTION REFUSED - Server is not running!");
            log.error("Error: {}", e.getMessage());
            log.error("SOLUTION: Start Spring Boot app with 'mvn spring-boot:run'");
            System.exit(1);
            
        } catch (Exception e) {
            log.warn("⚠️ Health endpoint not available (trying root endpoint...)");
            
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/", String.class);
                log.info("✅ Connection successful via root endpoint!");
                log.info("Status: {}", response.getStatusCode());
                
            } catch (Exception ex) {
                log.error("❌ Cannot connect to server: {}", ex.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Test 2: Login thành công với credentials hợp lệ
     */
    private static void testLoginSuccess() {
        log.info("\n🔐 TEST 2: LOGIN SUCCESS (Valid Credentials)");
        log.info("----------------------------------------");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin@example.com");
        loginRequest.put("password", "password");
        
        String url = BASE_URL + LOGIN_ENDPOINT;
        
        try {
            log.info("📤 Sending POST request to: {}", url);
            log.info("Request Body: {}", objectMapper.writeValueAsString(loginRequest));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
            
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("✅ LOGIN SUCCESSFUL!");
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Time: {}ms", duration);
            log.info("Response Headers: {}", response.getHeaders());
            log.info("Response Body: {}", formatJson(response.getBody()));
            
            // Extract và validate token
            if (response.getBody() != null && response.getBody().contains("access_token")) {
                log.info("🎫 Access Token found in response");
                
                // Parse JSON để lấy token
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                String accessToken = (String) responseMap.get("access_token");
                String refreshToken = (String) responseMap.get("refresh_token");
                
                log.info("Access Token: {}...", accessToken.substring(0, Math.min(30, accessToken.length())));
                log.info("Refresh Token: {}...", refreshToken.substring(0, Math.min(30, refreshToken.length())));
                
                // Test authenticated request
                testAuthenticatedRequest(accessToken);
            }
            
        } catch (HttpClientErrorException e) {
            log.error("❌ LOGIN FAILED - HTTP Error");
            log.error("Status Code: {}", e.getStatusCode());
            log.error("Response Body: {}", e.getResponseBodyAsString());
            log.error("Error Message: {}", e.getMessage());
            
        } catch (ResourceAccessException e) {
            log.error("❌ FAILED TO FETCH - Connection Error");
            log.error("Error: {}", e.getMessage());
            log.error("Possible causes:");
            log.error("1. Server not running (start with 'mvn spring-boot:run')");
            log.error("2. Wrong port (check application.properties: server.port)");
            log.error("3. Firewall blocking connection");
            
        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR", e);
        }
    }

    /**
     * Test 3: Login với sai mật khẩu (Bad Credentials)
     */
    private static void testLoginBadCredentials() {
        log.info("\n🔐 TEST 3: LOGIN FAILURE (Bad Credentials)");
        log.info("----------------------------------------");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin@example.com");
        loginRequest.put("password", "wrongpassword");
        
        String url = BASE_URL + LOGIN_ENDPOINT;
        
        try {
            log.info("📤 Sending POST request with WRONG password: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.warn("⚠️ Expected 401 but got: {}", response.getStatusCode());
            log.info("Response Body: {}", response.getBody());
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("✅ Expected 401 Unauthorized received");
                log.info("Status Code: {}", e.getStatusCode());
                log.info("Response: {}", e.getResponseBodyAsString());
            } else {
                log.error("❌ Unexpected status code: {}", e.getStatusCode());
                log.error("Response: {}", e.getResponseBodyAsString());
            }
            
        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR", e);
        }
    }

    /**
     * Test 4: Login với user không tồn tại
     */
    private static void testLoginUserNotFound() {
        log.info("\n🔐 TEST 4: LOGIN FAILURE (User Not Found)");
        log.info("----------------------------------------");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent@example.com");
        loginRequest.put("password", "password");
        
        String url = BASE_URL + LOGIN_ENDPOINT;
        
        try {
            log.info("📤 Sending POST request with NON-EXISTENT user: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.warn("⚠️ Expected 401 but got: {}", response.getStatusCode());
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.info("✅ Expected 401 Unauthorized received");
                log.info("Response: {}", e.getResponseBodyAsString());
            } else {
                log.error("❌ Unexpected status code: {}", e.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR", e);
        }
    }

    /**
     * Test 5: CORS request simulation
     */
    private static void testCorsRequest() {
        log.info("\n🌐 TEST 5: CORS REQUEST");
        log.info("----------------------------------------");
        
        String url = BASE_URL + LOGIN_ENDPOINT;
        
        try {
            log.info("📤 Sending OPTIONS request (CORS preflight): {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Origin", "http://localhost:3000");
            headers.set("Access-Control-Request-Method", "POST");
            headers.set("Access-Control-Request-Headers", "Content-Type");
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.OPTIONS, request, String.class
            );
            
            log.info("✅ CORS preflight successful!");
            log.info("Status: {}", response.getStatusCode());
            log.info("Access-Control-Allow-Origin: {}", 
                     response.getHeaders().getFirst("Access-Control-Allow-Origin"));
            log.info("Access-Control-Allow-Methods: {}", 
                     response.getHeaders().getFirst("Access-Control-Allow-Methods"));
            
        } catch (Exception e) {
            log.error("❌ CORS request failed: {}", e.getMessage());
            log.error("SOLUTION: Check CORS configuration in SecurityConfiguration");
        }
    }

    /**
     * Test authenticated request với token
     */
    private static void testAuthenticatedRequest(String accessToken) {
        log.info("\n🔑 BONUS TEST: AUTHENTICATED REQUEST");
        log.info("----------------------------------------");
        
        String url = BASE_URL + "/api/v1/users/me"; // Example protected endpoint
        
        try {
            log.info("📤 Sending GET request with Bearer token: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, request, String.class
            );
            
            log.info("✅ Authenticated request successful!");
            log.info("Status: {}", response.getStatusCode());
            log.info("Response: {}", formatJson(response.getBody()));
            
        } catch (HttpClientErrorException e) {
            log.warn("⚠️ Endpoint may not exist or requires different permissions");
            log.info("Status: {} | Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            
        } catch (Exception e) {
            log.error("❌ Authenticated request failed: {}", e.getMessage());
        }
    }

    /**
     * Format JSON cho readable output
     */
    private static String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }
}
