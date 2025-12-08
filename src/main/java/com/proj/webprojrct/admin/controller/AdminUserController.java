package com.proj.webprojrct.admin.controller;

import com.proj.webprojrct.admin.service.AdminUserService;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.admin.dto.AdminUserCreateRequest;
import com.proj.webprojrct.admin.dto.AdminUserUpdateRequest;
import com.proj.webprojrct.user.entity.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;


import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller quản lý người dùng trong admin dashboard
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Hiển thị trang quản lý người dùng
     */

    // View rendering moved to a dedicated page controller (com.proj.webprojrct.admin.user.AdminUserController)
    // This class focuses on API endpoints. The page-level GET /admin/users mapping was removed to avoid
    // ambiguous mapping with the page controller.

    @GetMapping("/admin/users")
    public String usersPage(Model model) {
        return "admin/users";
    }

    /**
     * API thống kê người dùng cho dashboard cards
     */
    @GetMapping("/api/admin/users/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = adminUserService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy danh sách người dùng với tìm kiếm và lọc
     */
    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable) {
        
        Page<User> userPage;
        if (search != null || role != null || isActive != null) {
            userPage = adminUserService.findUsers(search, role, isActive, pageable);
        } else {
            userPage = adminUserService.getAllUsers(pageable);
        }
        return ResponseEntity.ok(userPage);
    }

    /**
     * Lấy thông tin chi tiết một người dùng
     */
    @GetMapping("/api/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id)
                .map(this::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cập nhật thông tin người dùng
     */
    @PutMapping("/api/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid AdminUserUpdateRequest request) {
        try {
            Optional<User> updated = adminUserService.updateUser(id, request);
            if (updated.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật người dùng thành công",
                    "user", updated.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi hệ thống: " + ex.getMessage()));
        }
    }

    /**
     * Cập nhật trạng thái active của người dùng
     */
    @PutMapping("/api/admin/users/{id}/status")
    @ResponseBody
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        
        boolean updated = adminUserService.updateUserStatus(id, isActive);
        return updated ? success("Cập nhật trạng thái người dùng thành công") : notFound();
    }

    /**
     * Xóa người dùng (soft delete - set isActive = false)
     */
    @DeleteMapping("/api/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = adminUserService.deleteUser(id);
        return deleted ? success("Xóa người dùng thành công") : notFound();
    }

    /**
     * Tạo người dùng mới (từ modal)
     */
    @PostMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody @Valid AdminUserCreateRequest req) {
        try {
            var created = adminUserService.createUser(req);
            if (created.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Tạo người dùng thành công", 
                    "id", created.get().getId(),
                    "user", created.get()
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of("message", "Không thể tạo người dùng"));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi hệ thống: " + ex.getMessage()));
        }
    }

    /**
     * Tìm kiếm người dùng
     */
    @GetMapping("/api/admin/users/search")
    @ResponseBody
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String keyword, 
            Pageable pageable) {
        Page<User> users = adminUserService.searchUsers(keyword, pageable);
        return ok(users);
    }

    /**
     * Thống kê người dùng theo role
     */
    @GetMapping("/api/admin/users/stats/by-role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserStatsByRole() {
        Map<String, Object> stats = adminUserService.getUserStatistics();
        return ok(stats);
    }

    /**
     * Thống kê tổng số người dùng
     */
    @GetMapping("/api/admin/users/stats/total")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTotalUsers() {
        Map<String, Object> stats = adminUserService.getUserStatistics();
        return ok(Map.of("totalUsers", stats.get("totalUsers")));
    }

    // Helper methods để giảm code trùng lặp
    private <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<String> success(String message) {
        return ResponseEntity.ok(message);
    }

    private ResponseEntity<String> notFound() {
        return ResponseEntity.notFound().build();
    }
}