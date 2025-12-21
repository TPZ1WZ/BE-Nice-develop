package com.proj.webprojrct.cart.service;

import com.proj.webprojrct.cart.dto.CartResponse;
import com.proj.webprojrct.cart.entity.Cart;
import com.proj.webprojrct.cart.entity.CartItem;
import com.proj.webprojrct.cart.repository.CartItemRepository;
import com.proj.webprojrct.cart.repository.CartRepository;
import com.proj.webprojrct.order.repository.OrderRepository;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public CartResponse getCartByUser(User user) {
        System.out.println("🔍 Getting cart for user ID: " + user.getId());

        // Sử dụng getOrCreateCart để tránh duplicate
        Cart cart = getOrCreateCart(user);

        // Query items directly from database to avoid stale cache
        var freshItems = cartItemRepository.findAllByCartId(cart.getId());

        System.out.println("📦 Cart ID: " + cart.getId() + ", Fresh items count: " +
                (freshItems != null ? freshItems.size() : "null"));

        if (freshItems != null && !freshItems.isEmpty()) {
            System.out.println("📋 Cart items details:");
            freshItems.forEach(item -> {
                System.out.println("  - Product: " + item.getProduct().getName() +
                        ", Size: " + item.getSize() +
                        ", Quantity: " + item.getQuantity());
            });
        }

        return new CartResponse(cart);
    }

    @Transactional
    public void addToCart(User user, Long productId, Integer quantity, String size) {
        System.out.println("🛒 addToCart called - userId: " + user.getId() + ", productId: " + productId
                + ", quantity: " + quantity + ", size: " + size);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < quantity) {
            throw new RuntimeException("Quantity product not enough");
        }

        // Tìm hoặc tạo cart với synchronized để tránh duplicate
        Cart cart = getOrCreateCart(user);
        System.out.println("✅ Using cart with ID: " + cart.getId());

        // Tìm hoặc tạo cart item
        cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .ifPresentOrElse(cartItem -> {
                    System.out.println("📦 Updating existing cart item - Old quantity: " + cartItem.getQuantity());
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    cartItemRepository.save(cartItem);
                    System.out.println("📦 New quantity: " + cartItem.getQuantity());
                }, () -> {
                    System.out.println("➕ Creating new cart item");
                    var newCartItem = new CartItem();
                    newCartItem.setCart(cart);
                    newCartItem.setProduct(product);
                    newCartItem.setQuantity(quantity);
                    newCartItem.setProductPrice(product.getPrice());
                    newCartItem.setTotalPrice(product.getPrice() * quantity);
                    newCartItem.setSize(size);
                    cartItemRepository.save(newCartItem);
                    System.out.println("✅ Cart item saved successfully");
                });

        // Cập nhật tổng giá và số lượng của cart
        updateCartTotals(cart);
    }

    private synchronized Cart getOrCreateCart(User user) {
        // Tìm cart hiện có
        var existingCart = cartRepository.findByUserId(user.getId());
        if (existingCart.isPresent()) {
            System.out.println("✅ Found existing cart with ID: " + existingCart.get().getId());
            return existingCart.get();
        }

        // Nếu chưa có, thử tạo mới với error handling
        try {
            System.out.println("🆕 Creating new cart for user " + user.getId());
            var newCart = new Cart();
            newCart.setUser(user);
            var savedCart = cartRepository.save(newCart);
            System.out.println("✅ New cart created with ID: " + savedCart.getId());
            return savedCart;
        } catch (Exception e) {
            // Nếu bị duplicate key (concurrent creation), tìm lại cart
            System.out.println("⚠️ Exception creating cart: " + e.getMessage());
            System.out.println("🔄 Retrying to find existing cart...");

            var retryCart = cartRepository.findByUserId(user.getId());
            if (retryCart.isPresent()) {
                System.out.println("✅ Found cart on retry with ID: " + retryCart.get().getId());
                return retryCart.get();
            }

            // Nếu vẫn không tìm thấy, throw exception
            System.err.println("❌ Failed to get or create cart for user " + user.getId());
            throw new RuntimeException("Unable to get or create cart", e);
        }
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public void updateQuantity(User user, Long productId, Integer newQuantity, String size) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < newQuantity) {
            throw new RuntimeException("Quantity product not enough");
        }
        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));
        var cartItem = cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));
        cartItem.setQuantity(newQuantity);
        cartItem.setTotalPrice(cartItem.getProductPrice() * newQuantity);
        cartItemRepository.save(cartItem);

        // Cập nhật tổng giá và số lượng của cart
        updateCartTotals(cart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public void removeItem(User user, Long productId, String size) {
        System.out
                .println("🗑️ removeItem - userId: " + user.getId() + ", productId: " + productId + ", size: " + size);

        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));

        var cartItem = cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        System.out.println("🗑️ Deleting cart item ID: " + cartItem.getId());
        cartItemRepository.delete(cartItem);
        cartItemRepository.flush(); // Force delete to execute immediately

        // Cập nhật tổng giá và số lượng của cart
        updateCartTotals(cart);
    }

    // Xóa và return cart ngay lập tức (trong cùng transaction)
    @Transactional
    public CartResponse removeItemAndGetCart(User user, Long productId, String size) {
        System.out.println(
                "🗑️ removeItemAndGetCart - userId: " + user.getId() + ", productId: " + productId + ", size: " + size);

        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));

        // Debug: Print all cart items before attempting delete
        System.out.println("📦 Current cart items (cartId=" + cart.getId() + "):");
        var allItems = cartItemRepository.findAllByCartId(cart.getId());
        allItems.forEach(item -> {
            System.out.println("  - CartItem ID: " + item.getId() +
                    ", ProductID: " + item.getProduct().getId() +
                    ", Size: " + item.getSize() +
                    ", Quantity: " + item.getQuantity());
        });

        // ❌ Bỏ qua bước verify bằng method findBy... cũ vì có thể gây lỗi mapping
        // Chúng ta sẽ dùng chính câu lệnh DELETE JPQL đã fix để xóa và kiểm tra kết quả

        System.out.println("🗑️ Deleting cart item Using JPQL for ProductID: " + productId + ", Size: " + size);

        // Use JPQL DELETE query
        cartItemRepository.deleteByCartIdAndProductIdAndSize(cart.getId(), productId, size);

        System.out.println("✅ JPQL DELETE executed");

        // Clear Hibernate cache để query fresh từ database
        entityManager.clear();

        System.out.println("✅ Cart item deleted, now fetching fresh cart data");

        // Query fresh data from database sau khi clear cache
        var freshItems = cartItemRepository.findAllByCartId(cart.getId());
        System.out.println("📦 Fresh items count after delete: " + (freshItems != null ? freshItems.size() : "null"));

        if (freshItems != null && !freshItems.isEmpty()) {
            System.out.println("📋 Remaining cart items:");
            freshItems.forEach(item -> {
                System.out.println("  - Product: " + item.getProduct().getName() +
                        ", Size: " + item.getSize() +
                        ", Quantity: " + item.getQuantity());
            });
        } else {
            System.out.println("🧹 Cart is now empty");
        }

        // Query fresh cart entity sau khi clear cache
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // CRITICAL: Don't replace collection! Clear and add instead to avoid orphan
        // removal issues
        cart.getItems().clear();
        if (freshItems != null && !freshItems.isEmpty()) {
            cart.getItems().addAll(freshItems);
        }

        System.out.println("🔄 Cart entity refreshed with " + cart.getItems().size() + " items");

        return new CartResponse(cart);
    }

    // Helper method để tính toán lại tổng giá và số lượng
    private void updateCartTotals(Cart cart) {
        // Refresh cart items từ database
        var items = cartItemRepository.findByCartId(cart.getId());

        double totalPrice = items.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();

        int totalQuantity = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
        cartRepository.save(cart);

        System.out
                .println("💰 Cart totals updated - Total Price: " + totalPrice + ", Total Quantity: " + totalQuantity);
    }

    // Mua lại - Thêm tất cả sản phẩm từ đơn hàng vào giỏ
    @Transactional
    public Map<String, Object> reorderFromOrder(User user, Long orderId) {
        System.out.println("🔄 reorderFromOrder called - userId: " + user.getId() + ", orderId: " + orderId);

        // Kiểm tra đơn hàng có tồn tại và thuộc về user không
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập đơn hàng này");
        }

        // Kiểm tra trạng thái đơn hàng
        String status = order.getStatus().toUpperCase();
        if (!status.equals("COMPLETED") && !status.equals("DELIVERED")) {
            throw new RuntimeException("Chỉ có thể mua lại đơn hàng đã hoàn thành");
        }

        // Lấy giỏ hàng
        Cart cart = getOrCreateCart(user);

        // Danh sách sản phẩm không thể thêm
        List<String> unavailableProducts = new ArrayList<>();
        int successCount = 0;

        // Thêm từng sản phẩm trong đơn hàng vào giỏ
        for (var orderItem : order.getItems()) {
            try {
                var product = productRepository.findById(orderItem.getProduct().getId())
                        .orElse(null);

                if (product == null) {
                    unavailableProducts.add(orderItem.getProduct().getName() + " (không còn tồn tại)");
                    continue;
                }

                // Kiểm tra tồn kho
                if (product.getStock() < orderItem.getQuantity()) {
                    unavailableProducts.add(product.getName() + " (chỉ còn " + product.getStock() + " sản phẩm)");
                    continue;
                }

                // Thêm vào giỏ hàng
                String size = orderItem.getSize();
                int quantity = orderItem.getQuantity();

                cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, product.getId())
                        .ifPresentOrElse(cartItem -> {
                            cartItem.setQuantity(cartItem.getQuantity() + quantity);
                            cartItemRepository.save(cartItem);
                        }, () -> {
                            var newCartItem = new CartItem();
                            newCartItem.setCart(cart);
                            newCartItem.setProduct(product);
                            newCartItem.setQuantity(quantity);
                            newCartItem.setProductPrice(product.getPrice());
                            newCartItem.setTotalPrice(product.getPrice() * quantity);
                            newCartItem.setSize(size);
                            cartItemRepository.save(newCartItem);
                        });

                successCount++;

            } catch (Exception e) {
                System.err.println("Error adding product to cart: " + e.getMessage());
                unavailableProducts.add(orderItem.getProduct().getName() + " (lỗi hệ thống)");
            }
        }

        // Cập nhật tổng giá và số lượng
        updateCartTotals(cart);

        // Tạo response
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("successCount", successCount);
        result.put("totalItems", order.getItems().size());

        if (!unavailableProducts.isEmpty()) {
            result.put("unavailableProducts", unavailableProducts);
            result.put("message", successCount + "/" + order.getItems().size() + " sản phẩm đã được thêm vào giỏ hàng");
        } else {
            result.put("message", "Đã thêm tất cả sản phẩm vào giỏ hàng thành công");
        }

        System.out.println("✅ Reorder completed - Success: " + successCount + "/" + order.getItems().size());
        return result;
    }

}
