package com.proj.webprojrct.admin.repository;

import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<User, Long> {

    // Tìm tất cả người dùng với phân trang
    Page<User> findAll(Pageable pageable);

    // Tìm kiếm người dùng theo tên (fullName)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Tìm kiếm người dùng theo email
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Tìm kiếm người dùng theo số điện thoại
    Page<User> findByPhoneContaining(String phone, Pageable pageable);

    // Lọc người dùng theo vai trò
    Page<User> findByRole(UserRole role, Pageable pageable);

    // Lọc người dùng theo trạng thái hoạt động
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    // Tìm kiếm theo tên hoặc email
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String fullName, String email, Pageable pageable);

    // Tìm kiếm theo tên/email và vai trò
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRole(
        String fullName, String email, UserRole role, Pageable pageable);

    // Tìm kiếm theo tên/email và trạng thái
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndIsActive(
        String fullName, String email, Boolean isActive, Pageable pageable);

    // Tìm kiếm theo tên/email, vai trò và trạng thái
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRoleAndIsActive(
        String fullName, String email, UserRole role, Boolean isActive, Pageable pageable);

    // Lọc theo vai trò và trạng thái
    Page<User> findByRoleAndIsActive(UserRole role, Boolean isActive, Pageable pageable);

    // Lọc người dùng theo trạng thái hoạt động (removed emailVerified - not exist in entity)
    // Page<User> findByEmailVerified(Boolean emailVerified, Pageable pageable);

    // Tìm người dùng được tạo trong khoảng thời gian
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);

    /**
     * Tìm kiếm người dùng với nhiều bộ lọc kết hợp
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " u.phone LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:startDate IS NULL OR u.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR u.createdAt <= :endDate)")
    Page<User> findWithFilters(@Param("keyword") String keyword,
                              @Param("role") UserRole role,
                              @Param("isActive") Boolean isActive,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);

    // Thống kê số lượng người dùng theo vai trò
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    // Thống kê số lượng người dùng đăng ký theo tháng trong năm
    @Query("SELECT MONTH(u.createdAt), COUNT(u) FROM User u " +
           "WHERE YEAR(u.createdAt) = :year " +
           "GROUP BY MONTH(u.createdAt) " +
           "ORDER BY MONTH(u.createdAt)")
    List<Object[]> countUserRegistrationsByMonth(@Param("year") int year);

    // Đếm tổng số người dùng hoạt động
    Long countByIsActiveTrue();

    // Đếm số người dùng đăng ký từ thời điểm nhất định
    Long countByCreatedAtGreaterThanEqual(LocalDateTime fromDate);

    // Đếm tổng số người dùng theo vai trò
    Long countByRole(UserRole role);

    // Đếm tổng số người dùng đã xác thực email (removed - emailVerified not exist in entity)
    // Long countByEmailVerifiedTrue();

    // Tìm người dùng mới nhất
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    Page<User> findLatestUsers(Pageable pageable);

    // Cập nhật trạng thái hoạt động của người dùng
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :userId")
    void updateUserActiveStatus(@Param("userId") Long userId, @Param("isActive") Boolean isActive);

    // Cập nhật vai trò của người dùng
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateUserRole(@Param("userId") Long userId, @Param("role") UserRole role);

    // Tìm người dùng theo ID để quản lý
    Optional<User> findById(Long id);

    // Tìm người dùng theo email
    Optional<User> findByEmail(String email);

    // Kiểm tra xem email đã tồn tại chưa (trừ user hiện tại)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :excludeId")
    Boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);

    // Kiểm tra xem số điện thoại đã tồn tại chưa (trừ user hiện tại)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phone = :phone AND u.id != :excludeId")
    Boolean existsByPhoneAndIdNot(@Param("phone") String phone, @Param("excludeId") Long excludeId);
}