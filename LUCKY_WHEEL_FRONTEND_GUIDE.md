# 🎰 VÒNG QUAY MAY MẮN - HƯỚNG DẪN FRONTEND

## 📱 Luồng hoàn chỉnh cho Popup kết quả

### 1️⃣ **Thông báo kết quả + Copy mã voucher**

```kotlin
// ResultDialog.kt
class SpinResultDialog(
    private val spinHistory: SpinHistory,
    private val onUseNow: (String) -> Unit,
    private val onSaveToWallet: (Long) -> Unit
) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogSpinResultBinding.inflate(inflater, container, false)
        
        // Hiển thị kết quả
        binding.tvCongratulations.text = "Chúc mừng bạn!"
        binding.tvPrizeName.text = "Bạn đã trúng: ${spinHistory.prize.name}"
        binding.tvCouponCode.text = spinHistory.prizeCode
        
        // Copy mã voucher
        binding.layoutCouponCode.setOnClickListener {
            copyToClipboard(spinHistory.prizeCode)
            Toast.makeText(context, "Đã copy mã: ${spinHistory.prizeCode}", Toast.LENGTH_SHORT).show()
        }
        
        // Nút DÙNG NGAY
        binding.btnUseNow.setOnClickListener {
            dismiss()
            onUseNow(spinHistory.prizeCode)
        }
        
        // Nút LƯU VÀO VÍ VOUCHER
        binding.btnSaveToWallet.setOnClickListener {
            saveToWallet(spinHistory.id)
        }
        
        return binding.root
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mã voucher", text)
        clipboard.setPrimaryClip(clip)
    }
    
    private fun saveToWallet(historyId: Long) {
        // Gọi API lưu vào ví
        val call = luckyWheelApi.saveToWallet("Bearer $token", historyId)
        call.enqueue(object : Callback<SaveWalletResponse> {
            override fun onResponse(call: Call<SaveWalletResponse>, response: Response<SaveWalletResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "✅ Đã lưu vào ví voucher", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "❌ " + response.body()?.message, Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<SaveWalletResponse>, t: Throwable) {
                Toast.makeText(context, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
```

---

### 2️⃣ **Nút DÙNG NGAY - Flow hoàn chỉnh**

```kotlin
// LuckyWheelActivity.kt
private fun handleUseNow(couponCode: String) {
    // Bước 1: Kiểm tra giỏ hàng
    cartApi.getCart("Bearer $token").enqueue(object : Callback<CartResponse> {
        override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
            if (response.isSuccessful) {
                val cart = response.body()
                
                if (cart == null || cart.items.isEmpty()) {
                    // Giỏ hàng trống → đi về trang sản phẩm
                    navigateToShoppingPage()
                    Toast.makeText(this@LuckyWheelActivity, 
                        "Hãy thêm sản phẩm vào giỏ hàng để dùng voucher", 
                        Toast.LENGTH_LONG).show()
                } else {
                    // Có giỏ hàng → đi tới checkout với voucher
                    navigateToCheckout(couponCode)
                }
            }
        }
        
        override fun onFailure(call: Call<CartResponse>, t: Throwable) {
            Toast.makeText(this@LuckyWheelActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}

private fun navigateToShoppingPage() {
    val intent = Intent(this, MainActivity::class.java)
    intent.putExtra("navigate_to", "shop")
    startActivity(intent)
    finish()
}

private fun navigateToCheckout(couponCode: String) {
    val intent = Intent(this, CheckoutActivity::class.java)
    intent.putExtra("auto_apply_coupon", couponCode)
    startActivity(intent)
    finish()
}
```

---

### 3️⃣ **Checkout Activity - Tự động áp dụng voucher**

```kotlin
// CheckoutActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Nhận voucher từ intent
    val autoApplyCoupon = intent.getStringExtra("auto_apply_coupon")
    
    if (autoApplyCoupon != null) {
        // Tự động apply voucher
        applyCoupon(autoApplyCoupon)
    }
}

private fun applyCoupon(couponCode: String) {
    val request = ApplyCouponRequest(couponCode, calculateTotalAmount())
    
    couponApi.applyCoupon(request).enqueue(object : Callback<CouponDiscountResponse> {
        override fun onResponse(call: Call<CouponDiscountResponse>, response: Response<CouponDiscountResponse>) {
            if (response.isSuccessful) {
                val result = response.body()
                
                if (result?.isValid == true) {
                    // Hiển thị voucher đã áp dụng
                    binding.tvAppliedCoupon.text = couponCode
                    binding.tvDiscountAmount.text = "-${formatCurrency(result.discountAmount)}"
                    binding.tvFinalAmount.text = formatCurrency(result.finalAmount)
                    
                    // Lưu để submit order
                    appliedCouponCode = couponCode
                    discountAmount = result.discountAmount ?: 0.0
                    
                    Toast.makeText(this@CheckoutActivity, 
                        "✅ Đã áp dụng voucher giảm ${formatCurrency(result.discountAmount)}", 
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@CheckoutActivity, 
                        "❌ ${result?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        override fun onFailure(call: Call<CouponDiscountResponse>, t: Throwable) {
            Toast.makeText(this@CheckoutActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}

private fun submitOrder() {
    // Đánh dấu voucher đã sử dụng
    if (appliedCouponCode != null) {
        couponApi.useCoupon("Bearer $token", appliedCouponCode!!).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Checkout", "Voucher marked as used")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Checkout", "Failed to mark voucher: ${t.message}")
            }
        })
    }
    
    // Tiếp tục tạo order...
}
```

---

## 🔌 **API Endpoints cần tích hợp**

