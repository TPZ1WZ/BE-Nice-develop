# 🚀 NIKE STORE - POSTMAN API TEST COMMANDS
*Tài liệu test API toàn bộ hệ thống Nike Store E-commerce với Postman*

---

## 🔧 **SETUP & CONFIGURATION**

### 🌍 Environment Variables
```bash
# Tạo Environment trong Postman với các biến sau:
baseUrl = http://localhost:8080
token = {{jwt_token_after_login}}
adminToken = {{admin_jwt_token}}
userId = {{current_user_id}}
productId = {{test_product_id}}
orderId = {{test_order_id}}
couponCode = {{test_coupon_code}}
```

### 🔑 Authentication Headers
```bash
# Để vào Headers của mỗi request cần authentication:
Authorization: Bearer {{token}}
Content-Type: application/json
Accept: application/json
```

---

## 🔐 **1. AUTHENTICATION APIs** 
*Base URL: `{{baseUrl}}/api/v1/auth`*

### 📝 **Register**
```bash
POST {{baseUrl}}/api/v1/auth/register
Content-Type: application/json

{
    "fullName": "Nguyen Van A",
    "email": "user@example.com",
    "password": "123456789",
    "phone": "0123456789"
}
```

### 🔑 **Login**
```bash
POST {{baseUrl}}/api/v1/auth/login
Content-Type: application/json

{
    "username": "user@example.com",
    "password": "123456789"
}

# Response sẽ trả về access_token - copy và paste vào {{token}}
```

### 🔄 **Refresh Token**
```bash
POST {{baseUrl}}/api/v1/auth/refresh
Content-Type: application/json
```

### 🚪 **Logout**
```bash
POST {{baseUrl}}/api/v1/auth/logout
Authorization: Bearer {{token}}
```

### 🔒 **Forgot Password**
```bash
POST {{baseUrl}}/api/v1/auth/forgot-password?email=user@example.com
```

### ✅ **Verify Email**
```bash
GET {{baseUrl}}/api/v1/auth/verify/{verification_token}
```

### 🔐 **Change Password**
```bash
POST {{baseUrl}}/api/v1/auth/change-password
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "currentPassword": "123456789",
    "newPassword": "newPassword123",
    "confirmPassword": "newPassword123"
}
```

---

## 👤 **2. USER MANAGEMENT APIs**
*Base URL: `{{baseUrl}}/api/v1`*

### 👤 **Get Current User Profile**
```bash
GET {{baseUrl}}/api/v1/users/me
Authorization: Bearer {{token}}
```

### 👤 **Get Current User Profile (Alias)**
```bash
GET {{baseUrl}}/api/v1/users/current
Authorization: Bearer {{token}}
```

### ✏️ **Update User Profile**
```bash
PATCH {{baseUrl}}/api/v1/users/me
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "fullName": "Updated Name",
    "phone": "0987654321",
    "address": "Updated Address"
}
```

### 👥 **Get All Users (Admin Only)**
```bash
GET {{baseUrl}}/api/v1/users
Authorization: Bearer {{adminToken}}
```

### 🔍 **Search Users (Admin Only)**
```bash
GET {{baseUrl}}/api/v1/users?name=John&email=john@&role=USER&status=true
Authorization: Bearer {{adminToken}}
```

### 👥 **Get All Users For Admin Dashboard**
```bash
GET {{baseUrl}}/api/v1/admin/users
Authorization: Bearer {{adminToken}}
```

### ➕ **Create New User (Admin Only)**
```bash
POST {{baseUrl}}/api/v1/users
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "fullName": "New User",
    "email": "newuser@example.com",
    "password": "123456789",
    "phone": "0123456789",
    "role": "USER"
}
```

### ✏️ **Update User (Admin Only)**
```bash
PUT {{baseUrl}}/api/v1/users/{userId}
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "fullName": "Updated User",
    "email": "updated@example.com",
    "phone": "0987654321",
    "isActive": true
}
```

---

## 🛍️ **3. PRODUCT APIs**
*Base URL: `{{baseUrl}}/api/v1`*

### 📋 **Get All Products (Basic)**
```bash
GET {{baseUrl}}/api/v1/products
GET {{baseUrl}}/api/v1/products?name=Jordan&minPrice=100&maxPrice=500
```

### 🔍 **Get Products With Advanced Filter (Paginated)**
```bash
GET {{baseUrl}}/api/v1/products/filter?name=Air&color=Black&minPrice=100&maxPrice=1000&categoryId=1&sortBy=price&sortDirection=ASC&page=0&pageSize=10
```

