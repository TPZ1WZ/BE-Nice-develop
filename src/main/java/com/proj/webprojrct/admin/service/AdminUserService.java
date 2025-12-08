package com.proj.webprojrct.admin.service;

import com.proj.webprojrct.admin.repository.AdminUserRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.proj.webprojrct.admin.dto.AdminUserCreateRequest;
import com.proj.webprojrct.admin.dto.AdminUserUpdateRequest;
// imports cleaned

/**
 * Service xử lý business logic cho quản lý người dùng trong admin dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    // Helper methods để giảm code trùng lặp trong logging
    private void logDebug(String message, Object... args) {
        log.debug(message, args);
    }

    private void logInfo(String message, Object... args) {
        log.info(message, args);
    }

    private void logWarn(String message, Object... args) {
        log.warn(message, args);
    }

    private void logError(String message, Object... args) {
        log.error(message, args);
    }

    /**
     * Lấy tất cả người dùng với phân trang
     */
    public Page<User> getAllUsers(Pageable pageable) {
        logDebug("Lấy danh sách người dùng với phân trang: {}", pageable);
        return adminUserRepository.findAll(pageable);
    }

    /**
     * Tìm kiếm người dùng theo tên hoặc email
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        logDebug("Tìm kiếm người dùng với keyword: {} và phân trang: {}", keyword, pageable);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        return adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            keyword, keyword, pageable);
    }

    /**
     * Tìm kiếm người dùng với các bộ lọc
     */
    public Page<User> findUsers(String search, UserRole role, Boolean isActive, Pageable pageable) {
        logDebug("Tìm kiếm người dùng với search: {}, role: {}, isActive: {}", search, role, isActive);
        
        if (search != null && !search.trim().isEmpty()) {
            search = search.trim();
            if (role != null && isActive != null) {
                return adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleAndIsActive(
                    search, search, role, isActive, pageable);
            } else if (role != null) {
                return adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRole(
                    search, search, role, pageable);
            } else if (isActive != null) {
                return adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndIsActive(
                    search, search, isActive, pageable);
            } else {
                return adminUserRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
            }
        } else {
            if (role != null && isActive != null) {
                return adminUserRepository.findByRoleAndIsActive(role, isActive, pageable);
            } else if (role != null) {
                return adminUserRepository.findByRole(role, pageable);
            } else if (isActive != null) {
                return adminUserRepository.findByIsActive(isActive, pageable);
            } else {
                return getAllUsers(pageable);
            }
        }
    }

    /**
     * Lấy thông tin chi tiết người dùng
     */
    public Optional<User> getUserById(Long id) {
        logDebug("Lấy thông tin người dùng với ID: {}", id);
        return adminUserRepository.findById(id);
    }

    /**
     * Lấy thống kê tổng quan về người dùng
     */
    public Map<String, Object> getUserStatistics() {
        logDebug("Lấy thống kê người dùng");
        
        long totalUsers = adminUserRepository.count();
        
        // Đếm số người dùng đang hoạt động
        long activeUsers = adminUserRepository.countByIsActiveTrue();
        
        // Đếm số người dùng mới trong tháng này
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = adminUserRepository.countByCreatedAtGreaterThanEqual(startOfMonth);
        
        return Map.of(
            "totalUsers", totalUsers,
            "activeUsers", activeUsers,
            "newUsersThisMonth", newUsersThisMonth,
            "inactiveUsers", totalUsers - activeUsers
        );
    }

    /**
     * Cập nhật trạng thái hoạt động của người dùng
     */
    @Transactional
    public boolean updateUserStatus(Long userId, boolean isActive) {
        logDebug("Cập nhật trạng thái người dùng ID: {} thành: {}", userId, isActive);
        try {
            Optional<User> userOpt = adminUserRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setIsActive(isActive);
                user.setUpdatedAt(LocalDateTime.now());
                adminUserRepository.save(user);
                logInfo("Cập nhật trạng thái người dùng ID: {} thành công", userId);
                return true;
            }
            logWarn("Không tìm thấy người dùng với ID: {}", userId);
            return false;
        } catch (Exception e) {
            logError("Lỗi khi cập nhật trạng thái người dùng ID: {} - {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách người dùng đăng ký gần đây
     */
    public List<User> getRecentUsers(int limit) {
        logDebug("Lấy {} người dùng đăng ký gần đây", limit);
        // TODO: Implement method getRecentUsers() trong repository
        return List.of(); // Placeholder
    }

    /**
     * Kiểm tra email đã tồn tại chưa
     */
    public boolean isEmailExists(String email) {
        logDebug("Kiểm tra email tồn tại: {}", email);
        // TODO: Implement method existsByEmail() trong repository
        return false; // Placeholder
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     */
    public boolean isUsernameExists(String username) {
        logDebug("Kiểm tra username tồn tại: {}", username);
        // TODO: Implement method existsByUsername() trong repository  
        return false; // Placeholder
    }

    /**
     * Xóa người dùng (hard delete - xóa hoàn toàn)
     */
    @Transactional
    public boolean deleteUser(Long userId) {
        logDebug("Xóa người dùng ID: {}", userId);
        try {
            Optional<User> userOpt = adminUserRepository.findById(userId);
            if (userOpt.isPresent()) {
                // Hard delete - xóa hoàn toàn khỏi database
                adminUserRepository.deleteById(userId);
                logInfo("Xóa (hard delete) người dùng ID: {} thành công", userId);
                return true;
            }
            logWarn("Không tìm thấy người dùng với ID: {}", userId);
            return false;
        } catch (Exception e) {
            logError("Lỗi khi xóa người dùng ID: {} - {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * Tạo người dùng mới bởi admin
     */
    @Transactional
    public Optional<User> createUser(AdminUserCreateRequest req) {
        logDebug("Tạo người dùng mới: {}", req.getEmail());
        
        // Validation cơ bản
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        if (req.getFullName() == null || req.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên là bắt buộc");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu là bắt buộc");
        }
        if (req.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        // Kiểm tra email đã tồn tại
        Optional<User> existing = adminUserRepository.findByEmail(req.getEmail().trim());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        try {
            User u = new User();
            u.setFullName(req.getFullName().trim());
            u.setEmail(req.getEmail().trim().toLowerCase());
            u.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            
            // Xử lý role với giá trị mặc định
            if (req.getRole() != null && !req.getRole().trim().isEmpty()) {
                try {
                    u.setRole(UserRole.valueOf(req.getRole().toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    logWarn("Role không hợp lệ: {}, sử dụng MEMBER làm mặc định", req.getRole());
                    u.setRole(UserRole.MEMBER);
                }
            } else {
                u.setRole(UserRole.MEMBER);
            }
            
            u.setIsActive(true);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            
            // Set avatar mặc định
            u.setAvatarUrl("uploads/avatars/defaultAvt.jpg");

            User saved = adminUserRepository.save(u);
            logInfo("Tạo người dùng mới thành công: {} với ID: {}", saved.getEmail(), saved.getId());
            return Optional.of(saved);
            
        } catch (Exception ex) {
            logError("Lỗi khi tạo người dùng: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi hệ thống khi tạo người dùng");
        }
    }

    /**
     * Cập nhật thông tin người dùng
     */
    @Transactional
    public Optional<User> updateUser(Long userId, AdminUserUpdateRequest req) {
        logDebug("Cập nhật người dùng ID: {}", userId);
        
        try {
            Optional<User> userOpt = adminUserRepository.findById(userId);
            if (!userOpt.isPresent()) {
                logWarn("Không tìm thấy người dùng với ID: {}", userId);
                return Optional.empty();
            }
            
            User user = userOpt.get();
            
            // Cập nhật các trường nếu có giá trị
            if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
                user.setFullName(req.getFullName().trim());
            }
            
            if (req.getPhone() != null) {
                user.setPhone(req.getPhone().trim().isEmpty() ? null : req.getPhone().trim());
            }
            
            // Cập nhật role
            if (req.getRole() != null && !req.getRole().trim().isEmpty()) {
                try {
                    user.setRole(UserRole.valueOf(req.getRole().toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    logWarn("Role không hợp lệ: {}, giữ nguyên role hiện tại", req.getRole());
                }
            }
            
            // Cập nhật trạng thái
            if (req.getIsActive() != null) {
                user.setIsActive(req.getIsActive());
            }
            
            // Cập nhật mật khẩu nếu có
            if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
                if (req.getPassword().length() < 6) {
                    throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
                }
                user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                logDebug("Đã cập nhật mật khẩu cho user ID: {}", userId);
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            
            User updated = adminUserRepository.save(user);
            logInfo("Cập nhật người dùng ID: {} thành công", userId);
            return Optional.of(updated);
            
        } catch (IllegalArgumentException ex) {
            logWarn("Lỗi validation khi cập nhật user ID {}: {}", userId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logError("Lỗi hệ thống khi cập nhật user ID {}: {}", userId, ex.getMessage(), ex);
            throw new RuntimeException("Lỗi hệ thống khi cập nhật người dùng");
        }
    }
}