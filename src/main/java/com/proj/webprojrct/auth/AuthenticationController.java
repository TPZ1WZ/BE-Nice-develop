package com.proj.webprojrct.auth;

import com.proj.webprojrct.auth.dto.request.ChangePasswordRequest;
import com.proj.webprojrct.auth.dto.request.ForgotPasswordRequest;
import com.proj.webprojrct.auth.dto.request.LoginDTO;
import com.proj.webprojrct.auth.dto.request.RefreshTokenRequest;
import com.proj.webprojrct.auth.dto.request.RegisterDTO;
import com.proj.webprojrct.auth.dto.request.ResetPasswordRequest;
import com.proj.webprojrct.auth.dto.request.ResetPasswordWithOtpRequest;
import com.proj.webprojrct.auth.dto.request.VerifyOtpDTO;
import com.proj.webprojrct.auth.dto.response.ForgotPasswordResponse;
import com.proj.webprojrct.auth.dto.response.LoginResponseV1;
import com.proj.webprojrct.auth.dto.response.LoginResponseV2;
import com.proj.webprojrct.auth.dto.response.RegisterResponse;
import com.proj.webprojrct.auth.dto.response.VerifyOtpResponse;
import com.proj.webprojrct.auth.model.PendingPasswordReset;
import com.proj.webprojrct.auth.model.PendingRegistration;
import com.proj.webprojrct.common.error.Common;
import com.proj.webprojrct.common.config.ApiMessage;
import com.proj.webprojrct.common.exception.VerificationException;
import com.proj.webprojrct.common.service.TokenCleanUpService;
import com.proj.webprojrct.common.util.CookieUtil;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.repository.UserRepository;
import com.proj.webprojrct.user.service.UserService;
import com.proj.webprojrct.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Authentication Controller với logging chi tiết cho debugging
 * Xử lý Login, Register, Token Refresh, Password Reset, Email Verification
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthenticationController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenicationService authenicationService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final PendingRegistrationService pendingRegistrationService;
    private final PendingPasswordResetService pendingPasswordResetService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenCleanUpService tokenCleanUpService;

    public AuthenticationController(UserService userService, AuthenicationService authenicationService,
            AuthenticationManagerBuilder authenticationManagerBuilder,
            UserRepository userRepository, CookieUtil cookieUtil,
            PendingRegistrationService pendingRegistrationService,
            PendingPasswordResetService pendingPasswordResetService,
            EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.authenicationService = authenicationService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.cookieUtil = cookieUtil;
        this.pendingRegistrationService = pendingRegistrationService;
        this.pendingPasswordResetService = pendingPasswordResetService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("info")
    public Object info(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false, "message", "User not logged in"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("authenticated", true);
        result.put("email", user.getEmail());
        result.put("name", user.getFullName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    @ApiMessage("Register new user")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        log.info("📝 [REGISTER] Registration request - Email: {} | IP: {} | Time: {}",
                registerDTO.getEmail(), clientIp, LocalDateTime.now());

        try {
            log.debug("Validating registration data - Email: {} | HasPassword: {}",
                    registerDTO.getEmail(), registerDTO.getPassword() != null);

            authenicationService.handleRegister(registerDTO);

            log.info("✅ [REGISTER] User registered successfully - Email: {} | Verification email sent",
                    registerDTO.getEmail());

            RegisterResponse response = RegisterResponse.builder()
                    .success(true)
                    .message("User registered successfully. Please check your email to verify your account.")
                    .email(registerDTO.getEmail())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (EntityExistsException e) {
            log.warn("⚠️ [REGISTER] Registration failed - Email already exists: {} | IP: {}",
                    registerDTO.getEmail(), clientIp);

            RegisterResponse response = RegisterResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .email(registerDTO.getEmail())
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            log.error("❌ [REGISTER] Registration failed - Email: {} | Error: {} | IP: {}",
                    registerDTO.getEmail(), e.getMessage(), clientIp, e);

            RegisterResponse response = RegisterResponse.builder()
                    .success(false)
                    .message("Registration failed: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/register-with-otp")
    @ApiMessage("Register with OTP verification")
    public ResponseEntity<RegisterResponse> registerWithOtp(@RequestBody RegisterDTO registerDTO,
            HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        
        // Normalize email: trim and lowercase
        String normalizedEmail = registerDTO.getEmail().trim().toLowerCase();
        log.info("📝 [REGISTER-OTP] Registration request - Email: '{}' | IP: {}", normalizedEmail, clientIp);

        try {
            // Check if email already exists in database
            Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
            if (existingUser.isPresent()) {
                log.warn("⚠️ [REGISTER-OTP] Email already registered: '{}'", normalizedEmail);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Email already exists")
                                .email(normalizedEmail)
                                .build());
            }

            // Check if there's already a pending registration
            Optional<PendingRegistration> existingPending = pendingRegistrationService
                    .findByEmail(normalizedEmail);
            if (existingPending.isPresent()) {
                log.info("📧 [REGISTER-OTP] Resending OTP to existing pending registration: '{}'",
                        normalizedEmail);
            }

            // Generate 6-digit OTP
            long otp = 100000 + new Random().nextInt(900000);
            log.info("🔢 [REGISTER-OTP] Generated OTP for '{}': {}", normalizedEmail, otp);

            // Create pending registration
            PendingRegistration pending = PendingRegistration.builder()
                    .email(normalizedEmail)
                    .fullName(registerDTO.getFullName())
                    .phone(registerDTO.getPhone())
                    .passwordHash(passwordEncoder.encode(registerDTO.getPassword()))
                    .otp(otp)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            pendingRegistrationService.savePending(pending);

            // Send OTP email
            try {
                emailService.sendRegistrationOtp(normalizedEmail, registerDTO.getFullName(), otp);
                log.info("✅ [REGISTER-OTP] OTP email sent to: '{}'", normalizedEmail);
            } catch (Exception emailEx) {
                log.error("❌ [REGISTER-OTP] Failed to send OTP email: {}", emailEx.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Failed to send OTP email")
                                .build());
            }

            return ResponseEntity.ok(RegisterResponse.builder()
                    .success(true)
                    .message("OTP sent to your email. Please verify within 5 minutes.")
                    .email(normalizedEmail)
                    .build());

        } catch (Exception e) {
            log.error("❌ [REGISTER-OTP] Registration failed - Email: '{}' | Error: {}",
                    normalizedEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/verify-registration-otp")
    @ApiMessage("Verify OTP and complete registration")
    public ResponseEntity<VerifyOtpResponse> verifyRegistrationOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        log.info("🔢 [VERIFY-REG-OTP] Verification request - Email: '{}', OTP: {}", 
                verifyOtpDTO.getEmail(), verifyOtpDTO.getOtp());

        try {
            // Trim email to remove whitespace
            String trimmedEmail = verifyOtpDTO.getEmail().trim().toLowerCase();
            log.info("📧 [VERIFY-REG-OTP] Trimmed email: '{}'", trimmedEmail);
            
            // Verify OTP and get pending registration
            Optional<PendingRegistration> pendingOpt = pendingRegistrationService.verifyAndGet(
                    trimmedEmail, verifyOtpDTO.getOtp());

            if (pendingOpt.isEmpty()) {
                log.warn("⚠️ [VERIFY-REG-OTP] Invalid OTP or expired - Email: '{}', OTP provided: {}", 
                        trimmedEmail, verifyOtpDTO.getOtp());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VerifyOtpResponse.builder()
                                .success(false)
                                .message("Invalid OTP or OTP has expired")
                                .email(trimmedEmail)
                                .build());
            }

            PendingRegistration pending = pendingOpt.get();
            
            log.info("✅ [VERIFY-REG-OTP] OTP verified! Creating user - Email: '{}'", pending.getEmail());

            // Create user in database
            User newUser = User.builder()
                    .email(pending.getEmail())
                    .fullName(pending.getFullName())
                    .phone(pending.getPhone())
                    .passwordHash(pending.getPasswordHash())
                    .isActive(true)
                    .role(com.proj.webprojrct.user.entity.UserRole.MEMBER)
                    .build();

            userRepository.save(newUser);
            log.info("✅ [VERIFY-REG-OTP] User created successfully - Email: '{}'", pending.getEmail());

            // Remove pending registration
            pendingRegistrationService.removePending(pending.getEmail());

            return ResponseEntity.ok(VerifyOtpResponse.builder()
                    .success(true)
                    .message("Registration completed successfully. You can now login.")
                    .email(pending.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("❌ [VERIFY-REG-OTP] Verification failed - Email: {} | Error: {}",
                    verifyOtpDTO.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerifyOtpResponse.builder()
                            .success(false)
                            .message("Verification failed: " + e.getMessage())
                            .email(verifyOtpDTO.getEmail())
                            .build());
        }
    }

    @PostMapping("/resend-registration-otp")
    @ApiMessage("Resend OTP for registration")
    public ResponseEntity<RegisterResponse> resendRegistrationOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String normalizedEmail = email.trim().toLowerCase();
        
        log.info("🔄 [RESEND-OTP] Resend request for email: '{}'", normalizedEmail);

        try {
            // Check if email already exists in database
            Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
            if (existingUser.isPresent()) {
                log.warn("⚠️ [RESEND-OTP] Email already registered: '{}'", normalizedEmail);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Email already registered. Please login instead.")
                                .email(normalizedEmail)
                                .build());
            }

            // Check if there's a pending registration
            Optional<PendingRegistration> existingPending = pendingRegistrationService
                    .findByEmail(normalizedEmail);
            
            if (existingPending.isEmpty()) {
                log.warn("⚠️ [RESEND-OTP] No pending registration found for: '{}'", normalizedEmail);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("No pending registration found. Please register first.")
                                .email(normalizedEmail)
                                .build());
            }

            PendingRegistration pending = existingPending.get();
            
            // Generate new 6-digit OTP
            long newOtp = 100000 + new Random().nextInt(900000);
            log.info("🔢 [RESEND-OTP] Generated new OTP for '{}': {}", normalizedEmail, newOtp);

            // Update pending registration with new OTP and extended expiry
            PendingRegistration updatedPending = PendingRegistration.builder()
                    .email(pending.getEmail())
                    .fullName(pending.getFullName())
                    .phone(pending.getPhone())
                    .passwordHash(pending.getPasswordHash())
                    .otp(newOtp)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            pendingRegistrationService.savePending(updatedPending);

            // Send new OTP email
            try {
                emailService.sendRegistrationOtp(normalizedEmail, pending.getFullName(), newOtp);
                log.info("✅ [RESEND-OTP] New OTP email sent to: '{}'", normalizedEmail);
            } catch (Exception emailEx) {
                log.error("❌ [RESEND-OTP] Failed to send OTP email: {}", emailEx.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(RegisterResponse.builder()
                                .success(false)
                                .message("Failed to send OTP email")
                                .build());
            }

            return ResponseEntity.ok(RegisterResponse.builder()
                    .success(true)
                    .message("New OTP sent to your email. Please verify within 5 minutes.")
                    .email(normalizedEmail)
                    .build());

        } catch (Exception e) {
            log.error("❌ [RESEND-OTP] Failed to resend OTP - Email: '{}' | Error: {}",
                    normalizedEmail, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RegisterResponse.builder()
                            .success(false)
                            .message("Failed to resend OTP: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseV2> login(@RequestBody LoginDTO loginDto,
            HttpServletRequest request,
            HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();
        try {
            // Xác thực username/password
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginDto.getUsername(), loginDto.getPassword());

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

            // Lưu thông tin xác thực vào context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lấy user từ DB để sinh JWT
            User user = userRepository.findByEmail(loginDto.getUsername())
                    .orElseThrow(() -> {
                        return new RuntimeException("User not found");
                    });

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // 🍪 Set tokens as HTTP-only cookies (SECURE)
            cookieUtil.setAccessTokenCookie(response, accessToken);
            cookieUtil.setRefreshTokenCookie(response, refreshToken);

            // Lưu refresh_token vào DB
            user.setRefreshToken(refreshToken);
            userRepository.save(user);

            LoginResponseV2 loginResponse = LoginResponseV2.builder()
                    .accessToken(accessToken) // Add accessToken for mobile apps
                    .success(true)
                    .role(user.getRole().name())
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok().body(loginResponse);

        } catch (BadCredentialsException e) {
            log.warn("⚠️ [LOGIN] Bad credentials - Username: {} | IP: {} | Error: {}",
                    loginDto.getUsername(), clientIp, e.getMessage());
            throw e; // Spring Security sẽ xử lý

        } catch (AuthenticationException e) {
            log.error("❌ [LOGIN] Authentication failed - Username: {} | IP: {} | Error: {}",
                    loginDto.getUsername(), clientIp, e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("❌ [LOGIN] Unexpected error - Username: {} | IP: {} | Error: {}",
                    loginDto.getUsername(), clientIp, e.getMessage(), e);
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    @PostMapping("/refresh")
    @ApiMessage("Get Access Token")
    public ResponseEntity<LoginResponseV2> getNewAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws EntityExistsException {
        log.info("🔄 [REFRESH] Refresh token request from cookies");

        try {
            // 🍪 Lấy refresh token từ cookies
            String refreshToken = cookieUtil.getRefreshTokenFromCookies(request)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found in cookies"));

            // Tạo request object để tái sử dụng service hiện tại
            RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
            refreshRequest.setRefresh_token(refreshToken);

            LoginResponseV1 tokenResponse = this.authenicationService.handleRefreshToken(refreshRequest);

            // 🍪 Set new tokens as cookies
            cookieUtil.setAccessTokenCookie(response, tokenResponse.getAccess_token());
            cookieUtil.setRefreshTokenCookie(response, tokenResponse.getRefresh_token());

            // Get user info for response
            String username = jwtService.extractUsername(tokenResponse.getAccess_token());
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LoginResponseV2 response2 = LoginResponseV2.builder()
                    .success(true)
                    .message("Token refreshed successfully")
                    .build();

            log.info("✅ [REFRESH] New access token generated and set as cookie");
            return ResponseEntity.ok().body(response2);
        } catch (Exception e) {
            log.error("❌ [REFRESH] Failed to refresh token - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/logout")
    @ApiMessage("Logout user")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        log.info("🚪 [LOGOUT] Logout request - User: {}", username);

        try {
            // 🍪 Clear all authentication cookies
            cookieUtil.clearAuthCookies(response);

            // Clear security context
            SecurityContextHolder.clearContext();

            log.info("✅ [LOGOUT] Logout successful - User: {} | Cookies cleared", username);

            Map<String, Object> logoutResponse = Map.of(
                    "success", true,
                    "message", "Logout successful");

            return ResponseEntity.ok().body(logoutResponse);
        } catch (Exception e) {
            log.error("❌ [LOGOUT] Logout failed - User: {} | Error: {}", username, e.getMessage(), e);

            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Logout failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    @ApiMessage("Get Access Token")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) throws EntityExistsException {
        log.info("🔑 [FORGOT-PASSWORD] Password reset request - Email: {} | Time: {}", email, LocalDateTime.now());

        try {
            this.authenicationService.handleForgotPassword(email);
            log.info("✅ [FORGOT-PASSWORD] Reset email sent successfully - Email: {}", email);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (Exception e) {
            log.error("❌ [FORGOT-PASSWORD] Failed to send reset email - Email: {} | Error: {}", email, e.getMessage(),
                    e);
            throw e;
        }
    }

    @GetMapping("/verify/{token}")
    @ApiMessage("Verify Token")
    public ResponseEntity<Void> verifyToken(@PathVariable("token") String token) throws VerificationException {
        log.info("✉️ [VERIFY] Email verification request - Token: {}...",
                token.substring(0, Math.min(20, token.length())));

        try {
            this.authenicationService.handleVerify(token);
            log.info("✅ [VERIFY] Email verified successfully - Token: {}...", token.substring(0, 10));
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error("❌ [VERIFY] Email verification failed - Token: {}... | Error: {}",
                    token.substring(0, Math.min(20, token.length())), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reset-password")
    @ApiMessage("Reset password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest)
            throws VerificationException {
        log.info("🔐 [RESET-PASSWORD] Password reset request - Token provided");

        try {
            authenicationService.handleResetPassword(resetPasswordRequest);
            log.info("✅ [RESET-PASSWORD] Password reset successfully");
            return ResponseEntity.ok().body("Reset password successfully");
        } catch (Exception e) {
            log.error("❌ [RESET-PASSWORD] Password reset failed - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/change-password")
    @ApiMessage("Change password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        log.info("🔐 [CHANGE-PASSWORD] Password change request - User: {}", username);

        try {
            authenicationService.handleChangePassword(changePasswordRequest, authentication);
            log.info("✅ [CHANGE-PASSWORD] Password changed successfully - User: {}", username);
            return ResponseEntity.ok().body("Change password successful");
        } catch (Exception e) {
            log.error("❌ [CHANGE-PASSWORD] Password change failed - User: {} | Error: {}",
                    username, e.getMessage(), e);
            throw e;
        }
    }

    // ============================================
    // 🔐 NEW: Forgot Password with OTP Flow
    // ============================================

    @PostMapping("/forgot-password-otp")
    @ApiMessage("Request password reset with OTP")
    public ResponseEntity<ForgotPasswordResponse> forgotPasswordWithOtp(
            @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        log.info("🔑 [FORGOT-PASSWORD-OTP] Request - Email: {} | IP: {}", request.getEmail(), clientIp);

        try {
            // Check if user exists
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("⚠️ [FORGOT-PASSWORD-OTP] Email not found: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ForgotPasswordResponse.builder()
                                .success(false)
                                .message("Email không tồn tại trong hệ thống")
                                .email(request.getEmail())
                                .build());
            }

            User user = userOpt.get();

            // Generate 6-digit OTP
            long otp = 100000 + new Random().nextInt(900000);
            log.info("🔢 [FORGOT-PASSWORD-OTP] Generated OTP for {}: {}", request.getEmail(), otp);

            // Create pending password reset
            PendingPasswordReset pending = PendingPasswordReset.builder()
                    .email(request.getEmail())
                    .otp(otp)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .otpVerified(false)
                    .build();

            pendingPasswordResetService.savePending(pending);

            // Send OTP email
            try {
                emailService.sendPasswordResetOtp(request.getEmail(), user.getFullName(), otp);
                log.info("✅ [FORGOT-PASSWORD-OTP] OTP email sent to: {}", request.getEmail());
            } catch (Exception emailEx) {
                log.error("❌ [FORGOT-PASSWORD-OTP] Failed to send OTP email: {}", emailEx.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ForgotPasswordResponse.builder()
                                .success(false)
                                .message("Không thể gửi email OTP")
                                .build());
            }

            return ResponseEntity.ok(ForgotPasswordResponse.builder()
                    .success(true)
                    .message("Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra và nhập trong vòng 5 phút.")
                    .email(request.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("❌ [FORGOT-PASSWORD-OTP] Failed - Email: {} | Error: {}",
                    request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ForgotPasswordResponse.builder()
                            .success(false)
                            .message("Có lỗi xảy ra: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/verify-password-reset-otp")
    @ApiMessage("Verify OTP for password reset")
    public ResponseEntity<VerifyOtpResponse> verifyPasswordResetOtp(@RequestBody VerifyOtpDTO verifyOtpDTO) {
        log.info("🔢 [VERIFY-RESET-OTP] Verification request - Email: {}", verifyOtpDTO.getEmail());

        try {
            boolean verified = pendingPasswordResetService.verifyOtp(
                    verifyOtpDTO.getEmail(),
                    verifyOtpDTO.getOtp());

            if (!verified) {
                log.warn("⚠️ [VERIFY-RESET-OTP] Invalid OTP or expired - Email: {}", verifyOtpDTO.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VerifyOtpResponse.builder()
                                .success(false)
                                .message("Mã OTP không đúng hoặc đã hết hạn")
                                .email(verifyOtpDTO.getEmail())
                                .build());
            }

            log.info("✅ [VERIFY-RESET-OTP] OTP verified successfully - Email: {}", verifyOtpDTO.getEmail());

            return ResponseEntity.ok(VerifyOtpResponse.builder()
                    .success(true)
                    .message("Xác thực OTP thành công. Bạn có thể đặt mật khẩu mới.")
                    .email(verifyOtpDTO.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("❌ [VERIFY-RESET-OTP] Verification failed - Email: {} | Error: {}",
                    verifyOtpDTO.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerifyOtpResponse.builder()
                            .success(false)
                            .message("Có lỗi xảy ra: " + e.getMessage())
                            .email(verifyOtpDTO.getEmail())
                            .build());
        }
    }

    @PostMapping("/reset-password-with-otp")
    @ApiMessage("Reset password with verified OTP")
    public ResponseEntity<VerifyOtpResponse> resetPasswordWithOtp(
            @RequestBody ResetPasswordWithOtpRequest request) {
        log.info("🔐 [RESET-PASSWORD-OTP] Reset request - Email: {}", request.getEmail());

        try {
            // Check if OTP was verified
            if (!pendingPasswordResetService.isOtpVerified(request.getEmail())) {
                log.warn("⚠️ [RESET-PASSWORD-OTP] OTP not verified - Email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(VerifyOtpResponse.builder()
                                .success(false)
                                .message("Vui lòng xác thực OTP trước khi đặt lại mật khẩu")
                                .email(request.getEmail())
                                .build());
            }

            // Find user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

            // Update password
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Remove pending reset
            pendingPasswordResetService.removePending(request.getEmail());

            log.info("✅ [RESET-PASSWORD-OTP] Password reset successfully - Email: {}", request.getEmail());

            return ResponseEntity.ok(VerifyOtpResponse.builder()
                    .success(true)
                    .message("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.")
                    .email(request.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("❌ [RESET-PASSWORD-OTP] Reset failed - Email: {} | Error: {}",
                    request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(VerifyOtpResponse.builder()
                            .success(false)
                            .message("Có lỗi xảy ra: " + e.getMessage())
                            .email(request.getEmail())
                            .build());
        }
    }

    /*
     * @GetMapping("/oauth2/success")
     * public ResponseEntity<?> success(@AuthenticationPrincipal OAuth2User
     * principal) {
     * // Lấy thông tin user từ GitLab
     * String username = principal.getAttribute("username");
     * String email = principal.getAttribute("email");
     * String name = principal.getAttribute("name");
     * 
     * // Tạo JWT token
     * String token = jwtService.generateToken(username);
     * 
     * // Trả JSON gồm token + thông tin user
     * Map<String, Object> response = Map.of(
     * "token", token,
     * "username", username,
     * "email", email,
     * "name", name,
     * "attributes", principal.getAttributes() // nếu muốn trả toàn bộ dữ liệu
     * );
     * 
     * return ResponseEntity.ok(response);
     * }
     */
}

