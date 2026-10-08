package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.Category;
import com.trainning.ordersystem.entity.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByStatus(CategoryStatus status);

    @Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<Category> searchCategories(@Param("keyword") String keyword,
                                   @Param("status") CategoryStatus status,
                                   Pageable pageable);
}
