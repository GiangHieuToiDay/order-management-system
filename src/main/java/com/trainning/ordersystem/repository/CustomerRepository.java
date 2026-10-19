package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.user WHERE c.user.id = :userId")
    Optional<Customer> findByUserId(@Param("userId") Long userId);

    @Override
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.user WHERE c.id = :id")
    Optional<Customer> findById(@Param("id") Long id);
}