### 🔍 **Search Products (No Pagination)**
```bash
GET {{baseUrl}}/api/v1/products/search?name=Jordan&color=Red&productSize=42&minPrice=200&maxPrice=800&minStock=1&categoryId=1
```

### 📦 **Filter Products with POST Body**
```bash
POST {{baseUrl}}/api/v1/products/filter
Content-Type: application/json

{
    "name": "Air Jordan",
    "colors": ["Black", "Red"],
    "sizes": ["42", "43"],
    "minPrice": 100,
    "maxPrice": 1000,
    "minStock": 5,
    "categoryIds": [1, 2],
    "sortBy": "price",
    "sortDirection": "DESC",
    "page": 0,
    "pageSize": 12
}
```

### 🆕 **Create Product (Admin Only)**
```bash
POST {{baseUrl}}/api/v1/products
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "name": "Nike Air Max 97",
    "slug": "nike-air-max-97",
    "subTitle": "Comfortable Running Shoes",
    "description": "Premium running shoes with excellent cushioning",
    "price": 299.99,
    "stock": 100,
    "colors": ["White", "Black"],
    "sizes": ["40", "41", "42", "43"],
    "images": ["https://example.com/image1.jpg"],
    "categoryId": 1
}
```

### ✏️ **Update Product (Admin Only)**
```bash
PATCH {{baseUrl}}/api/v1/products/{{productId}}
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "name": "Updated Product Name",
    "price": 349.99,
    "stock": 150
}
```

### 🗑️ **Delete Product (Admin Only)**
```bash
DELETE {{baseUrl}}/api/v1/products/{{productId}}
Authorization: Bearer {{adminToken}}
```

### 👁️ **Get Product Detail**
```bash
GET {{baseUrl}}/api/v1/products/{{productId}}
```

### ⭐ **Get Featured Products**
```bash
GET {{baseUrl}}/api/v1/products/featured
GET {{baseUrl}}/api/v1/products/featured?limit=12
```

### 🏷️ **Get All Brands**
```bash
GET {{baseUrl}}/api/v1/products/brands
```

### 📂 **Get All Categories**
```bash
GET {{baseUrl}}/api/v1/products/categories
```

### 💡 **Get Search Suggestions**
```bash
GET {{baseUrl}}/api/v1/products/suggestions?query=Air&limit=5
```

---

## 📂 **4. CATEGORY APIs**
*Base URL: `{{baseUrl}}/api/categories`*

### 📋 **Get All Categories**
```bash
GET {{baseUrl}}/api/categories
```

---

## 🛒 **5. CART APIs**
*Base URL: `{{baseUrl}}/api/v1/carts`*

### 👁️ **Get Cart Items**
```bash
GET {{baseUrl}}/api/v1/carts
Authorization: Bearer {{token}}
```

### 🔢 **Get Cart Count**
```bash
GET {{baseUrl}}/api/v1/carts/count
Authorization: Bearer {{token}}
```

### ➕ **Add to Cart**
```bash
POST {{baseUrl}}/api/v1/carts/add
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "productId": 1,
    "quantity": 2,
    "size": "42"
}
```

### 🔄 **Update Cart Quantity**
```bash
PATCH {{baseUrl}}/api/v1/carts/update
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "productId": 1,
    "quantity": 3,
    "size": "42"
}
```

### 🗑️ **Remove Cart Item**
```bash
DELETE {{baseUrl}}/api/v1/carts/remove/{{productId}}/{{size}}
Authorization: Bearer {{token}}
```

---

## 📦 **6. ORDER APIs**
*Service Layer - Các API có thể cần implement controller*

### 🛍️ **Place Order**
```bash
# Cần implement OrderController
POST {{baseUrl}}/api/v1/orders
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "shippingAddress": "123 Main Street, City",
    "paymentMethod": "COD", // COD, VNPAY, MOMO, PAYPAL
    "phone": "0123456789",
    "couponCode": "NIKE2024" // Optional
}
```

### 📋 **Get User Orders**
```bash
# Cần implement OrderController
GET {{baseUrl}}/api/v1/orders
Authorization: Bearer {{token}}
```

### 👁️ **Get Order Detail**
```bash
# Cần implement OrderController
GET {{baseUrl}}/api/v1/orders/{{orderId}}
Authorization: Bearer {{token}}
```

### ❌ **Cancel Order**
```bash
# Cần implement OrderController
PATCH {{baseUrl}}/api/v1/orders/{{orderId}}/cancel
Authorization: Bearer {{token}}
```

