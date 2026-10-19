package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.role.RoleResponse;
import com.trainning.ordersystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);

    @NativeQuery(
        value = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
        """,
        sqlResultSetMapping = "RoleResponseMapping"
    )
    List<RoleResponse> findAllRoleResponses();

    @NativeQuery(
        value = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
            WHERE r.id = :id
        """,
        sqlResultSetMapping = "RoleResponseMapping"
    )
    Optional<RoleResponse> findRoleResponseById(@Param("id") Long id);

    @NativeQuery(
        value = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.description AS description
            FROM roles r
            WHERE r.name = :name
        """,
        sqlResultSetMapping = "RoleResponseMapping"
    )
    Optional<RoleResponse> findRoleResponseByName(@Param("name") String name);
}
