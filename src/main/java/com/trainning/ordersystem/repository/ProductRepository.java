package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.entity.enums.ProductStatus;
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

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.product.ProductDetailResponse(
            p.id,p.name,p.sku,p.price,p.unit,p.stockQuantity,p.description,p.imageUrl,c.id,c.name,p.status,p.createdAt,p.updatedAt
        )
        FROM Product p
        LEFT JOIN p.category c
        WHERE p.id = :id
    """)
    Optional<ProductDetailResponse> findDetailById(@Param("id") Long id);

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.product.ProductDetailResponse(
            p.id,p.name,p.sku,p.price,p.unit,p.stockQuantity,p.description,p.imageUrl,c.id,c.name,p.status,p.createdAt,p.updatedAt
        )
        FROM Product p
        LEFT JOIN p.category c
        WHERE p.sku = :sku
    """)
    Optional<ProductDetailResponse> findDetailBySku(@Param("sku") String sku);

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.product.ProductSummaryResponse(
            p.id, p.name, p.sku, p.price, p.unit, p.imageUrl, c.id, c.name, p.status
        )
        FROM Product p
        LEFT JOIN p.category c
        WHERE p.id = :id
    """)
    Optional<ProductSummaryResponse> findSummaryById(@Param("id") Long id);

    @Query(value = """
    SELECT new com.trainning.ordersystem.dto.response.product.ProductSummaryResponse(
        p.id, p.name, p.sku, p.price, p.unit, p.imageUrl, c.id, c.name, p.status
    )
    FROM Product p
    LEFT JOIN p.category c
    WHERE (:keyword IS NULL OR :keyword = '' 
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryId IS NULL OR c.id = :categoryId)
      AND (:status IS NULL OR p.status = :status)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    Page<ProductSummaryResponse> searchProductSummaries(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") ProductStatus status,
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
