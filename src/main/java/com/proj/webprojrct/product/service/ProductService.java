package com.proj.webprojrct.product.service;

import com.proj.webprojrct.category.dto.CategoryDto;
import com.proj.webprojrct.category.entity.Category;
import com.proj.webprojrct.category.repo.CategoryRepo;
import com.proj.webprojrct.product.dto.*;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.product.mapper.ProductMapper;
import com.proj.webprojrct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepo categoryRepo;

    public Page<Product> getFilteredProducts(List<Long> categoryIds, String text, String sort, String price, int page, int size) {
        if (sort == null) sort = "default";
        Sort sortOption = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name_asc" -> Sort.by("name").ascending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("id").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sortOption);

        // Parse price range: e.g. "0-1000000"
        Double min = null, max = null;
        if (StringUtils.hasText(price)) {
            String[] parts = price.split("-");
            if (parts.length == 2) {
                try {
                    min = Double.parseDouble(parts[0]);
                    max = Double.parseDouble(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (!categoryIds.isEmpty() && StringUtils.hasText(text) && min != null && max != null) {
            return productRepository.findByCategoryIdInAndNameContainingIgnoreCaseAndPriceBetweenAndIsDeleteFalse(categoryIds, text, min, max, pageable);
        } else if (!categoryIds.isEmpty() && min != null && max != null) {
            return productRepository.findByCategoryIdInAndPriceBetweenAndIsDeleteFalse(categoryIds, min, max, pageable);
        } else if (StringUtils.hasText(text) && min != null && max != null) {
            return productRepository.findByNameContainingIgnoreCaseAndPriceBetweenAndIsDeleteFalseAndCategory_IsDeleteFalse(text, min, max, pageable);
        } else if (min != null && max != null) {
            return productRepository.findByPriceBetweenAndIsDeleteFalseAndCategory_IsDeleteFalse(min, max, pageable);
        } else if (!categoryIds.isEmpty() && StringUtils.hasText(text)) {
            return productRepository.findByCategoryIdInAndNameContainingIgnoreCaseAndIsDeleteFalse(categoryIds, text, pageable);
        } else if (!categoryIds.isEmpty()) {
            return productRepository.findByCategoryIdInAndIsDeleteFalse(categoryIds, pageable);
        } else if (StringUtils.hasText(text)) {
            return productRepository.findByNameContainingIgnoreCaseAndIsDeleteFalseAndCategory_IsDeleteFalse(text, pageable);
        } else {
            return productRepository.findByIsDeleteFalseAndCategory_IsDeleteFalse(pageable);
        }
    }

    public List<ProductListDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    public List<Product> getNewProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByIsDeleteOrderByCreatedAtDesc(false, pageable).getContent();
    }

    public List<Product> searchProducts(String name, Double minPrice, Double maxPrice) {
        List<Product> products;

        // If no filters, return all
        if (name == null && minPrice == null && maxPrice == null) {
            products = productRepository.findByIsDeleteAndCategory_IsDeleteFalse(false);
        } else {
            // Use database query for better performance
            products = productRepository.searchProducts(name, minPrice, maxPrice);
        }

        return products;
    }

    /**
     * Lấy sản phẩm theo bộ lọc với phân trang
     */
    public ProductPageDto getProductsWithFilters(ProductFilterDto filter) {
        try {
            // Xử lý pagination
            int page = filter.getPage() != null ? filter.getPage() : 0;
            int size = filter.getPageSize() != null ? filter.getPageSize() : 10;

            // Xử lý sorting
            Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Product> productPage = Page.empty(pageable);

            // Kiểm tra xem có filter nào không
            boolean hasFilters = (filter.getName() != null && !filter.getName().trim().isEmpty()) ||
                    filter.getMinPrice() != null ||
                    filter.getMaxPrice() != null ||
                    (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty());

            if (!hasFilters) {
                // Nếu không có filter nào, sử dụng method đơn giản như featured products
                productPage = productRepository.findByIsDelete(false, pageable);
            } else if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                productPage = productRepository.findWithFiltersAndCategories(
                        filter.getName(),
                        filter.getMinPrice(),
                        filter.getMaxPrice(),
                        filter.getCategoryIds(),
                        pageable
                );
            }

            // Convert sang DTO
            List<ProductListDto> productDtos = productPage.getContent()
                    .stream()
                    .map(this::convertToListDto)
                    .collect(Collectors.toList());

            return new ProductPageDto(
                    productDtos,
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages(),
                    productPage.isFirst(),
                    productPage.isLast(),
                    productPage.hasNext(),
                    productPage.hasPrevious()
            );
        } catch (Exception e) {
            System.err.println("Error in getProductsWithFilters: " + e.getMessage());
            e.printStackTrace();
            // Return empty result instead of throwing exception
            return new ProductPageDto(
                    List.of(),
                    0,
                    filter.getPageSize() != null ? filter.getPageSize() : 10,
                    0L,
                    0,
                    true,
                    true,
                    false,
                    false
            );
        }
    }

    /**
     * Lấy sản phẩm theo bộ lọc không phân trang
     */
    public List<ProductListDto> getProductsWithFiltersNoPage(ProductFilterDto filter) {
        List<Product> products = productRepository.findWithFilters(
                filter.getName(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                0L
        );

        return products.stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        String sortField = "id"; // default
        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "name":
                    sortField = "name";
                    break;
                case "price":
                    sortField = "price";
                    break;
                case "createdat":
                case "created_at":
                    sortField = "createdAt";
                    break;
                case "stock":
                    sortField = "stock";
                    break;
                default:
                    sortField = "id";
            }
        }

        return Sort.by(direction, sortField);
    }

    private ProductListDto convertToListDto(Product product) {
        String thumbnail = null;

        try {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String firstImage = product.getImages().get(0);
                // Nếu là base64 image, sử dụng trực tiếp
                if (firstImage != null && firstImage.startsWith("data:image/")) {
                    thumbnail = firstImage;
                }
            }
        } catch (Exception e) {
            // Log error but continue processing
            System.err.println("Error processing images for product " + product.getId() + ": " + e.getMessage());
            thumbnail = null;
        }

        return new ProductListDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                thumbnail
        );
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public ProductResponseDto createProduct(ProductCreateDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setDelete(false);
        Product saved = productRepository.save(product);
        return productMapper.toResponseDto(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // --- Cập nhật dữ liệu cơ bản ---
        if (dto.getName() != null && !dto.getName().isBlank())
            product.setName(dto.getName().trim());

        if (dto.getSlug() != null && !dto.getSlug().isBlank())
            product.setSlug(dto.getSlug().trim());

        if (dto.getSubTitle() != null)
            product.setSubTitle(dto.getSubTitle());

        if (dto.getDescription() != null)
            product.setDescription(dto.getDescription());

        if (dto.getPrice() != null)
            product.setPrice(dto.getPrice());

        if (dto.getStock() != null)
            product.setStock(dto.getStock());

        // --- Cập nhật danh mục ---
        if (dto.getCategoryId() != null) {
            Category category = categoryRepo.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        // --- Cập nhật ảnh (chỉ khi có ảnh mới) ---
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setImages(dto.getImages());
        }

        // --- Lưu lại ---
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }


    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setDelete(true);
            productRepository.save(product);
        });
    }

    /**
     * Lấy danh sách sản phẩm nổi bật
     */
    public List<ProductListDto> getFeaturedProducts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 8);
        Page<Product> products = productRepository.findByIsDeleteOrderByCreatedAtDesc(false, pageable);

        return products.getContent()
                .stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả brands
     */
    public List<String> getAllBrands() {
        // Trả về default brands vì entity Product không có brand field
        return List.of("Nike", "Adidas", "Puma", "New Balance", "Converse", "Vans", "Reebok", "Jordan", "Under Armour", "ASICS");
    }

    /**
     * Lấy tất cả categories
     */
    public List<CategoryDto> getAllCategories() {
        return categoryRepo.findAll()
                .stream()
                .map(category -> new CategoryDto(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Lấy search suggestions
     */
    public List<String> getSearchSuggestions(String query, Integer limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit != null ? limit : 5);
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndIsDelete(
                query.trim(), false, pageable);

        return products.stream()
                .map(Product::getName)
                .distinct()
                .collect(Collectors.toList());
    }
}
