package com.proj.webprojrct.chatbot.listener;

import com.proj.webprojrct.chatbot.service.DocumentIngestionService;
import com.proj.webprojrct.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener để tự động đồng bộ sản phẩm với Chatbot Vector Database
 * Mỗi khi có product được tạo/cập nhật/xóa, listener sẽ tự động cập nhật vector DB
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final DocumentIngestionService documentIngestionService;

    /**
     * Xử lý khi có sản phẩm được tạo mới
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        try {
            Product product = event.getProduct();
            log.info("🔔 Product Created Event: Đang index sản phẩm ID {} vào chatbot...", product.getId());
            
            String productText = buildProductDescription(product);
            documentIngestionService.ingestText(
                    productText,
                    "product-" + product.getId(),
                    "product",
                    null
            );
            
            log.info("✅ Đã index sản phẩm ID {} vào chatbot thành công", product.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi khi index sản phẩm vào chatbot: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi có sản phẩm được cập nhật
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        try {
            Product product = event.getProduct();
            log.info("🔔 Product Updated Event: Đang cập nhật sản phẩm ID {} trong chatbot...", product.getId());
            
            // Xóa document cũ
            documentIngestionService.deleteBySource("product-" + product.getId());
            
            // Thêm document mới với dữ liệu cập nhật
            String productText = buildProductDescription(product);
            documentIngestionService.ingestText(
                    productText,
                    "product-" + product.getId(),
                    "product",
                    null
            );
            
            log.info("✅ Đã cập nhật sản phẩm ID {} trong chatbot thành công", product.getId());
        } catch (Exception e) {
            log.error("❌ Lỗi khi cập nhật sản phẩm trong chatbot: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi có sản phẩm bị xóa
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        try {
            Long productId = event.getProductId();
            log.info("🔔 Product Deleted Event: Đang xóa sản phẩm ID {} khỏi chatbot...", productId);
            
            documentIngestionService.deleteBySource("product-" + productId);
            
            log.info("✅ Đã xóa sản phẩm ID {} khỏi chatbot thành công", productId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi xóa sản phẩm khỏi chatbot: {}", e.getMessage(), e);
        }
    }

    /**
     * Build product description giống như trong DataSeedService
     */
    private String buildProductDescription(Product product) {
        StringBuilder desc = new StringBuilder();

        // 1. Tên sản phẩm
        desc.append("Tên sản phẩm: ").append(product.getName()).append(". ");
        
        if (product.getSubTitle() != null && !product.getSubTitle().isEmpty()) {
            desc.append(product.getSubTitle()).append(". ");
        }

        // 2. Category
        if (product.getCategory() != null) {
            desc.append("Loại: ").append(product.getCategory().getName()).append(". ");
        }

        // 3. Price
        desc.append("Giá: ").append(String.format("%,.0f", product.getPrice())).append(" VNĐ. ");
        
        if (product.getPrice() >= 5000000) {
            desc.append("(Phân khúc cao cấp). ");
        } else if (product.getPrice() >= 2000000) {
            desc.append("(Phân khúc trung cấp). ");
        } else {
            desc.append("(Phân khúc bình dân). ");
        }

        // 4. Description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            String shortDesc = product.getDescription().length() > 200 
                ? product.getDescription().substring(0, 200) + "..." 
                : product.getDescription();
            desc.append("Mô tả: ").append(shortDesc).append(" ");
        }

        // 5. Size
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            desc.append("Size có sẵn: ").append(String.join(", ", product.getSizes())).append(". ");
        }

        // 6. Stock
        if (product.getStock() != null) {
            if (product.getStock() > 10) {
                desc.append("Tình trạng: Còn hàng (").append(product.getStock()).append(" sản phẩm - Sẵn sàng giao ngay). ");
            } else if (product.getStock() > 0) {
                desc.append("Tình trạng: Còn hàng (").append(product.getStock()).append(" sản phẩm - Số lượng có hạn). ");
            } else {
                desc.append("Tình trạng: Tạm hết hàng. ");
            }
        }

        desc.append("Thương hiệu: Nike chính hãng. ");
        desc.append("Giao hàng toàn quốc. Đổi trả trong 30 ngày. Bảo hành 6 tháng.");

        return desc.toString();
    }

    /**
     * Event classes
     */
    public static class ProductCreatedEvent {
        private final Product product;
        
        public ProductCreatedEvent(Product product) {
            this.product = product;
        }
        
        public Product getProduct() {
            return product;
        }
    }

    public static class ProductUpdatedEvent {
        private final Product product;
        
        public ProductUpdatedEvent(Product product) {
            this.product = product;
        }
        
        public Product getProduct() {
            return product;
        }
    }

    public static class ProductDeletedEvent {
        private final Long productId;
        
        public ProductDeletedEvent(Long productId) {
            this.productId = productId;
        }
        
        public Long getProductId() {
            return productId;
        }
    }
}