### 💳 **VNPay Payment**
```bash
# VNPay callback endpoint
GET {{baseUrl}}/orders/vnpay/callback?vnp_ResponseCode=00&vnp_TxnRef={{txnRef}}
```

---

## 🎟️ **7. COUPON APIs**
*Base URL: `{{baseUrl}}/api/v1/coupons`*

### 📋 **Get Available Coupons**
```bash
GET {{baseUrl}}/api/v1/coupons
Authorization: Bearer {{token}}
```

### ✅ **Validate Coupon**
```bash
POST {{baseUrl}}/api/v1/coupons/validate
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "code": "NIKE2024",
    "orderAmount": 500
}
```

### 🧹 **Cleanup Expired Coupons (Admin)**
```bash
POST {{baseUrl}}/api/v1/coupons/cleanup-expired
Authorization: Bearer {{adminToken}}
```

---

## ⭐ **8. REVIEW APIs**
*Base URL: `{{baseUrl}}/api/v1/reviews`*

### ✍️ **Create Review**
```bash
POST {{baseUrl}}/api/v1/reviews
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "productId": 1,
    "orderId": 1,
    "rating": 5,
    "comment": "Excellent product!",
    "title": "Amazing shoes",
    "images": ["https://example.com/review1.jpg"]
}
```

### 📋 **Get Product Reviews**
```bash
GET {{baseUrl}}/api/v1/reviews/product/{{productId}}
```

### 🔍 **Filter Reviews**
```bash
POST {{baseUrl}}/api/v1/reviews/filter
Content-Type: application/json

{
    "productId": 1,
    "rating": 5,
    "sortBy": "newest",
    "page": 0,
    "pageSize": 10
}
```

### 📊 **Get Review Summary**
```bash
GET {{baseUrl}}/api/v1/reviews/product/{{productId}}/summary
```

### 🔍 **Search Reviews**
```bash
GET {{baseUrl}}/api/v1/reviews/product/{{productId}}/search?keyword=comfortable
```

### 📈 **Get Sorted Reviews**
```bash
GET {{baseUrl}}/api/v1/reviews/product/{{productId}}/sorted?sortBy=newest&page=0&size=10
```

### ✏️ **Update Review**
```bash
PUT {{baseUrl}}/api/v1/reviews/{{reviewId}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "rating": 4,
    "comment": "Updated review",
    "title": "Updated title"
}
```

### 💬 **Create Review Reply**
```bash
POST {{baseUrl}}/api/v1/reviews/{{reviewId}}/replies
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "comment": "Thank you for your feedback!"
}
```

### 📋 **Get Review Replies**
```bash
GET {{baseUrl}}/api/v1/reviews/{{reviewId}}/replies
```

---

## 🔧 **9. ADMIN DASHBOARD APIs**

### 📊 **Dashboard Overview**
```bash
GET {{baseUrl}}/admin/api/dashboard/overview
Authorization: Bearer {{adminToken}}
```

### 📈 **Dashboard Statistics**
```bash
GET {{baseUrl}}/api/admin/dashboard/statistics
Authorization: Bearer {{adminToken}}
```

### 💰 **Revenue Chart Data**
```bash
GET {{baseUrl}}/api/admin/dashboard/revenue-chart
Authorization: Bearer {{adminToken}}
```

### 🏆 **Top Products**
```bash
GET {{baseUrl}}/api/admin/dashboard/top-products
Authorization: Bearer {{adminToken}}
```

### 📊 **Order Status Stats**
```bash
GET {{baseUrl}}/api/admin/dashboard/order-status
Authorization: Bearer {{adminToken}}
```

### 📋 **Recent Activities**
```bash
GET {{baseUrl}}/admin/api/dashboard/activities
Authorization: Bearer {{adminToken}}
```

### ⚠️ **System Alerts**
```bash
GET {{baseUrl}}/admin/api/dashboard/alerts
Authorization: Bearer {{adminToken}}
```

### 📄 **Export Summary Report**
```bash
GET {{baseUrl}}/admin/api/dashboard/export/summary
Authorization: Bearer {{adminToken}}
```

---

## 🛍️ **10. ADMIN PRODUCT MANAGEMENT**
*Base URL: `{{baseUrl}}/admin/api/products`*

### 📋 **Get All Products (Admin)**
```bash
GET {{baseUrl}}/admin/api/products
Authorization: Bearer {{adminToken}}
GET {{baseUrl}}/admin/api/products?page=0&size=20&sort=name,asc
```

### 👁️ **Get Product by ID (Admin)**
```bash
GET {{baseUrl}}/admin/api/products/{{productId}}
Authorization: Bearer {{adminToken}}
```