### 1. Lưu vào ví
```kotlin
interface LuckyWheelApi {
    @POST("/api/v1/lucky-wheel/save-to-wallet/{historyId}")
    fun saveToWallet(
        @Header("Authorization") token: String,
        @Path("historyId") historyId: Long
    ): Call<SaveWalletResponse>
}

data class SaveWalletResponse(
    val success: Boolean,
    val message: String,
    val userCoupon: UserCoupon?
)
```

### 2. Lấy voucher trong ví
```kotlin
@GET("/api/v1/lucky-wheel/my-wallet")
fun getMyWallet(
    @Header("Authorization") token: String
): Call<List<UserCoupon>>
```

### 3. Áp dụng voucher
```kotlin
interface CouponApi {
    @POST("/api/v1/coupons/apply")
    fun applyCoupon(
        @Body request: ApplyCouponRequest
    ): Call<CouponDiscountResponse>
    
    @POST("/api/v1/coupons/{code}/use")
    fun useCoupon(
        @Header("Authorization") token: String,
        @Path("code") code: String
    ): Call<Void>
}
```

---

## 📦 **Data Models**

```kotlin
data class UserCoupon(
    val id: Long,
    val couponCode: String,
    val prizeName: String,
    val prizeDescription: String?,
    val discountValue: Double?,
    val prizeType: String, // VOUCHER, FREESHIP
    val isUsed: Boolean,
    val expiresAt: String,
    val savedAt: String
)

data class ApplyCouponRequest(
    val couponCode: String,
    val orderAmount: Double
)

data class CouponDiscountResponse(
    val isValid: Boolean,
    val message: String,
    val discountAmount: Double?,
    val finalAmount: Double?,
    val coupon: CouponResponse?
)
```

---

## 🎨 **Layout XML cho Dialog**

```xml
<!-- dialog_spin_result.xml -->
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cardCornerRadius="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <!-- Icon -->
        <ImageView
            android:layout_width="80dp"
            android:layout_height="80dp"
            android:layout_gravity="center"
            android:src="@drawable/ic_gift" />

        <!-- Chúc mừng -->
        <TextView
            android:id="@+id/tvCongratulations"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="Chúc mừng bạn!"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@color/colorPrimary"
            android:layout_marginTop="16dp" />

        <!-- Tên phần thưởng -->
        <TextView
            android:id="@+id/tvPrizeName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="Bạn đã trúng: Giảm 10%"
            android:textSize="18sp"
            android:layout_marginTop="8dp" />

        <!-- Mã voucher (có thể copy) -->
        <LinearLayout
            android:id="@+id/layoutCouponCode"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:background="@drawable/bg_coupon_code"
            android:padding="12dp"
            android:layout_marginTop="16dp"
            android:gravity="center">

            <TextView
                android:text="Mã voucher của bạn:"
                android:textSize="14sp"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content" />

            <TextView
                android:id="@+id/tvCouponCode"
                android:text="VCE9B90107"
                android:textSize="18sp"
                android:textStyle="bold"
                android:textColor="@color/colorAccent"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp" />

            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:src="@drawable/ic_copy"
                android:layout_marginStart="8dp"
                android:tint="@color/colorAccent" />
        </LinearLayout>

        <!-- Nút DÙNG NGAY -->
        <Button
            android:id="@+id/btnUseNow"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="✨ DÙNG NGAY"
            android:textSize="16sp"
            android:textStyle="bold"
            android:background="@drawable/bg_button_primary"
            android:layout_marginTop="16dp" />

        <!-- Nút LƯU VÀO VÍ -->
        <Button
            android:id="@+id/btnSaveToWallet"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="💰 Lưu vào ví voucher"
            android:textSize="16sp"
            android:background="@drawable/bg_button_secondary"
            android:layout_marginTop="12dp" />

    </LinearLayout>

</androidx.cardview.widget.CardView>
```

---

## ✅ **Checklist triển khai**

- [ ] Tạo `SpinResultDialog.kt`
- [ ] Thêm API endpoints vào Retrofit
- [ ] Implement logic "Dùng ngay" với kiểm tra giỏ hàng
- [ ] Implement "Lưu vào ví"
- [ ] Tự động áp dụng voucher ở CheckoutActivity
- [ ] Tạo màn hình "Ví voucher" để xem danh sách đã lưu
- [ ] Test flow: Quay → Dùng ngay → Checkout → Submit order
- [ ] Test flow: Quay → Lưu ví → Vào ví → Chọn voucher → Checkout

---

## 🗂️ **Màn hình VÍ VOUCHER bổ sung**

```kotlin
// MyWalletActivity.kt
class MyWalletActivity : AppCompatActivity() {
    
    private lateinit var adapter: WalletAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        loadMyWallet()
    }
    
    private fun loadMyWallet() {
        luckyWheelApi.getMyWallet("Bearer $token").enqueue(object : Callback<List<UserCoupon>> {
            override fun onResponse(call: Call<List<UserCoupon>>, response: Response<List<UserCoupon>>) {
                if (response.isSuccessful) {
                    val vouchers = response.body() ?: emptyList()
                    adapter.submitList(vouchers)
                }
            }
            
            override fun onFailure(call: Call<List<UserCoupon>>, t: Throwable) {
                Toast.makeText(this@MyWalletActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
```

---

🎉 **Hoàn thành!** Frontend giờ có đầy đủ chức năng:
- ✅ Popup kết quả đẹp với copy mã
- ✅ Nút "Dùng ngay" thông minh
- ✅ Lưu vào ví voucher
- ✅ Quản lý ví voucher
