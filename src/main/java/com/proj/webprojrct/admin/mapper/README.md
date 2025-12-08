# Admin Dashboard Mappers

Module này chứa các **MapStruct mapper interfaces** được thiết kế theo pattern **Cart Module** cho **Admin Dashboard** của hệ thống NICESTORE. Các mapper này chuyển đổi dữ liệu giữa **Entity** và **Admin DTOs** một cách đơn giản và hiệu quả.

---

## 📝 Pattern Chuẩn (Theo Cart Module)

Tất cả mapper đều follow **pattern nhất quán**:

```java
@Mapper(componentModel = "spring")
public interface AdminXXXMapper {
    AdminXXXMapper INSTANCE = Mappers.getMapper(AdminXXXMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true) 
    @Mapping(target = "updatedAt", ignore = true)
    Entity toEntity(RequestDTO dto);

    ResponseDTO toResponse(Entity entity);

    List<ResponseDTO> toResponseList(List<Entity> entities);
    List<Entity> toEntityList(List<RequestDTO> dtos);
}
```

---

## 🗂️ Danh sách Mappers

### 1. **AdminUserMapper** 
```java
@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    AdminUserMapper INSTANCE = Mappers.getMapper(AdminUserMapper.class);

    // Request -> Entity (Tạo mới user từ admin)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User toEntity(UserManagementRequest request);

    // Entity -> Response (Hiển thị user trong admin)
    UserManagementResponse toResponse(User user);

    // Bulk operations
    List<UserManagementResponse> toResponseList(List<User> users);
    List<User> toEntityList(List<UserManagementRequest> requests);
}
```

**🎯 Mục đích**: Chuyển đổi User entity ↔ Admin DTOs cho **user management**

---

### 2. **AdminProductMapper**
```java
@Mapper(componentModel = "spring")
public interface AdminProductMapper {
    AdminProductMapper INSTANCE = Mappers.getMapper(AdminProductMapper.class);

    // Request -> Entity (Tạo mới product từ admin)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categoryId", ignore = true) // Complex field handled separately
    Product toEntity(ProductManagementRequest request);

    // Entity -> Response (Hiển thị product trong admin)
    ProductManagementResponse toResponse(Product product);

    // Bulk operations
    List<ProductManagementResponse> toResponseList(List<Product> products);
    List<Product> toEntityList(List<ProductManagementRequest> requests);
}
```

**🎯 Mục đích**: Chuyển đổi Product entity ↔ Admin DTOs cho **inventory management**

---

### 3. **AdminOrderMapper**
```java
@Mapper(componentModel = "spring")
public interface AdminOrderMapper {
    AdminOrderMapper INSTANCE = Mappers.getMapper(AdminOrderMapper.class);

    // Request -> Entity (Tạo mới order từ admin)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "items", ignore = true) // OneToMany relationship handled separately
    Order toEntity(OrderManagementRequest request);

    // Entity -> Response (Hiển thị order trong admin)
    OrderManagementResponse toResponse(Order order);

    // Bulk operations
    List<OrderManagementResponse> toResponseList(List<Order> orders);
    List<Order> toEntityList(List<OrderManagementRequest> requests);
}
```

**🎯 Mục đích**: Chuyển đổi Order entity ↔ Admin DTOs cho **order processing**

---

### 4. **AdminReviewMapper** 
```java
@Mapper(componentModel = "spring")
public interface AdminReviewMapper {
    AdminReviewMapper INSTANCE = Mappers.getMapper(AdminReviewMapper.class);

    // Review admin mapper methods will be added later
}
```

**🎯 Mục đích**: Sẽ chuyển đổi Review entity ↔ Admin DTOs cho **content moderation**

---

### 5. **AdminDashboardMapper**
```java
@Mapper(componentModel = "spring")
public interface AdminDashboardMapper {
    AdminDashboardMapper INSTANCE = Mappers.getMapper(AdminDashboardMapper.class);

    // Dashboard statistics mapper methods will be added later
}
```

**🎯 Mục đích**: Sẽ xử lý **statistical data** cho dashboard analytics

---

## ⚡ Ưu điểm Pattern Này