### 🆕 **Create Product (Admin)**
```bash
POST {{baseUrl}}/admin/api/products
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "name": "Nike Air Force 1",
    "slug": "nike-air-force-1",
    "subTitle": "Classic Basketball Shoes",
    "description": "Iconic basketball shoes with timeless style",
    "price": 229.99,
    "stock": 200,
    "colors": ["White", "Black", "Red"],
    "sizes": ["39", "40", "41", "42", "43", "44"],
    "images": ["https://example.com/af1-1.jpg", "https://example.com/af1-2.jpg"],
    "categoryId": 2
}
```

### ✏️ **Update Product (Admin)**
```bash
PUT {{baseUrl}}/admin/api/products/{{productId}}
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
    "name": "Updated Product Name",
    "price": 259.99,
    "stock": 150
}
```

### 🗑️ **Delete Product (Admin)**
```bash
DELETE {{baseUrl}}/admin/api/products/{{productId}}
Authorization: Bearer {{adminToken}}
```

### 🔍 **Search Products (Admin)**
```bash
GET {{baseUrl}}/admin/api/products/search?name=Air&category=Running
Authorization: Bearer {{adminToken}}
```

### 📊 **Product Statistics**
```bash
# Total Products
GET {{baseUrl}}/admin/api/products/stats/total
Authorization: Bearer {{adminToken}}

# Out of Stock Products
GET {{baseUrl}}/admin/api/products/stats/out-of-stock
Authorization: Bearer {{adminToken}}

# Low Stock Products
GET {{baseUrl}}/admin/api/products/stats/low-stock
Authorization: Bearer {{adminToken}}

# Top Selling Products
GET {{baseUrl}}/admin/api/products/top-selling?limit=10
Authorization: Bearer {{adminToken}}
```

---

## 📦 **11. ADMIN ORDER MANAGEMENT**
*Base URL: `{{baseUrl}}/admin/orders` (Hiện tại là @Controller, có thể cần chuyển sang @RestController)*

### 📋 **Get Order Page**
```bash
GET {{baseUrl}}/admin/orders
Authorization: Bearer {{adminToken}}
```

### 👁️ **Get Order by ID**
```bash
GET {{baseUrl}}/admin/orders/{{orderId}}
Authorization: Bearer {{adminToken}}
```

### 🔄 **Update Order Status**
```bash
GET {{baseUrl}}/admin/orders/update-status?id={{orderId}}&status=confirmed
Authorization: Bearer {{adminToken}}
```

---

## 👥 **12. ADMIN USER MANAGEMENT**
*Base URL: `{{baseUrl}}/admin`*

### 📋 **Admin Users Page**
```bash
GET {{baseUrl}}/admin/users
Authorization: Bearer {{adminToken}}
```

### 👤 **Get User Statistics**
```bash
GET {{baseUrl}}/api/admin/users/statistics
Authorization: Bearer {{adminToken}}
```

### 📋 **Get Users List (API)**
```bash
GET {{baseUrl}}/api/admin/users
Authorization: Bearer {{adminToken}}
GET {{baseUrl}}/api/admin/users?search=john&role=USER&status=active&page=0&size=20
```

---

## ⭐ **13. ADMIN REVIEW MANAGEMENT**
*Base URL: `{{baseUrl}}/api/v1/admin/reviews`*

### 📋 **Get All Reviews (Admin)**
```bash
GET {{baseUrl}}/api/v1/admin/reviews
Authorization: Bearer {{adminToken}}
```

### 👁️ **Get Review Detail (Admin)**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}
Authorization: Bearer {{adminToken}}
```

### ✅ **Approve Review**
```bash
PUT {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}/approve
Authorization: Bearer {{adminToken}}
```

### 🙈 **Hide Review**
```bash
PUT {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}/hide
Authorization: Bearer {{adminToken}}
```

### ⏳ **Get Pending Reviews**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/pending
Authorization: Bearer {{adminToken}}
```

### 📊 **Review Statistics by Status**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/stats/by-status
Authorization: Bearer {{adminToken}}
```

### 📊 **Total Reviews Count**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/stats/total
Authorization: Bearer {{adminToken}}
```

