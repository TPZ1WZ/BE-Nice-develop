# Admin Module Repositories

## Tổng quan
Module admin repositories cung cấp các interface repository chuyên biệt cho việc quản lý và thống kê dữ liệu trong admin dashboard.

## Cấu trúc Repositories

### 1. AdminUserRepository
**Mục đích**: Quản lý người dùng trong admin dashboard
**Chức năng chính**:
- ✅ Tìm kiếm người dùng theo tên, email, phone
- ✅ Lọc theo vai trò (role), trạng thái hoạt động, xác thực email
- ✅ Thống kê người dùng theo tháng, vai trò
- ✅ Cập nhật trạng thái và vai trò người dùng
- ✅ Kiểm tra trùng lặp email/phone khi cập nhật

### 2. AdminProductRepository
**Mục đích**: Quản lý sản phẩm trong admin dashboard
**Chức năng chính**:
- ✅ Tìm kiếm sản phẩm theo tên, mô tả, danh mục
- ✅ Lọc theo khoảng giá, số lượng bán
- ✅ Thống kê sản phẩm theo danh mục, doanh thu
- ✅ Tìm sản phẩm bán chạy/ít bán nhất
- ✅ Quản lý attributes (tags, sizes, colors)

### 3. AdminOrderRepository
**Mục đích**: Quản lý đơn hàng trong admin dashboard
**Chức năng chính**:
- ✅ Tìm kiếm đơn hàng theo trạng thái, phương thức thanh toán
- ✅ Lọc theo người dùng, khoảng thời gian, giá trị
- ✅ Thống kê doanh thu theo tháng/ngày
- ✅ Tìm top khách hàng theo giá trị/số lượng đơn
- ✅ Quản lý đơn hàng quá hạn xử lý

### 4. AdminOrderItemRepository
**Mục đích**: Quản lý chi tiết đơn hàng
**Chức năng chính**:
- ✅ Thống kê sản phẩm bán chạy theo số lượng/doanh thu
- ✅ Phân tích sản phẩm theo khoảng thời gian
- ✅ Tìm sản phẩm chưa từng được bán
- ✅ Thống kê theo tuần/tháng/ngày

### 5. AdminReviewRepository
**Mục đích**: Quản lý đánh giá sản phẩm
**Chức năng chính**:
- ✅ Tìm kiếm đánh giá theo sản phẩm, người dùng, rating
- ✅ Quản lý duyệt/ẩn đánh giá
- ✅ Thống kê rating theo sản phẩm/danh mục
- ✅ Xử lý báo cáo vi phạm
- ✅ Tìm top reviewer và sản phẩm rating cao/thấp

### 6. AdminDashboardRepository
**Mục đích**: Thống kê tổng hợp cho dashboard
**Chức năng chính**:
- ✅ Thống kê tổng quan hệ thống
- ✅ Báo cáo doanh thu theo khoảng thời gian
- ✅ Top sản phẩm bán chạy/khách hàng VIP
- ✅ Phân tích xu hướng 12 tháng
- ✅ Cảnh báo các chỉ số cần chú ý
- ✅ Thống kê theo phương thức thanh toán

## Đặc điểm Kỹ thuật

### Native Queries
- Sử dụng native SQL cho các query phức tạp
- Tối ưu hiệu suất với các thống kê đa bảng
- Hỗ trợ các hàm tính toán phức tạp

### JPQL Queries
- Sử dụng JPQL cho các query đơn giản
- Type-safe với entity classes
- Dễ maintain và debug

### Parameterized Queries
- Tất cả queries đều sử dụng parameters
- Bảo mật chống SQL injection
- Có thể cache và reuse

### Pagination Support
- Hỗ trợ phân trang cho tất cả list operations
- Tích hợp Spring Data Pageable
- Tối ưu memory với large datasets

## Patterns và Best Practices

### 1. Naming Convention
```java
// Tìm kiếm đơn giản
findByField(value)
findByFieldContaining(value)

// Tìm kiếm phức tạp
findWithFilters(params...)

// Thống kê
countByField()
calculateTotalField()

// Cập nhật
updateFieldStatus()
```

### 2. Filter Patterns
```java
// Null-safe filtering
(:param IS NULL OR field = :param)

// Range filtering  
field BETWEEN :min AND :max

// Text search
LOWER(field) LIKE LOWER(CONCAT('%', :keyword, '%'))
```

### 3. Statistics Patterns
```java
// Group by với tính toán
SELECT field, COUNT(*), SUM(amount) 
FROM entity 
GROUP BY field 
ORDER BY COUNT(*) DESC

// Time-based analysis
SELECT MONTH(date), SUM(amount) 
FROM entity 
WHERE YEAR(date) = :year 
GROUP BY MONTH(date)
```

## Sử dụng trong Service Layer

### Inject Repository
```java
@Service
public class AdminUserService {
    @Autowired
    private AdminUserRepository adminUserRepository;
    
    @Autowired
    private AdminDashboardRepository dashboardRepository;
}
```

### Pagination Example
```java
public Page<User> getUsers(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by("createdAt").descending());
    return adminUserRepository.findAll(pageable);
}
```

### Filter Example
```java
public Page<User> searchUsers(UserSearchCriteria criteria) {
    return adminUserRepository.findWithFilters(
        criteria.getKeyword(),
        criteria.getRole(),
        criteria.getIsActive(),
        criteria.getEmailVerified(),
        criteria.getStartDate(),
        criteria.getEndDate(),
        criteria.getPageable()
    );
}
```

## Mở rộng và Tùy chỉnh

### Thêm Query Mới
1. Thêm method signature vào interface
2. Sử dụng @Query annotation
3. Test thoroughly với sample data
4. Document trong comments

### Performance Optimization
1. Thêm indexes cho các fields thường query
2. Sử dụng native queries cho complex operations
3. Implement caching cho frequent queries
4. Monitor query execution time

### Error Handling
1. Validate parameters trước khi query
2. Handle null results gracefully
3. Log slow queries
4. Implement retry mechanism nếu cần

## Kết hợp với DTOs
Repositories này được thiết kế để làm việc với:
- ✅ Admin Request DTOs (từ frontend)
- ✅ Admin Response DTOs (trả về frontend) 
- ✅ PageResponse<T> cho pagination
- ✅ Entity classes hiện có

## Next Steps
1. Tạo Admin Service layer sử dụng các repositories này
2. Implement Admin Controllers với proper validation
3. Tạo Admin Mappers cho entity-DTO conversion
4. Viết unit tests cho tất cả repository methods
5. Optimize database indexes dựa trên query patterns