package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsByCategoryId(Long categoryId);

    @NativeQuery(
        value = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.stock_quantity AS stockQuantity,
                   p.description AS description,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status,
                   p.created_at AS createdAt,
                   p.updated_at AS updatedAt
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = :id
        """,
        sqlResultSetMapping = "ProductDetailMapping"
    )
    Optional<ProductDetailResponse> findDetailById(@Param("id") Long id);

    @NativeQuery(
        value = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.stock_quantity AS stockQuantity,
                   p.description AS description,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status,
                   p.created_at AS createdAt,
                   p.updated_at AS updatedAt
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.sku = :sku
        """,
        sqlResultSetMapping = "ProductDetailMapping"
    )
    Optional<ProductDetailResponse> findDetailBySku(@Param("sku") String sku);

    @NativeQuery(
        value = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = :id
        """,
        sqlResultSetMapping = "ProductSummaryMapping"
    )
    Optional<ProductSummaryResponse> findSummaryById(@Param("id") Long id);

    @NativeQuery(
        value = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:status IS NULL OR p.status = :status)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """,
        countQuery = """
            SELECT COUNT(p.id)
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:status IS NULL OR p.status = :status)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """,
        sqlResultSetMapping = "ProductSummaryMapping"
    )
    Page<ProductSummaryResponse> searchProductSummaries(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
           "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int deductStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity " +
           "WHERE p.id = :productId")
    int addStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Query("SELECT p.stockQuantity FROM Product p WHERE p.id = :id")
    int getStockQuantity(@Param("id") Long id);
}