### 📋 **Reviews by Product**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/by-product/{{productId}}
Authorization: Bearer {{adminToken}}
```

### 🗑️ **Delete Review**
```bash
DELETE {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}
Authorization: Bearer {{adminToken}}
```

### ⭐ **Filter Reviews by Rating**
```bash
GET {{baseUrl}}/api/v1/admin/reviews/by-rating?rating=5
Authorization: Bearer {{adminToken}}
```

---

## 🎟️ **14. ADMIN COUPON MANAGEMENT**
*Base URL: `{{baseUrl}}/admin/coupons`*

### 📋 **Coupon Management Page**
```bash
GET {{baseUrl}}/admin/coupons
Authorization: Bearer {{adminToken}}
```

---

## 🧪 **15. TEST SCENARIOS & WORKFLOWS**

### 🔄 **Complete User Journey Test**
```bash
# 1. Register new user
POST {{baseUrl}}/api/v1/auth/register

# 2. Login
POST {{baseUrl}}/api/v1/auth/login

# 3. Browse products
GET {{baseUrl}}/api/v1/products

# 4. Add to cart
POST {{baseUrl}}/api/v1/carts/add

# 5. View cart
GET {{baseUrl}}/api/v1/carts

# 6. Place order
POST {{baseUrl}}/api/v1/orders

# 7. View orders
GET {{baseUrl}}/api/v1/orders

# 8. Write review
POST {{baseUrl}}/api/v1/reviews
```

### 👨‍💼 **Admin Workflow Test**
```bash
# 1. Admin login
POST {{baseUrl}}/api/v1/auth/login

# 2. Dashboard stats
GET {{baseUrl}}/api/admin/dashboard/statistics

# 3. Manage products
GET {{baseUrl}}/admin/api/products
POST {{baseUrl}}/admin/api/products

# 4. Manage orders
GET {{baseUrl}}/admin/orders
GET {{baseUrl}}/admin/orders/update-status

# 5. Manage users
GET {{baseUrl}}/api/admin/users

# 6. Manage reviews
GET {{baseUrl}}/api/v1/admin/reviews/pending
PUT {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}/approve
```

### 🛒 **E-commerce Flow Test**
```bash
# 1. Browse products with filters
GET {{baseUrl}}/api/v1/products/filter?categoryId=1&minPrice=100&maxPrice=500

# 2. Search products
GET {{baseUrl}}/api/v1/products/search?name=Jordan

# 3. Get product details
GET {{baseUrl}}/api/v1/products/{{productId}}

# 4. Add multiple items to cart
POST {{baseUrl}}/api/v1/carts/add (multiple times)

# 5. Update cart quantities
PATCH {{baseUrl}}/api/v1/carts/update

# 6. Apply coupon
POST {{baseUrl}}/api/v1/coupons/validate

# 7. Checkout with different payment methods
POST {{baseUrl}}/api/v1/orders (COD)
POST {{baseUrl}}/api/v1/orders (VNPAY)

# 8. Review purchased products
POST {{baseUrl}}/api/v1/reviews
```

---

## 🚨 **TROUBLESHOOTING & COMMON ISSUES**

### ❌ **Authentication Issues**
```bash
# Check if token is valid
GET {{baseUrl}}/api/v1/users/me
Authorization: Bearer {{token}}

# If 401 Unauthorized, refresh token
POST {{baseUrl}}/api/v1/auth/refresh

# Or login again
POST {{baseUrl}}/api/v1/auth/login
```

### 🔍 **Debug API Calls**
```bash
# Check server health
GET {{baseUrl}}/api/v1/health

# Validate request body
# Ensure Content-Type: application/json
# Check required fields
# Verify data types
```

### 📊 **Performance Testing**
```bash
# Load test with pagination
GET {{baseUrl}}/api/v1/products?page=0&pageSize=50

# Filter performance
POST {{baseUrl}}/api/v1/products/filter

# Search performance
GET {{baseUrl}}/api/v1/products/suggestions?query=A
```

---

## 📝 **NOTES & BEST PRACTICES**

### ✅ **Request Guidelines**
- Always include `Content-Type: application/json` for POST/PUT/PATCH requests
- Use Bearer token authentication for protected endpoints
- Include proper error handling in tests
- Test both success and failure scenarios
- Validate response data structure
- Check HTTP status codes

### 🎯 **Testing Tips**
- Test with different user roles (USER, ADMIN, ROOT)
- Verify pagination works correctly
- Test edge cases (empty results, invalid IDs)
- Check data validation (required fields, formats)
- Test concurrent operations (cart updates, order placement)
- Verify business logic (stock updates, coupon usage)

### 🔒 **Security Testing**
- Test unauthorized access attempts
- Verify role-based access control
- Check input sanitization
- Test for SQL injection vulnerabilities
- Verify password policies
- Test session management

---

*📅 Created: December 2024*  
*🔄 Last Updated: December 2024*  
*👨‍💻 Nike Store API Testing Guide*