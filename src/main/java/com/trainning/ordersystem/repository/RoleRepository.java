package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.role.RoleResponse;
import com.trainning.ordersystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.role.RoleResponse(r.id, r.name, r.description)
        FROM Role r
    """)
    List<RoleResponse> findAllRoleResponses();

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.role.RoleResponse(r.id, r.name, r.description)
        FROM Role r
        WHERE r.id = :id
    """)
    Optional<RoleResponse> findRoleResponseById(@Param("id") Long id);

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.role.RoleResponse(r.id, r.name, r.description)
        FROM Role r
        WHERE r.name = :name
    """)
    Optional<RoleResponse> findRoleResponseByName(@Param("name") String name);
}