### **🎯 Đơn giản & Nhất quán**
- **Cùng 1 pattern** cho tất cả mappers → dễ hiểu, dễ maintain
- **4 methods chuẩn**: `toEntity()`, `toResponse()`, `toResponseList()`, `toEntityList()`
- **INSTANCE constant** để có thể dùng programmatically nếu cần

### **🔧 Tự động hóa**  
- **MapStruct compile-time generation** → zero reflection overhead
- **Spring integration** tự động với `componentModel = "spring"`
- **Type-safe mapping** với compile-time validation

### **🛡️ Bảo mật**
- **Explicit ignore** cho sensitive fields (`passwordHash`, `id`, timestamps)
- **Clear separation** giữa Request DTOs và Response DTOs  
- **No accidental data exposure**

---

## 🏗️ Kiến trúc Sử dụng

```
┌─────────────────────┐    ┌──────────────────────┐    ┌─────────────────────────┐
│   ADMIN CONTROLLER  │    │   ADMIN MAPPERS      │    │   ADMIN SERVICE LAYER   │
│                     │    │                      │    │                         │
│ UserManagementReq   │────▶│ AdminUserMapper      │────▶│ User Entity Operations  │
│ ProductManagementReq│────▶│ AdminProductMapper   │────▶│ Product Entity Ops      │
│ OrderManagementReq  │────▶│ AdminOrderMapper     │────▶│ Order Entity Ops        │
│                     │    │                      │    │                         │
│ ← Response DTOs ─────│◀───│ ← toResponse() ──────│◀───│ ← Entity Results        │
└─────────────────────┘    └──────────────────────┘    └─────────────────────────┘
```

---

## 🚀 Usage Example

### **Service Layer Integration**
```java
@Service
@RequiredArgsConstructor
public class AdminUserService {
    
    private final AdminUserMapper adminUserMapper;
    private final UserRepository userRepository;
    
    public UserManagementResponse createUser(UserManagementRequest request) {
        // Request -> Entity
        User user = adminUserMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Save entity
        User savedUser = userRepository.save(user);
        
        // Entity -> Response
        return adminUserMapper.toResponse(savedUser);
    }
    
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return adminUserMapper.toResponseList(users); // Bulk conversion
    }
}
```

### **Controller Layer Usage**
```java
@RestController
@RequestMapping("/admin/api")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminUserService adminUserService;
    
    @PostMapping("/users")
    public ResponseEntity<UserManagementResponse> createUser(
            @RequestBody @Valid UserManagementRequest request) {
        
        UserManagementResponse response = adminUserService.createUser(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users") 
    public ResponseEntity<List<UserManagementResponse>> getUsers() {
        List<UserManagementResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
```

---

## 🔧 Configuration

### **Maven Dependency** 
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### **Spring Auto-Configuration**
```java
// MapStruct tự động tạo implementation với @Component annotation
// Spring tự động scan và register beans

@Autowired
private AdminUserMapper adminUserMapper; // ✅ Ready to use!
```

---

## 📊 Performance Notes

- **✅ Compile-time code generation** → No reflection runtime cost
- **✅ Zero memory overhead** cho mapping operations  
- **✅ Thread-safe** → Generated implementations are stateless
- **✅ Type-safe** → Compile-time validation prevents ClassCastException
- **✅ Optimized bulk operations** → Efficient List<Entity> ↔ List<DTO> conversions

---

## 🎯 Design Principles

### **Single Responsibility**
- Mỗi mapper **chỉ focus vào 1 entity type** 
- **Clear separation** giữa admin operations và user-facing operations

### **Consistency First**
- **Same pattern** across all admin mappers
- **Predictable method signatures** → easy to learn và use
- **Uniform error handling** approach

### **Future-Proof**  
- **Extensible structure** → easy to add new mappers
- **Standard approach** → team members có thể quickly understand và contribute
- **Maintainable code** → changes ít affect other mappers

---

## 🎉 Kết luận

Admin Dashboard Mappers module này được thiết kế theo **pattern đơn giản**, **nhất quán**, và **hiệu quả**. Follow theo mẫu Cart Module, tất cả mappers đều có **cùng 1 structure** và **easy to use**. 

**Perfect balance** giữa **simplicity** và **functionality** cho admin management operations! 🚀