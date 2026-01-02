package com.proj.webprojrct.chatbot.service;

import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to seed initial data into vector store
 * Runs on application startup
 * Enabled - pgvector extension is now configured
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataSeedService { // REMOVED implements CommandLineRunner to disable auto-seed

    private final ProductRepository productRepository;
    private final DocumentIngestionService documentIngestionService;

    // @Override - DISABLED auto-seed on startup to save RAM
    public void run(String... args) {
        log.info("🌱 Starting data seeding for chatbot...");

        // Check each document type separately
        long existingProductCount = documentIngestionService.getDocumentCount("product");
        long existingFaqCount = documentIngestionService.getDocumentCount("faq");
        long existingPolicyCount = documentIngestionService.getDocumentCount("policy");

        try {
            if (existingProductCount == 0) {
                seedProducts();
            } else {
                log.info("📊 Vector store already contains {} product documents, skipping product seed", existingProductCount);
            }

            if (existingFaqCount == 0) {
                seedFAQs();
            } else {
                log.info("📊 Vector store already contains {} FAQ documents, skipping FAQ seed", existingFaqCount);
            }

            if (existingPolicyCount == 0) {
                seedPolicies();
            } else {
                log.info("📊 Vector store already contains {} policy documents, skipping policy seed", existingPolicyCount);
            }

            log.info("✅ Data seeding completed successfully");
        } catch (Exception e) {
            log.error("❌ Error during data seeding: {}", e.getMessage(), e);
        }
    }

    /**
     * Seed product information
     */
    public Map<String, Object> seedProducts() {
        log.info("📦 Seeding product data...");
        Map<String, Object> result = new HashMap<>();

        // 🗑️ XÓA DỮ LIỆU CŨ TRƯỚC KHI SEED LẠI (tránh trùng lặp)
        try {
            log.info("🗑️ Deleting old product documents...");
            documentIngestionService.deleteBySourceType("product");
            log.info("✅ Old product documents deleted");
        } catch (Exception e) {
            log.warn("⚠️ Could not delete old products: {}", e.getMessage());
        }

        List<Product> products = productRepository.findAll();
        result.put("found", products.size());
        log.info("Found {} products to index", products.size());

        int indexed = 0;
        int errors = 0;
        List<String> errorDetails = new ArrayList<>();

        for (Product product : products) {
            try {
                // Skip deleted products
                if (product.isDelete()) {
                    continue;
                }

                String productText = buildProductDescription(product);

                // Xóa metadata để tránh lỗi type conversion
                documentIngestionService.ingestText(
                        productText,
                        "product-" + product.getId(),
                        "product",
                        null);
                indexed++;

                if (indexed % 10 == 0) {
                    log.info("Indexed {} products...", indexed);
                }
            } catch (Exception e) {
                log.error("Error indexing product {}: {}", product.getId(), e.getMessage(), e);
                errors++;
                errorDetails.add("ID " + product.getId() + ": " + e.getMessage());
            }
        }

        log.info("✅ Indexed {} products", indexed);
        result.put("indexed", indexed);
        result.put("errors", errors);
        result.put("error_details", errorDetails);
        return result;
    }

    /**
     * Build product description for embedding - COMPREHENSIVE VERSION
     */
    private String buildProductDescription(Product product) {
        StringBuilder desc = new StringBuilder();

        // 1. Tên sản phẩm (product_name)
        desc.append("Tên sản phẩm: ").append(product.getName()).append(". ");
        
        if (product.getSubTitle() != null && !product.getSubTitle().isEmpty()) {
            desc.append(product.getSubTitle()).append(". ");
        }

        // 2. Category - loại giày
        if (product.getCategory() != null) {
            desc.append("Loại: ").append(product.getCategory().getName()).append(". ");
        }

        // 3. Price - giá bán
        desc.append("Giá: ").append(String.format("%,.0f", product.getPrice())).append(" VNĐ. ");
        
        // Phân loại giá để chatbot tư vấn dễ hơn
        if (product.getPrice() >= 5000000) {
            desc.append("(Phân khúc cao cấp, phù hợp người yêu thích chất lượng premium). ");
        } else if (product.getPrice() >= 2000000) {
            desc.append("(Phân khúc trung cấp, giá trị tốt). ");
        } else {
            desc.append("(Phân khúc bình dân, tiết kiệm). ");
        }

        // 4. Description - mô tả chi tiết
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            String shortDesc = product.getDescription().length() > 200 
                ? product.getDescription().substring(0, 200) + "..." 
                : product.getDescription();
            desc.append("Mô tả: ").append(shortDesc).append(" ");
        }

        // 5. Size available - size còn hàng
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            desc.append("Size có sẵn: ").append(String.join(", ", product.getSizes())).append(". ");
        } else {
            desc.append("Size: 39, 40, 41, 42, 43, 44 (các size phổ biến). ");
        }

        // 6. Status - tình trạng kho
        if (product.getStock() != null) {
            if (product.getStock() > 10) {
                desc.append("Tình trạng: Còn hàng (").append(product.getStock()).append(" sản phẩm - Sẵn sàng giao ngay). ");
            } else if (product.getStock() > 0) {
                desc.append("Tình trạng: Còn hàng (").append(product.getStock()).append(" sản phẩm - Số lượng có hạn, nên đặt sớm). ");
            } else {
                desc.append("Tình trạng: Tạm hết hàng (Có thể đặt trước hoặc chọn sản phẩm tương tự). ");
            }
        }

        // 7. Brand - thương hiệu (mặc định là Nike)
        desc.append("Thương hiệu: Nike chính hãng. ");

        // 8. Thông tin hỗ trợ khách hàng (ngắn gọn)
        desc.append("Giao hàng toàn quốc. Đổi trả trong 30 ngày. Bảo hành 6 tháng. ");

        return desc.toString();
    }

    /**
     * Seed FAQ data
     */
    public void seedFAQs() {
        log.info("❓ Seeding FAQ data...");

        Map<String, String> faqs = new HashMap<>();

        faqs.put("Chính sách đổi trả",
                "Nike Store hỗ trợ đổi trả trong vòng 30 ngày kể từ ngày mua hàng. " +
                        "Sản phẩm phải còn nguyên vẹn, chưa qua sử dụng, còn đầy đủ hộp và phụ kiện. " +
                        "Quý khách vui lòng mang theo hóa đơn mua hàng khi đến đổi trả.");

        faqs.put("Thời gian giao hàng",
                "Thời gian giao hàng từ 2-5 ngày làm việc đối với nội thành Hà Nội và TP.HCM. " +
                        "Các tỉnh thành khác từ 3-7 ngày làm việc. " +
                        "Giao hàng nhanh trong 24h có phụ thu thêm phí.");

        faqs.put("Phương thức thanh toán",
                "Nike Store chấp nhận các hình thức thanh toán: " +
                        "1. Tiền mặt khi nhận hàng (COD) " +
                        "2. Chuyển khoản ngân hàng " +
                        "3. Thẻ tín dụng/ghi nợ (Visa, Mastercard) " +
                        "4. Ví điện tử (MoMo, ZaloPay, VNPay)");

        faqs.put("Làm thế nào để chọn size giày phù hợp",
                "Để chọn size giày Nike phù hợp: " +
                        "1. Đo chiều dài bàn chân từ gót đến đầu ngón dài nhất " +
                        "2. Tham khảo bảng size chart của Nike " +
                        "3. Nếu bàn chân rộng, nên chọn size lớn hơn 0.5 " +
                        "4. Đối với giày thể thao, nên để khoảng trống 0.5-1cm ở phía trước " +
                        "5. Thử giày vào buổi chiều khi bàn chân hơi phồng lên");

        faqs.put("Chương trình khuyến mãi hiện tại",
                "Nike Store thường xuyên có các chương trình khuyến mãi: " +
                        "- Giảm giá 10-30% vào các dịp lễ lớn " +
                        "- Tích điểm thành viên: mua 10 triệu tặng voucher 500k " +
                        "- Free ship đơn hàng từ 1 triệu " +
                        "- Combo 2 đôi giảm thêm 15%");

        faqs.put("Bảo hành sản phẩm",
                "Tất cả sản phẩm Nike chính hãng đều được bảo hành 6 tháng. " +
                        "Bảo hành bao gồm: lỗi keo đế, sứt chỉ may, lỗi sản xuất. " +
                        "Không bảo hành: hư hỏng do sử dụng không đúng cách, mòn tự nhiên.");

        // Thông tin về số lượng sản phẩm (tự động cập nhật)
        long activeProducts = productRepository.findByIsDeleteAndCategory_IsDeleteFalse(false).size();
        faqs.put("Số lượng sản phẩm hiện có",
                String.format("Nike Store Vietnam hiện đang có **%d sản phẩm** đa dạng trong kho hàng của chúng tôi. " +
                        "Tất cả sản phẩm đều là hàng chính hãng Nike, được nhập khẩu trực tiếp. " +
                        "Bạn có thể tham khảo và lựa chọn từ nhiều dòng giày khác nhau như Air Force, Air Jordan, Air Max, Dunk, Pegasus và nhiều dòng khác. " +
                        "Để xem chi tiết từng sản phẩm, bạn có thể hỏi tôi về dòng giày cụ thể hoặc mức giá phù hợp với ngân sách của bạn.", 
                        activeProducts));

        // === KNOWLEDGE BASE CHUNG VỀ GIÀY NIKE ===
        
        faqs.put("Cách chọn size giày Nike chính xác",
                "HƯỚNG DẪN CHỌN SIZE NIKE:\n" +
                        "1. Đo bàn chân: Đo chiều dài từ gót đến đầu ngón dài nhất vào buổi chiều (khi chân hơi phồng).\n" +
                        "2. Tham khảo bảng size: Size US thường nhỏ hơn size EU 0.5-1 size.\n" +
                        "3. Quy tắc chọn size:\n" +
                        "   - Giày chạy bộ (Running): Chọn lớn hơn 0.5-1 size để thoải mái khi chạy\n" +
                        "   - Giày bóng rổ (Basketball): Vừa khít để hỗ trợ cổ chân\n" +
                        "   - Giày lifestyle: Chọn size thường xuyên mang\n" +
                        "4. Bàn chân rộng: Chọn size lớn hơn 0.5 hoặc model có width rộng.");

        faqs.put("Cách vệ sinh và bảo quản giày Nike",
                "HƯỚNG DẪN VỆ SINH GIÀY NIKE:\n" +
                        "1. Giày da (Leather): Dùng khăn ẩm lau nhẹ, tránh ngâm nước. Dùng kem dưỡng da định kỳ.\n" +
                        "2. Giày vải/mesh: Có thể giặt tay với nước xà phòng nhẹ, KHÔNG giặt máy.\n" +
                        "3. Đế giày: Dùng bàn chải mềm và nước xà phòng.\n" +
                        "4. Sấy khô: Phơi nơi thoáng mát, TRÁNH phơi nắng trực tiếp hoặc sấy nhiệt.\n" +
                        "BẢO QUẢN:\n" +
                        "- Bảo quản nơi khô ráo, thoáng mát\n" +
                        "- Nhét giấy báo vào giày khi không dùng để giữ form\n" +
                        "- Sử dụng túi chống ẩm nếu bảo quản lâu");

        faqs.put("Phân biệt giày Nike real và fake",
                "CÁCH PHÂN BIỆT GIÀY NIKE THẬT – GIẢ:\n" +
                        "1. Kiểm tra logo Swoosh: Logo thật có đường nét sắc sảo, cân đối, không bị lệch hoặc mờ.\n" +
                        "2. Chất liệu: Giày thật dùng chất liệu cao cấp, mềm mại, đều màu. Giày fake thường cứng, không đều.\n" +
                        "3. Đường chỉ may: Giày thật có đường chỉ đều, chắc chắn, không chỉ thừa. Giày fake thường chỉ lởm, không đều.\n" +
                        "4. Mã vạch SKU: Kiểm tra mã trên hộp và mã trong giày phải khớp nhau.\n" +
                        "5. Mùi: Giày thật có mùi da/cao su tự nhiên. Giày fake có mùi hóa chất nặng.\n" +
                        "6. Giá: Nếu giá rẻ bất thường (dưới 50% giá thị trường) → nghi ngờ hàng fake.\n" +
                        "LỜI KHUYÊN: Mua tại Nike Store chính hãng để đảm bảo 100% hàng real!");

        faqs.put("Các dòng giày Nike phổ biến",
                "CÁC DÒNG GIÀY NIKE PHỔ BIẾN:\n" +
                        "1. AIR FORCE 1: Giày lifestyle cổ điển, phù hợp mọi outfit, đế Air êm ái.\n" +
                        "2. AIR JORDAN: Giày bóng rổ huyền thoại, thiết kế iconic, nhiều phiên bản retro.\n" +
                        "3. AIR MAX: Có đệm khí Air Max nổi bật ở đế, phong cách thể thao-street.\n" +
                        "4. DUNK: Giày sneaker đa dụng, nhiều colorway, phù hợp phối đồ hàng ngày.\n" +
                        "5. BLAZER: Giày cổ cao/thấp, phong cách retro vintage.\n" +
                        "6. PEGASUS: Giày chạy bộ chuyên nghiệp, nhẹ, đệm tốt.\n" +
                        "7. TIEMPO: Giày bóng đá da cao cấp, cảm giác chạm bóng tuyệt vời.\n" +
                        "8. REACT: Công nghệ đế React êm ái, phù hợp chạy bộ và đi hàng ngày.");

        faqs.put("Tư vấn phối đồ với giày Nike",
                "TƯ VẤN PHỐI ĐỒ VỚI GIÀY NIKE:\n" +
                        "1. AIR FORCE 1 TRẮNG: All-match, phối được mọi outfit từ jeans, shorts đến váy.\n" +
                        "2. AIR JORDAN RETRO: Phối với quần jogger, jeans baggy, áo hoodie/bomber jacket.\n" +
                        "3. GIÀY CHẠY BỘ (Running): Phối với quần thể thao, legging, áo thun/tank top.\n" +
                        "4. DUNK: Phù hợp streetwear - jeans ống rộng, áo thun oversized, áo khoác bomber.\n" +
                        "5. BLAZER: Phong cách vintage - quần kaki, sơ mi, áo len.\n" +
                        "QUY TẮC CHUNG:\n" +
                        "- Giày trắng: Dễ phối, chọn màu quần áo tương phản\n" +
                        "- Giày màu sắc: Chọn 1 item quần áo cùng tone màu\n" +
                        "- Giày cổ cao: Phối với quần ngắn hoặc quần ống ôm");

        faqs.put("Ai phù hợp mang giày Nike",
                "GIÀY NIKE PHÙ HỢP CHO:\n" +
                        "1. Người chơi thể thao: Bóng rổ, chạy bộ, bóng đá, tập gym → chọn dòng chuyên biệt (Jordan, Pegasus, Tiempo)\n" +
                        "2. Người yêu thích sneaker/streetwear: Air Force 1, Dunk, Blazer\n" +
                        "3. Người cần giày đi làm-đi chơi: Dòng lifestyle như Air Max, React, Court Vision\n" +
                        "4. Học sinh, sinh viên: Air Force 1, Dunk (bền, dễ phối đồ, giá hợp lý)\n" +
                        "5. Người trưởng thành: Dòng cao cấp như Jordan Retro, Air Max 90/97\n" +
                        "6. Trẻ em: Nike Kids với thiết kế an toàn, êm ái cho bàn chân đang phát triển");

        faqs.put("Về đội ngũ phát triển Nike Store",
                "THÔNG TIN ĐỘI NGŨ PHÁT TRIỂN:\n" +
                        "🏆 Nike Store được phát triển bởi team sinh viên tài năng từ Đại học Sư phạm Kỹ thuật TP.HCM\n\n" +
                        "👥 THÀNH VIÊN:\n" +
                        "   • Phát - Thợ săn: Full Stack Developer + AI Engineer, chuyên xây dựng chatbot và tích hợp AI\n" +
                        "   • Kiệt - Máy bào: Chuyên gia xử lý dữ liệu + UI/UX Designer\n" +
                        "   • Lâm - Thợ điện bất ổn: Chuyên hệ thống nhúng (nhưng hơi bất ổn)\n" +
                        "   • Đạt - Quả tạ: Đang nạp thêm kiến thức và rèn luyện kỹ năng\n\n" +
                        "🎯 THÀNH TỰU:\n" +
                        "   • Từng tham gia thi Hackathon tại trường\n" +
                        "   • Đồ án Android cuối kỳ với mục tiêu 10 điểm\n" +
                        "   • Xây dựng hệ thống Nike Store hoàn chỉnh với chatbot AI\n\n" +
                        "💪 SỨ MỆNH: Mang đến trải nghiệm mua sắm giày Nike tốt nhất cho khách hàng Việt Nam!");

        int indexed = 0;
        for (Map.Entry<String, String> entry : faqs.entrySet()) {
            try {
                // Xóa metadata để tránh lỗi type conversion
                documentIngestionService.ingestText(
                        "Câu hỏi: " + entry.getKey() + "\n\nTrả lời: " + entry.getValue(),
                        "faq-" + entry.getKey(),
                        "faq",
                        null);
                indexed++;
            } catch (Exception e) {
                log.error("Error indexing FAQ: {}", e.getMessage());
            }
        }

        log.info("✅ Indexed {} FAQs", indexed);
    }

    /**
     * Seed policy data
     */
    public void seedPolicies() {
        log.info("📋 Seeding policy data...");

        Map<String, String> policies = new HashMap<>();

        policies.put("Chính sách bảo mật",
                "Nike Store cam kết bảo mật thông tin khách hàng tuyệt đối. " +
                        "Thông tin cá nhân chỉ được sử dụng cho mục đích xử lý đơn hàng và chăm sóc khách hàng. " +
                        "Chúng tôi không chia sẻ thông tin với bên thứ ba khi chưa có sự đồng ý.");

        policies.put("Điều khoản sử dụng",
                "Khi mua hàng tại Nike Store, khách hàng đồng ý tuân thủ các điều khoản: " +
                        "- Cung cấp thông tin chính xác khi đặt hàng " +
                        "- Thanh toán đúng hạn theo phương thức đã chọn " +
                        "- Kiểm tra kỹ sản phẩm khi nhận hàng " +
                        "- Liên hệ ngay nếu có vấn đề với đơn hàng");

        policies.put("Thông tin liên hệ Nike Store",
                "LIÊN HỆ NIKE STORE:\n" +
                        "📍 Địa chỉ cửa hàng:\n" +
                        "   - Hà Nội: 123 Nguyễn Trãi, Thanh Xuân, Hà Nội\n" +
                        "   - TP.HCM: 456 Lê Lợi, Quận 1, TP.HCM\n" +
                        "📞 Hotline: 1900-xxxx (8h-22h hàng ngày)\n" +
                        "📧 Email: support@nikestore.vn\n" +
                        "🌐 Website: www.nikestore.vn\n" +
                        "💬 Chat: Chatbot 24/7 hoặc nhân viên tư vấn trong giờ hành chính");

        policies.put("Chi tiết chính sách đổi trả",
                "CHÍNH SÁCH ĐỔI TRẢ CHI TIẾT:\n" +
                        "✅ ĐIỀU KIỆN ĐỔI TRẢ:\n" +
                        "   - Trong vòng 30 ngày kể từ ngày mua\n" +
                        "   - Sản phẩm chưa qua sử dụng, còn nguyên tem mác\n" +
                        "   - Còn đầy đủ hộp, phụ kiện đi kèm\n" +
                        "   - Có hóa đơn mua hàng\n" +
                        "✅ CÁCH THỨC:\n" +
                        "   - Mang sản phẩm đến cửa hàng\n" +
                        "   - Hoặc gửi qua đường chuyển phát (chi phí vận chuyển do khách hàng chi trả)\n" +
                        "✅ HOÀN TIỀN:\n" +
                        "   - Hoàn tiền trong 5-7 ngày làm việc\n" +
                        "   - Hoàn về tài khoản/ví đã thanh toán\n" +
                        "❌ KHÔNG ĐƯỢC ĐỔI TRẢ:\n" +
                        "   - Sản phẩm đã qua sử dụng, giặt tẩy\n" +
                        "   - Hết thời hạn 30 ngày\n" +
                        "   - Không có hóa đơn");

        policies.put("Chi tiết chính sách giao hàng",
                "CHÍNH SÁCH GIAO HÀNG CHI TIẾT:\n" +
                        "🚚 THỜI GIAN GIAO HÀNG:\n" +
                        "   - Nội thành HN, HCM: 1-3 ngày\n" +
                        "   - Các tỉnh thành khác: 3-7 ngày\n" +
                        "   - Vùng xa, đảo: 7-14 ngày\n" +
                        "💰 PHÍ VẬN CHUYỂN:\n" +
                        "   - FREE SHIP đơn hàng từ 1 triệu đồng\n" +
                        "   - Dưới 1 triệu: 30.000đ (nội thành), 50.000đ (tỉnh)\n" +
                        "   - Giao hàng nhanh 24h: +50.000đ\n" +
                        "📦 KIỂM TRA HÀNG:\n" +
                        "   - Được kiểm tra hàng trước khi thanh toán (COD)\n" +
                        "   - Từ chối nhận nếu sản phẩm không đúng/hư hỏng\n" +
                        "📍 THEO DÕI ĐƠN HÀNG:\n" +
                        "   - Nhận mã vận đơn qua SMS/Email\n" +
                        "   - Tra cứu trên website hoặc liên hệ hotline");

        int indexed = 0;
        for (Map.Entry<String, String> entry : policies.entrySet()) {
            try {
                // Xóa metadata để tránh lỗi type conversion
                documentIngestionService.ingestText(
                        entry.getKey() + ":\n\n" + entry.getValue(),
                        "policy-" + entry.getKey(),
                        "policy",
                        null);
                indexed++;
            } catch (Exception e) {
                log.error("Error indexing policy: {}", e.getMessage());
            }
        }

        log.info("✅ Indexed {} policies", indexed);
    }

    /**
     * Sync single product to vector store (auto-update)
     * Call this method when creating/updating a product
     */
    public void syncSingleProduct(Product product) {
        try {
            if (product == null || product.isDelete()) {
                return;
            }

            String productText = buildProductDescription(product);
            documentIngestionService.ingestText(
                    productText,
                    "product-" + product.getId(),
                    "product",
                    null);
            
            log.info("✅ Synced product {} to chatbot", product.getId());
        } catch (Exception e) {
            log.error("❌ Error syncing product {}: {}", product.getId(), e.getMessage(), e);
        }
    }

    /**
     * Remove single product from vector store (when deleted)
     * Call this method when soft-deleting a product
     */
    public void removeSingleProduct(Long productId) {
        try {
            documentIngestionService.deleteBySource("product-" + productId);
            log.info("✅ Removed product {} from chatbot", productId);
        } catch (Exception e) {
            log.error("❌ Error removing product {}: {}", productId, e.getMessage(), e);
        }
    }
}
